/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.effects.shockwave;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import havoc.client.render.engine.RenderState;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.core.RenderFrameContext;
import havoc.client.render.engine.pipeline.PostProcessRenderPipelines;
import havoc.client.render.engine.postprocess.PostProcessExecutionContext;
import havoc.client.render.engine.postprocess.PostProcessManager;
import havoc.client.render.engine.postprocess.PostProcessPass;
import havoc.client.render.engine.renderer.Renderer3D;
import havoc.client.render.engine.rhi.FullscreenDrawCommand;
import havoc.client.render.engine.rhi.resource.TransientTargetDescriptor;
import havoc.client.render.engine.uniform.impl.JumpShockwaveUniforms;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public final class WorldJumpShockwaves implements PostProcessPass {
    public static final WorldJumpShockwaves INSTANCE = new WorldJumpShockwaves();

    private static final int MAX_SHOCKWAVES = 12;
    private static final String OWNER = "WorldJumpShockwaves";
    private static final String TEMP_A = "havoc-jump-shockwave-a";
    private static final String TEMP_B = "havoc-jump-shockwave-b";

    private final List<Request> pending = new ArrayList<>();
    private long pendingFrameId = Long.MIN_VALUE;

    private WorldJumpShockwaves() {
    }

    public static void submit(Vec3 center,
                              float radius,
                              float thickness,
                              int argb,
                              float strength,
                              boolean depthTest) {
        INSTANCE.enqueue(center, radius, thickness, argb, strength, depthTest);
    }

    public static void clear() {
        INSTANCE.clearPending();
    }

    private synchronized void enqueue(Vec3 center,
                                      float radius,
                                      float thickness,
                                      int argb,
                                      float strength,
                                      boolean depthTest) {
        if (center == null || radius <= 0.001f || thickness <= 0.0001f || strength <= 0.0001f) return;

        float extent = radius + thickness;
        AABB bounds = new AABB(
                center.x - extent, center.y - 0.08, center.z - extent,
                center.x + extent, center.y + 0.08, center.z + extent
        );
        if (!Renderer3D.Culling.isInFrustum(bounds)) return;

        long frameId = currentFrameId();
        if (pendingFrameId != frameId) {
            pending.clear();
            pendingFrameId = frameId;
        }
        if (pending.size() >= MAX_SHOCKWAVES) return;
        pending.add(new Request(
                center,
                Math.max(0.001f, radius),
                Math.max(0.0001f, thickness),
                argb,
                Math.max(0.0f, strength),
                depthTest
        ));
    }

    @Override
    public synchronized boolean isActive() {
        return !pending.isEmpty() && pendingFrameId == currentFrameId();
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Phase getPhase() {
        return Phase.PRE_HAND;
    }

    @Override
    public boolean render(PostProcessExecutionContext execution) {
        if (execution == null || execution.context() == null || execution.source() == null || execution.destination() == null) {
            drain();
            return false;
        }

        List<Request> requests = drain();
        if (requests.isEmpty()) return false;

        GpuTextureView depth = execution.context().preTranslucentDepth();
        boolean depthUsable = depth != null
                && depth.getWidth(0) == execution.context().width()
                && depth.getHeight(0) == execution.context().height();

        List<Request> valid = new ArrayList<>(requests);
        if (valid.isEmpty()) return false;

        Matrix4f view = new Matrix4f().rotation(new Quaternionf(RenderState.cameraRotation).conjugate());
        Matrix4f inverseViewProjection = new Matrix4f(RenderState.worldProjection).mul(view).invert();
        Vec3 camera = RenderState.cameraPos;
        if (camera == null) return false;

        TextureTarget tempA = null;
        TextureTarget tempB = null;
        if (valid.size() > 1) {
            int width = execution.context().width();
            int height = execution.context().height();
            tempA = HavocRenderSystem.resources().frameTransient(
                    TransientTargetDescriptor.frame(TEMP_A, width, height, false, OWNER));
            if (valid.size() > 2) {
                tempB = HavocRenderSystem.resources().frameTransient(
                        TransientTargetDescriptor.frame(TEMP_B, width, height, false, OWNER));
            }
            if (tempA == null || tempA.getColorTextureView() == null) return false;
            if (valid.size() > 2 && (tempB == null || tempB.getColorTextureView() == null)) return false;
        }

        GpuSampler linear = PostProcessManager.getSampler();
        GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        GpuTextureView source = execution.source();
        boolean zeroToOneDepth = execution.rhi().capabilities().zeroToOneDepth();

        for (int i = 0; i < valid.size(); i++) {
            Request request = valid.get(i);
            boolean last = i == valid.size() - 1;
            GpuTextureView destination;
            if (last) {
                destination = execution.destination();
            } else if ((i & 1) == 0) {
                destination = tempA.getColorTextureView();
            } else {
                destination = tempB != null ? tempB.getColorTextureView() : tempA.getColorTextureView();
            }

            Vec3 relative = request.center().subtract(camera);
            boolean useDepth = request.depthTest() && depthUsable;
            JumpShockwaveUniforms.update(
                    inverseViewProjection,
                    relative,
                    request.radius(),
                    request.thickness(),
                    request.strength(),
                    request.argb(),
                    useDepth,
                    zeroToOneDepth
            );

            execution.rhi().drawFullscreen(
                    FullscreenDrawCommand.builder("Havoc jump shockwave")
                            .colorAttachment(destination)
                            .pipeline(PostProcessRenderPipelines.POSTPROCESS_JUMP_SHOCKWAVE)
                            .uniform("JumpShockwave", JumpShockwaveUniforms.get())
                            .sampler("u_Texture", source, linear)
                            .sampler("u_Depth", useDepth ? depth : source, nearest)
                            .build()
            );
            source = destination;
        }
        return true;
    }

    private synchronized List<Request> drain() {
        long frameId = currentFrameId();
        if (pendingFrameId != frameId || pending.isEmpty()) {
            pending.clear();
            pendingFrameId = frameId;
            return List.of();
        }
        List<Request> out = List.copyOf(pending);
        pending.clear();
        return out;
    }

    private synchronized void clearPending() {
        pending.clear();
        pendingFrameId = Long.MIN_VALUE;
    }

    private static long currentFrameId() {
        RenderFrameContext context = HavocRenderSystem.currentContext();
        return context != null ? context.frameId() : HavocRenderSystem.resources().frameId();
    }

    private record Request(Vec3 center,
                           float radius,
                           float thickness,
                           int argb,
                           float strength,
                           boolean depthTest) {
    }
}
