/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.deferred;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import havoc.client.render.engine.rhi.HavocRhi;
import havoc.client.render.engine.rhi.shader.ComputeDispatchCommand;
import havoc.client.render.engine.rhi.shader.ComputePipelineDescriptor;
import havoc.client.render.engine.rhi.shader.RhiComputePipeline;
import havoc.client.render.engine.rhi.shader.RhiShaderStage;
import havoc.client.render.engine.rhi.shader.RhiStorageBuffer;
import havoc.client.render.engine.rhi.shader.RhiStorageImage;
import havoc.client.render.engine.rhi.shader.SampledTextureBinding;
import havoc.client.render.engine.rhi.shader.ShaderResourceKind;
import havoc.client.render.engine.rhi.shader.ShaderResourceLayout;
import havoc.client.render.engine.rhi.shader.ShaderResourceSlot;
import havoc.client.render.engine.rhi.shader.StorageAccess;
import havoc.client.render.engine.rhi.shader.StorageBinding;
import havoc.client.render.engine.rhi.shader.StorageBufferDescriptor;
import havoc.client.render.engine.rhi.shader.StorageImageBinding;
import havoc.client.render.engine.rhi.shader.Std430StructLayout;
import havoc.client.render.engine.rhi.shader.Std430Type;
import havoc.client.render.engine.rhi.shader.Std430Writer;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Produces a reusable full-resolution disocclusion mask for temporal consumers. */
final class DeferredDisocclusionSource implements AutoCloseable {
    private static final int LOCAL_SIZE = 8;
    private static final Identifier SHADER = id("deferred/disocclusion_mask");
    private static final Std430StructLayout PARAMS_LAYOUT = Std430StructLayout.builder()
            .member("params", Std430Type.VEC4)
            .build();
    private static final ShaderResourceLayout LAYOUT = new ShaderResourceLayout(List.of(
            new ShaderResourceSlot(0, ShaderResourceKind.SAMPLED_TEXTURE, StorageAccess.READ_ONLY),
            new ShaderResourceSlot(1, ShaderResourceKind.SAMPLED_TEXTURE, StorageAccess.READ_ONLY),
            new ShaderResourceSlot(2, ShaderResourceKind.SAMPLED_TEXTURE, StorageAccess.READ_ONLY),
            new ShaderResourceSlot(3, ShaderResourceKind.SAMPLED_TEXTURE, StorageAccess.READ_ONLY),
            new ShaderResourceSlot(4, ShaderResourceKind.STORAGE_IMAGE, StorageAccess.WRITE_ONLY),
            new ShaderResourceSlot(5, ShaderResourceKind.STORAGE_BUFFER, StorageAccess.READ_ONLY)
    ));

    private static final float FINAL_TEMPORAL_DEPTH_THRESHOLD = 0.006f;

    private HavocRhi owner;
    private RhiComputePipeline pipeline;
    private RhiStorageBuffer params;

    void install(ArrayList<DeferredPassSpec> passes) {
        passes.add(DeferredPassSpec.builder("world.temporal.final-disocclusion", DeferredStage.POST_TRANSLUCENCY)
                .priority(100)
                .read(DeferredResource.FINAL_VELOCITY, DeferredResource.FINAL_MOTION_VALIDITY,
                        DeferredResource.FINAL_RESOLVED_DEPTH, DeferredResource.HISTORY_DEPTH)
                .write(DeferredResource.FINAL_DISOCCLUSION_MASK)
                .requires(RhiShaderStage.COMPUTE)
                .when(context -> available(context, DeferredResource.FINAL_VELOCITY,
                        DeferredResource.FINAL_MOTION_VALIDITY, DeferredResource.FINAL_RESOLVED_DEPTH))
                .execute(context -> render(context, DeferredResource.FINAL_VELOCITY,
                        DeferredResource.FINAL_MOTION_VALIDITY, DeferredResource.FINAL_RESOLVED_DEPTH,
                        DeferredResource.FINAL_DISOCCLUSION_MASK, "Havoc final temporal disocclusion mask",
                        FINAL_TEMPORAL_DEPTH_THRESHOLD))
                .build());
    }

    void prepare(HavocRhi rhi) {
        ensureOwner(rhi);
        pipeline();
        params();
    }

    void release(HavocRhi currentOwner) {
        if (owner != null && currentOwner != null && owner != currentOwner) return;
        closeOwned();
        owner = null;
    }

    private boolean available(DeferredPassContext context,
                              DeferredResource velocityResource,
                              DeferredResource validityResource,
                              DeferredResource depthResource) {
        return context.history(DeferredTemporalHistoryId.SCENE).valid()
                && context.isValid(velocityResource)
                && context.isValid(validityResource)
                && context.isValid(depthResource)
                && context.resources().texture(velocityResource) != null
                && context.resources().texture(validityResource) != null
                && context.resources().texture(depthResource) != null
                && context.resources().texture(DeferredResource.HISTORY_DEPTH) != null;
    }

    private void render(DeferredPassContext context,
                        DeferredResource velocityResource,
                        DeferredResource validityResource,
                        DeferredResource depthResource,
                        DeferredResource outputResource,
                        String label,
                        float depthThreshold) {
        ensureOwner(context.rhi());
        GpuTextureView velocity = requireTexture(context, velocityResource);
        GpuTextureView motionValidity = requireTexture(context, validityResource);
        GpuTextureView currentDepth = requireTexture(context, depthResource);
        GpuTextureView historyDepth = requireTexture(context, DeferredResource.HISTORY_DEPTH);
        RhiStorageImage output = requireImage(context, outputResource);

        float pixelScale = 1.0f / Math.max(1.0f, Math.min(output.descriptor().width(), output.descriptor().height()));
        Std430Writer writer = new Std430Writer(PARAMS_LAYOUT, 1)
                .putVec4(0, "params", depthThreshold, pixelScale, 1.5f, 0.0f);
        RhiStorageBuffer paramBuffer = params();
        paramBuffer.upload(writer.buffer(), 0L);

        GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        context.advancedShaders().dispatch(new ComputeDispatchCommand(
                label,
                pipeline(), groups(output.descriptor().width()), groups(output.descriptor().height()), 1,
                List.of(new StorageBinding(5, paramBuffer, 0L, writer.byteSize(), StorageAccess.READ_ONLY)),
                List.of(
                        new SampledTextureBinding(0, velocity, nearest),
                        new SampledTextureBinding(1, currentDepth, nearest),
                        new SampledTextureBinding(2, historyDepth, nearest),
                        new SampledTextureBinding(3, motionValidity, nearest)
                ),
                List.of(new StorageImageBinding(4, output, StorageAccess.WRITE_ONLY))
        ));
    }


    private void ensureOwner(HavocRhi rhi) {
        if (owner == rhi) return;
        closeOwned();
        owner = rhi;
    }

    private RhiComputePipeline pipeline() {
        if (owner == null) throw new IllegalStateException("Disocclusion source has no RHI owner");
        if (pipeline == null) {
            pipeline = owner.advancedShaders().createComputePipeline(new ComputePipelineDescriptor(
                    "havoc-temporal-disocclusion", SHADER, LAYOUT
            ));
        }
        return pipeline;
    }

    private RhiStorageBuffer params() {
        if (owner == null) throw new IllegalStateException("Disocclusion source has no RHI owner");
        if (params == null) {
            params = owner.advancedShaders().createStorageBuffer(new StorageBufferDescriptor(
                    "havoc-temporal-disocclusion-params", PARAMS_LAYOUT, 1, StorageAccess.READ_ONLY, false
            ));
        }
        return params;
    }

    private void closeOwned() {
        if (pipeline != null) {
            try { pipeline.close(); } catch (Throwable ignored) { }
            pipeline = null;
        }
        if (params != null) {
            try { params.close(); } catch (Throwable ignored) { }
            params = null;
        }
    }

    @Override
    public void close() {
        closeOwned();
        owner = null;
    }

    private static GpuTextureView requireTexture(DeferredPassContext context, DeferredResource resource) {
        GpuTextureView value = context.resources().texture(resource);
        if (value == null) throw new IllegalStateException("Deferred texture is not bound: " + resource);
        return value;
    }

    private static RhiStorageImage requireImage(DeferredPassContext context, DeferredResource resource) {
        RhiStorageImage value = context.resources().storageImage(resource);
        if (value == null) throw new IllegalStateException("Deferred storage image is not bound: " + resource);
        return value;
    }

    private static int groups(int extent) {
        return Math.max(1, (Math.max(1, extent) + LOCAL_SIZE - 1) / LOCAL_SIZE);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("havoc", path);
    }
}
