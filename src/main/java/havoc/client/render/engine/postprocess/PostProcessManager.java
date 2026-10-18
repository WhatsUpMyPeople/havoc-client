/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.postprocess;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import havoc.client.render.engine.RenderState;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.pipeline.PostProcessRenderPipelines;
import havoc.client.render.engine.postprocess.graph.PostProcessGraph;
import havoc.client.render.engine.profiler.RenderCostProfiler;
import havoc.client.render.engine.renderer.FullScreenRenderer;
import havoc.client.render.engine.renderer.MeshRenderer;
import havoc.client.render.engine.rhi.FullscreenDrawCommand;
import havoc.client.runtime.RuntimeGate;

import java.util.OptionalDouble;
import java.util.function.Predicate;

public enum PostProcessManager {
    ;
    private static final PostProcessGraph GRAPH = new PostProcessGraph();
    private static GpuSampler sampler;

    static {
        register(TemporalAntiAliasingPass.INSTANCE);
    }

    public static void register(PostProcessPass pass) {
        GRAPH.add(pass);
    }

    public static void renderAll(PostProcessPass.Phase phase, float tickDelta) {
        renderSelected(phase, tickDelta, pass -> true);
    }

    /**
     * Runs an isolated subset of a phase. Preview scenes use this to reuse a feature's
     * real compositor without pulling unrelated world post effects into the preview framebuffer.
     */
    public static void renderSelected(PostProcessPass.Phase phase,
                                      float tickDelta,
                                      Predicate<? super PostProcessPass> selector) {
        if (!RuntimeGate.canRunRender()) return;
        if (selector == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gameRenderer.mainRenderTarget() == null) return;

        FullScreenRenderer.ensureInit();
        ensureSampler();

        var mv = RenderSystem.getModelViewStack();
        Matrix4f previousProjection = MeshRenderer.projection();
        GpuBufferSlice previousProjectionBuffer = RenderSystem.getProjectionMatrixBuffer();
        ProjectionType previousProjectionType = RenderSystem.getProjectionType();
        boolean previousRendering3D = RenderState.rendering3D;
        mv.pushMatrix();
        mv.identity();
        MeshRenderer.setProjection(new Matrix4f().identity());
        RenderState.rendering3D = false;

        try {
            GRAPH.execute(
                    phase,
                    tickDelta,
                    HavocRenderSystem.rhi(),
                    PostProcessManager::copy,
                    selector
            );
        } finally {
            MeshRenderer.setProjection(previousProjection);
            if (previousProjectionBuffer != null && previousProjectionType != null) {
                RenderSystem.setProjectionMatrix(previousProjectionBuffer, previousProjectionType);
            }
            RenderState.rendering3D = previousRendering3D;
            mv.popMatrix();
        }
    }

    private static void ensureSampler() {
        if (sampler != null) return;
        sampler = RenderSystem.getDevice().createSampler(
                AddressMode.CLAMP_TO_EDGE,
                AddressMode.CLAMP_TO_EDGE,
                FilterMode.LINEAR,
                FilterMode.LINEAR,
                1,
                OptionalDouble.empty()
        );
    }

    public static GpuSampler getSampler() {
        ensureSampler();
        return sampler;
    }

    public static void copy(GpuTextureView src, GpuTextureView dst) {
        try (RenderCostProfiler.Scope ignored = RenderCostProfiler.postPass("copy")) {
            if (src == null || dst == null || src == dst) return;
            if (HavocRenderSystem.rhi().textureBlitter().copyFast(src, dst)) {
                return;
            }

            ensureSampler();
            HavocRenderSystem.rhi().drawFullscreen(
                    FullscreenDrawCommand.builder("Havoc PostProcess Copy")
                            .colorAttachment(dst)
                            .pipeline(PostProcessRenderPipelines.POSTPROCESS_COPY)
                            .sampler("u_Texture", src, sampler)
                            .build()
            );
            HavocRenderSystem.rhi().stats().textureShaderCopy();
        }
    }

    public static void releaseBackendResources(havoc.client.render.engine.rhi.HavocRhi owner) {
        GRAPH.releaseBackendResources(owner);
        invalidateBackendObjects();
    }

    /** Backend-generation boundary for Blaze3D objects that are not owned by the graph itself. */
    public static void invalidateBackendObjects() {
        GpuSampler previous = sampler;
        sampler = null;
        if (previous != null) {
            try {
                previous.close();
            } catch (Throwable ignored) {
                // The owning backend may already be tearing down. Never keep the stale handle.
            }
        }
    }

    public static void shutdownForRuntime() {
        GRAPH.close();
        invalidateBackendObjects();
    }
}
