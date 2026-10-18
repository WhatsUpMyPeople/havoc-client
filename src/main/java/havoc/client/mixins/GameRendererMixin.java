/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import havoc.client.features.gui.preview.VisualPreviewRuntime;
import havoc.client.features.module.modules.visuals.*;
import havoc.client.render.engine.postprocess.PostProcessPass;
import havoc.client.render.engine.postprocess.TemporalAntiAliasingPass;
import havoc.client.util.screen.ClientScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import havoc.client.features.gui.clickgui.ClickGuiRenderer;
import havoc.client.features.gui.hud.nondraggable.impl.BetterButtons;
import havoc.client.features.gui.hud.nondraggable.impl.BetterTooltips;
import havoc.client.addon.AddonRenderPipelineManager;
import havoc.client.api.v0.render.HavocRenderStage;
import havoc.client.features.module.ModuleManager;
import havoc.client.features.module.Modules;
import havoc.client.features.module.WorldPhase;
import havoc.client.features.module.modules.visuals.*;
import havoc.client.mixins.accessors.GameRendererAccessor;
import havoc.client.mixins.accessors.LocalPlayerAccessor;
import havoc.client.render.engine.RenderState;
import havoc.client.render.engine.asset.gltf.render.ImportedAssetCompatibilityRenderer;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.core.HavocWorldMatrices;
import havoc.client.render.engine.temporal.TemporalJitterSequence;
import havoc.client.render.engine.core.RenderPhase;
import havoc.client.render.engine.core.RenderPhaseScope;
import havoc.client.render.engine.depth.WorldSceneDepth;
import havoc.client.render.engine.debug.UiClipDebugScene;
import havoc.client.render.engine.msaa.MsaaWorldTarget;
import havoc.client.config.MainConfig;
import havoc.client.render.engine.pipeline.WorldRenderPipelines;
import havoc.client.render.engine.postprocess.PostProcessManager;
import havoc.client.render.engine.postprocess.MenuBackgroundRenderer;
import havoc.client.render.engine.profiler.ProfilerPhase;
import havoc.client.render.engine.profiler.RenderProfiler3D;
import havoc.client.render.engine.profiler.TracyGpuProfiler;
import havoc.client.render.engine.renderer.MeshRenderer;
import havoc.client.features.module.modules.visuals.MotionBlur;
import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.engine.renderer.ui.UiBlurResources;
import havoc.client.render.engine.renderer.Renderer3D;
import havoc.client.render.engine.text.TextRenderer;
import havoc.client.render.iris.IrisHavocFrameHooks;
import havoc.client.render.iris.IrisFinalizedSceneRenderer;
import havoc.client.render.iris.IrisCompatibilityGuards;
import havoc.client.render.iris.IrisRuntime;
import havoc.client.util.logging.DebugLog;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements IrisFinalizedSceneRenderer {

    @Unique
    private static final boolean HAVOC_DEBUG_PRIMITIVE = false;
    @Unique
    private static final boolean HAVOC_DEBUG_CLIP_SPACE = false;
    @Unique
    private static final long HAVOC_DEBUG_INTERVAL_MS = 2000L;
    @Unique
    private static long HAVOC_DEBUG_LAST_MS;
    @Unique
    private final PoseStack havoc$matrices = new PoseStack();
    @Unique
    private final PoseStack havoc$postMatrices = new PoseStack();
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private Camera mainCamera;
    @Shadow
    @Final
    private GameRenderState gameRenderState;
    @Shadow
    private float spinningEffectTime;
    @Shadow
    private float spinningEffectSpeed;
    @Unique
    private Renderer3D havoc$renderer;
    @Unique
    private Renderer3D havoc$depthRenderer;
    @Unique
    private Renderer3D havoc$postRenderer;
    @Unique
    private Renderer3D havoc$postDepthRenderer;

    @Unique
    private static void havoc$drawDebugCube(Renderer3D rdr,
                                                double x1, double y1, double z1,
                                                double x2, double y2, double z2) {
        rdr.line(x1, y1, z1, x2, y1, z1, 255, 0, 0, 255);
        rdr.line(x1, y2, z1, x2, y2, z1, 255, 0, 0, 255);
        rdr.line(x1, y1, z2, x2, y1, z2, 255, 0, 0, 255);
        rdr.line(x1, y2, z2, x2, y2, z2, 255, 0, 0, 255);

        rdr.line(x1, y1, z1, x1, y2, z1, 0, 255, 0, 255);
        rdr.line(x2, y1, z1, x2, y2, z1, 0, 255, 0, 255);
        rdr.line(x1, y1, z2, x1, y2, z2, 0, 255, 0, 255);
        rdr.line(x2, y1, z2, x2, y2, z2, 0, 255, 0, 255);

        rdr.line(x1, y1, z1, x1, y1, z2, 0, 0, 255, 255);
        rdr.line(x2, y1, z1, x2, y1, z2, 0, 0, 255, 255);
        rdr.line(x1, y2, z1, x1, y2, z2, 0, 0, 255, 255);
        rdr.line(x2, y2, z1, x2, y2, z2, 0, 0, 255, 255);
    }

    @Unique
    private static boolean isWorldTranslucent(FogType type) {
        return type == FogType.WATER
                || type == FogType.LAVA
                || type == FogType.POWDER_SNOW;
    }

    @Shadow
    protected abstract void bobView(CameraRenderState cameraRenderState, PoseStack matrices);

    @Shadow
    protected abstract void bobHurt(CameraRenderState cameraRenderState, PoseStack matrices);

    @Unique
    private static boolean havoc$needsResolvedMainDepth() {
        if (MotionBlur.isActiveStatic() || MainConfig.get().isTaaRuntimeActive()) return true;
        ReimaginedVisual module = Modules.get(ReimaginedVisual.class);
        if (module == null) return false;
        if (module.needsResolvedMainDepthCapture()) return true;
        return MainConfig.get().getMsaa3dSamples() > 1 && module.isActive();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void havoc$skipTickWithoutPlayer(CallbackInfo ci) {
        if (minecraft != null && minecraft.level != null && minecraft.player == null) {
            ci.cancel();
        }
    }

    @Inject(method = "extract(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void havoc$beginDeferred2DExtract(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        Renderer2D.beginDeferredExtractFrame();
    }

    @Inject(method = "extract(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("TAIL"))
    private void havoc$endDeferred2DExtract(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        Renderer2D.endDeferredExtractFrame();
    }

    @Redirect(
            method = "renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"
            )
    )
    private void havoc$viewModel$replaceHandBobbing(GameRenderer instance,
                                                        CameraRenderState cameraRenderState,
                                                        PoseStack matrices,
                                                        CameraRenderState originalCameraRenderState,
                                                        float tickDelta,
                                                        Matrix4fc positionMatrix) {
        ViewModel viewModel = Modules.get(ViewModel.class);
        if (viewModel != null && viewModel.smoothRunLoweringEnabled()) {
            if (minecraft != null && minecraft.getCameraEntity() instanceof AbstractClientPlayer player) {
                float movement = player.avatarState().getInterpolatedBob(tickDelta);
                float yOffset = -Mth.clamp(movement, 0.0f, 1.0f) * viewModel.getSmoothRunLoweringAmount();
                if (Math.abs(yOffset) > 0.0001f) {
                    matrices.translate(0.0f, yOffset, 0.0f);
                }
            }
            return;
        }

        ((GameRendererAccessor) instance).invokeBobView(cameraRenderState, matrices);
    }

    @Inject(method = "extractCamera", at = @At("TAIL"))
    private void havoc$modifyAspect(DeltaTracker tickCounter, float tickDelta, float fov, CallbackInfo ci) {

        AspectRatio ar = Modules.get(AspectRatio.class);
        if (ar == null || !ar.isEnabled()) {
            return;
        }

        float mul = ar.getMultiplier();
        if (mul == 1.0f) {
            return;
        }

        Matrix4f m = gameRenderState.levelRenderState.cameraRenderState.projectionMatrix;
        if (m == null) return;

        // horizontal aspect stretch
        m.m00(m.m00() * mul);
    }

    @Inject(method = "extractCamera", at = @At("TAIL"))
    private void havoc$captureProjectionFor2DOverlays(DeltaTracker tickCounter, float tickDelta, float fov, CallbackInfo ci) {
        if (gameRenderState == null || gameRenderState.levelRenderState == null) return;
        CameraRenderState cameraRenderState = gameRenderState.levelRenderState.cameraRenderState;
        if (cameraRenderState == null || cameraRenderState.viewRotationMatrix == null || cameraRenderState.projectionMatrix == null) return;

        PoseStack effectMatrices = new PoseStack();
        Matrix4f renderProjection = havoc$buildWorldProjection(fov, effectMatrices, tickDelta);
        AspectRatio.applyToProjection(renderProjection);
        Matrix4f screenProjection = new Matrix4f(cameraRenderState.projectionMatrix);
        HavocWorldMatrices.capture(
                cameraRenderState.viewRotationMatrix,
                renderProjection,
                screenProjection,
                cameraRenderState.pos != null ? cameraRenderState.pos : mainCamera.position()
        );
    }

    @Inject(method = "mainRenderTarget", at = @At("HEAD"), cancellable = true)
    private void havoc$overrideWorldFramebuffer(CallbackInfoReturnable<com.mojang.blaze3d.pipeline.RenderTarget> cir) {
        com.mojang.blaze3d.pipeline.RenderTarget msaa = MsaaWorldTarget.getFramebufferOverride();
        if (msaa != null) {
            cir.setReturnValue(msaa);
        }
    }

    @Unique
    private void havoc$renderWorldEngine(DeltaTracker tickCounter) {
        try (ProfilerPhase.Scope phaseScope = ProfilerPhase.scope("3d:world");
             TracyGpuProfiler.Scope gpuScope = TracyGpuProfiler.beginZone("3d:world")) {
            float tickDelta = tickCounter.getGameTimeDeltaPartialTick(true);
            float fov = mainCamera.getFov();
            Matrix4f capturedPosition = HavocWorldMatrices.positionMatrix();
            Matrix4f capturedProjection = HavocWorldMatrices.renderProjectionMatrix();
            boolean useCapturedMatrices = capturedPosition != null && capturedProjection != null;
            Matrix4f position = useCapturedMatrices
                    ? capturedPosition
                    : new Matrix4f().rotation(mainCamera.rotation().conjugate(new org.joml.Quaternionf()));
            RenderState.rendering3D = true;
            Vec3 capturedCameraPos = HavocWorldMatrices.cameraPosition();
            RenderState.cameraPos = capturedCameraPos != null ? capturedCameraPos : mainCamera.position();
            RenderState.cameraRotation.set(mainCamera.rotation());
            RenderState.cameraYaw = mainCamera.yRot();
            RenderState.cameraPitch = mainCamera.xRot();
            RenderState.cameraLook = Vec3.directionFromRotation(RenderState.cameraPitch, RenderState.cameraYaw).normalize();
            RenderState.cameraSubmersion = mainCamera.getFluidInCamera();
            RenderState.worldTranslucent = isWorldTranslucent(RenderState.cameraSubmersion);
            RenderState.frustum = mainCamera != null
                    ? mainCamera.getCapturedFrustum()
                    : null;

            if (HAVOC_DEBUG_CLIP_SPACE) {
                var mv = com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();
                mv.pushMatrix();
                mv.identity();

                MeshRenderer.setProjection(new Matrix4f().identity());
                RenderState.rendering3D = false;

                if (havoc$renderer == null) {
                    havoc$renderer = new Renderer3D(
                            WorldRenderPipelines.WORLD_COLORED_LINES,
                            WorldRenderPipelines.WORLD_COLORED
                    );
                }

                havoc$renderer.begin();

                // Clip-space square + diagonals (NDC)
                havoc$renderer.line(-0.8, -0.8, 0.0, 0.8, -0.8, 0.0, 255, 0, 0, 255);
                havoc$renderer.line(0.8, -0.8, 0.0, 0.8, 0.8, 0.0, 0, 255, 0, 255);
                havoc$renderer.line(0.8, 0.8, 0.0, -0.8, 0.8, 0.0, 0, 0, 255, 255);
                havoc$renderer.line(-0.8, 0.8, 0.0, -0.8, -0.8, 0.0, 255, 255, 0, 255);
                havoc$renderer.line(-0.8, -0.8, 0.0, 0.8, 0.8, 0.0, 255, 0, 255, 255);
                havoc$renderer.line(0.8, -0.8, 0.0, -0.8, 0.8, 0.0, 0, 255, 255, 255);

                havoc$renderer.render(new PoseStack());

                mv.popMatrix();
                RenderState.rendering3D = false;
                return;
            }

            if (havoc$renderer == null) {
                havoc$renderer = new Renderer3D(
                        WorldRenderPipelines.WORLD_COLORED_LINES,
                        WorldRenderPipelines.WORLD_COLORED
                );
            }

            if (havoc$depthRenderer == null) {
                havoc$depthRenderer = new Renderer3D(
                        WorldRenderPipelines.WORLD_COLORED_LINES_DEPTH,
                        WorldRenderPipelines.WORLD_COLORED_DEPTH
                );
            }

            var mv = com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();
            mv.pushMatrix();
            if (IrisHavocFrameHooks.isRenderingAfterIrisFinalization()) {
                mv.identity();
            }
            mv.mul(position);
            havoc$matrices.pushPose();
            Matrix4f projection = useCapturedMatrices
                    ? capturedProjection
                    : havoc$buildWorldProjection(fov, havoc$matrices, tickDelta);
            MeshRenderer.setProjection(projection);
            HavocRenderSystem.beginFrame(
                    tickDelta,
                    tickCounter.getGameTimeDeltaTicks(),
                    tickCounter.getRealtimeDeltaTicks(),
                    projection,
                    new Matrix4f(mv)
            );
            try (RenderPhaseScope havoc$beforeTranslucentPhase = HavocRenderSystem.phase(RenderPhase.WORLD_BEFORE_TRANSLUCENT)) {
                RenderState.worldProjection.set(projection);
                RenderState.frustum = new Frustum(position, projection);
                RenderState.frustum.prepare(RenderState.cameraPos.x, RenderState.cameraPos.y, RenderState.cameraPos.z);

                if (!useCapturedMatrices) {
                    Matrix4f inverseBob = new Matrix4f(havoc$matrices.last().pose()).invert();
                    mv.mul(inverseBob);
                }

                havoc$renderer.begin();
                havoc$depthRenderer.begin();

                if (HAVOC_DEBUG_PRIMITIVE) {
                    Vec3 camPos = mainCamera.position();
                    float yaw = mainCamera.yRot() * ((float) Math.PI / 180.0f);
                    float pitch = mainCamera.xRot() * ((float) Math.PI / 180.0f);
                    Vec3 forward = new Vec3(
                            -Mth.sin(yaw) * Mth.cos(pitch),
                            -Mth.sin(pitch),
                            Mth.cos(yaw) * Mth.cos(pitch)
                    );

                    Vec3 center = camPos.add(forward.scale(3.0));
                    double r = 0.5;
                    double x1 = center.x - r, x2 = center.x + r;
                    double y1 = center.y - r, y2 = center.y + r;
                    double z1 = center.z - r, z2 = center.z + r;
                    double ox1 = -1.0, oy1 = -1.0, oz1 = -1.0;
                    double ox2 = 1.0, oy2 = 1.0, oz2 = 1.0;

                    // Wire cube (no depth) and depth variant
                    Renderer3D[] targets = new Renderer3D[]{havoc$renderer, havoc$depthRenderer};
                    for (Renderer3D rdr : targets) {
                        havoc$drawDebugCube(rdr, x1, y1, z1, x2, y2, z2);
                        havoc$drawDebugCube(rdr, ox1, oy1, oz1, ox2, oy2, oz2);
                    }

                    long now = System.currentTimeMillis();
                    if (now - HAVOC_DEBUG_LAST_MS >= HAVOC_DEBUG_INTERVAL_MS) {
                        HAVOC_DEBUG_LAST_MS = now;
                        Matrix4f mvMat = com.mojang.blaze3d.systems.RenderSystem.getModelViewMatrixCopy();
                        DebugLog.renderThread(
                                "[Havoc] Debug diag: cam=" + camPos
                                        + " proj=[" + projection.m00() + "," + projection.m11() + "," + projection.m22() + "," + projection.m33() + "]"
                                        + " mv=[" + mvMat.m30() + "," + mvMat.m31() + "," + mvMat.m32() + "]"
                        );
                    }
                }

                try (ProfilerPhase.Scope phase = ProfilerPhase.scope("3d:modules_before_translucent");
                     RenderProfiler3D.Section ignored = RenderProfiler3D.section("modules_before_translucent")) {
                    ModuleManager.renderWorldEngine(WorldPhase.BEFORE_TRANSLUCENT, havoc$renderer, havoc$depthRenderer, tickDelta);
                    AddonRenderPipelineManager.render3D(HavocRenderStage.WORLD_BEFORE_TRANSLUCENT,
                            WorldPhase.BEFORE_TRANSLUCENT, havoc$renderer, havoc$depthRenderer, havoc$matrices, tickDelta);
                }

                try (ProfilerPhase.Scope phase = ProfilerPhase.scope("3d:flush_before_translucent");
                     TracyGpuProfiler.Scope gpuFlush = TracyGpuProfiler.beginZone("3d:flush_before_translucent");
                     RenderProfiler3D.Section ignored = RenderProfiler3D.section("flush_before_translucent")) {
                    havoc$renderer.render(havoc$matrices);
                    havoc$depthRenderer.render(havoc$matrices);
                }
            }

            // Start a new batch for END_MAIN so overlays render above grass.
            havoc$renderer.begin();
            havoc$depthRenderer.begin();

            try (RenderPhaseScope havoc$afterTranslucentPhase = HavocRenderSystem.phase(RenderPhase.WORLD_AFTER_TRANSLUCENT)) {
                try (ProfilerPhase.Scope phase = ProfilerPhase.scope("3d:modules_end_main");
                     RenderProfiler3D.Section ignored = RenderProfiler3D.section("modules_end_main")) {
                    ModuleManager.renderWorldEngine(WorldPhase.END_MAIN, havoc$renderer, havoc$depthRenderer, tickDelta);
                    AddonRenderPipelineManager.render3D(HavocRenderStage.WORLD_END_MAIN,
                            WorldPhase.END_MAIN, havoc$renderer, havoc$depthRenderer, havoc$matrices, tickDelta);

                    // Keep world billboards in the same END_MAIN world stage, but record them
                    // after ordinary translucent effects/particles. Billboard primitives use
                    // DepthMode.NONE, so the final ordering is deterministic without moving
                    // them into a HUD/post-process layer.
                    ModuleManager.renderWorldEngine(WorldPhase.END_MAIN_BILLBOARD,
                            havoc$renderer, havoc$depthRenderer, tickDelta);
                }

                try (ProfilerPhase.Scope phase = ProfilerPhase.scope("3d:flush_end_main");
                     TracyGpuProfiler.Scope gpuFlush = TracyGpuProfiler.beginZone("3d:flush_end_main");
                     RenderProfiler3D.Section ignored = RenderProfiler3D.section("flush_end_main")) {
                    havoc$renderer.render(havoc$matrices);
                    havoc$depthRenderer.render(havoc$matrices);
                }
            }
            havoc$matrices.popPose();
            mv.popMatrix();
            RenderState.rendering3D = false;
        }
    }


    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void havoc$beginWorldMsaa(DeltaTracker tickCounter, CallbackInfo ci) {
        HavocWorldMatrices.reset();
        if (ClickGuiRenderer.suppressesBackgroundRendering() || VisualPreviewRuntime.isActive()) return;
        IrisHavocFrameHooks.beginRenderLevel(tickCounter);
        int samples = MainConfig.get().getMsaa3dSamples();
        MsaaWorldTarget.begin(minecraft, samples, havoc$needsResolvedMainDepth());
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void havoc$endIrisFrameHooks(DeltaTracker tickCounter, CallbackInfo ci) {
        if (ClickGuiRenderer.suppressesBackgroundRendering() || VisualPreviewRuntime.isActive()) return;
        IrisHavocFrameHooks.endRenderLevel(tickCounter);
    }

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V"
            )
    )
    private void havoc$skipWorldForVisualPreview(LevelRenderer renderer,
                                                      GraphicsResourceAllocator resources,
                                                      DeltaTracker tickCounter,
                                                      boolean renderBlockOutline,
                                                      CameraRenderState camera,
                                                      Matrix4fc positionMatrix,
                                                      GpuBufferSlice fog,
                                                      Vector4f clearColor,
                                                      boolean renderSky) {
        if (ClickGuiRenderer.suppressesBackgroundRendering() || VisualPreviewRuntime.isActive()) return;
        renderer.render(resources, tickCounter, renderBlockOutline, camera, positionMatrix, fog, clearColor, renderSky);
    }

    @WrapOperation(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
            )
    )
    private GpuBufferSlice havoc$captureFullWorldMatrices(ProjectionMatrixBuffer instance,
                                                               Matrix4f renderProjectionMatrix,
                                                               Operation<GpuBufferSlice> original) {
        if (renderProjectionMatrix != null
                && gameRenderState != null
                && gameRenderState.levelRenderState != null
                && gameRenderState.levelRenderState.cameraRenderState != null
                && gameRenderState.levelRenderState.cameraRenderState.viewRotationMatrix != null
                && gameRenderState.levelRenderState.cameraRenderState.projectionMatrix != null) {
            CameraRenderState cameraRenderState = gameRenderState.levelRenderState.cameraRenderState;
            Vec3 cameraPosition = cameraRenderState.pos != null ? cameraRenderState.pos : mainCamera.position();

            // Open/refresh the Havoc frame first so the jitter sample is keyed to the canonical
            // frame id. The second beginFrame call below only refreshes CameraContext; it never
            // advances frameId while the frame is already open.
            DeltaTracker tickCounter = minecraft.getDeltaTracker();
            float tickProgress = tickCounter != null
                    ? tickCounter.getGameTimeDeltaPartialTick(true) : RenderState.tickProgress;
            float frameDeltaTicks = tickCounter != null
                    ? tickCounter.getGameTimeDeltaTicks() : RenderState.frameDeltaTicks;
            float fixedDeltaTicks = tickCounter != null
                    ? tickCounter.getRealtimeDeltaTicks() : RenderState.fixedDeltaTicks;
            var frame = HavocRenderSystem.beginFrame(
                    tickProgress,
                    frameDeltaTicks,
                    fixedDeltaTicks,
                    renderProjectionMatrix,
                    new Matrix4f(cameraRenderState.viewRotationMatrix)
            );

            Matrix4f unjitteredProjection = new Matrix4f(renderProjectionMatrix);
            org.joml.Vector2f jitter = new org.joml.Vector2f();
            Matrix4f effectiveProjection = unjitteredProjection;
            if (TemporalAntiAliasingPass.shouldJitter()) {
                int width = minecraft != null && minecraft.gameRenderer.mainRenderTarget() != null
                        ? Math.max(1, minecraft.gameRenderer.mainRenderTarget().width) : 1;
                int height = minecraft != null && minecraft.gameRenderer.mainRenderTarget() != null
                        ? Math.max(1, minecraft.gameRenderer.mainRenderTarget().height) : 1;
                jitter = TemporalJitterSequence.sample(frame.frameId());
                effectiveProjection = TemporalJitterSequence.apply(unjitteredProjection, jitter, width, height);
            }

            HavocWorldMatrices.capture(
                    cameraRenderState.viewRotationMatrix,
                    effectiveProjection,
                    unjitteredProjection,
                    cameraRenderState.projectionMatrix,
                    jitter,
                    cameraPosition
            );
            HavocRenderSystem.refreshPrimarySceneView();
            return original.call(instance, effectiveProjection);
        }
        return original.call(instance, renderProjectionMatrix);
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    args = "ldc=hand",
                    shift = At.Shift.AFTER
            )
    )
    private void havoc$postProcess(DeltaTracker tickCounter, CallbackInfo ci) {
        if (ClickGuiRenderer.suppressesBackgroundRendering() || VisualPreviewRuntime.isActive()) return;
        if (IrisRuntime.isShaderpackRendererActive()
                && IrisCompatibilityGuards.deferIrisFinalizationForSecondHandScene()) {
            return;
        }
        havoc$renderPreHandPostProcess(tickCounter);
    }

    @Unique
    private void havoc$renderPreHandPostProcess(DeltaTracker tickCounter) {
        try (ProfilerPhase.Scope profilerScope = ProfilerPhase.scope("3d:post_pre_hand");
            TracyGpuProfiler.Scope gpuScope = TracyGpuProfiler.beginZone("3d:post_pre_hand")) {
            if (minecraft != null) {
                try (ProfilerPhase.Scope ignored = ProfilerPhase.scope("3d:pre_hand_resolve");
                     TracyGpuProfiler.Scope ignoredGpu = TracyGpuProfiler.beginZone("3d:pre_hand:resolve")) {
                    boolean needsResolvedDepth = !IrisRuntime.isShaderpackRendererActive()
                            && havoc$needsResolvedMainDepth();
                    boolean capturedMsaaDepth = needsResolvedDepth
                            && MsaaWorldTarget.isActive()
                            && WorldSceneDepth.captureResolvedMain(MsaaWorldTarget.getMsaaFramebuffer());
                    MsaaWorldTarget.resolveToMain(minecraft);
                    if (needsResolvedDepth && !capturedMsaaDepth) {
                        WorldSceneDepth.captureResolvedMain(minecraft.gameRenderer.mainRenderTarget());
                    }
                }
            }
            try (RenderPhaseScope havoc$postPreHandPhase = HavocRenderSystem.phase(RenderPhase.WORLD_POST_PRE_HAND)) {
                float postTickDelta = tickCounter.getGameTimeDeltaPartialTick(true);
                if (havoc$prepareWorldPostMaskState()) {
                    ModuleManager.prepareWorldPostProcess(postTickDelta);
                }
                PostProcessManager.renderAll(PostProcessPass.Phase.PRE_HAND,
                        postTickDelta);
                havoc$renderHavocWorldAfterPreHandPostProcess(tickCounter);
            }
        }
    }

    @Unique
    private boolean havoc$prepareWorldPostMaskState() {
        Matrix4f capturedPosition = HavocWorldMatrices.positionMatrix();
        Matrix4f capturedProjection = HavocWorldMatrices.renderProjectionMatrix();
        if (capturedProjection == null || mainCamera == null) return false;

        Vec3 capturedCameraPos = HavocWorldMatrices.cameraPosition();
        Vec3 cameraPos = capturedCameraPos != null ? capturedCameraPos : mainCamera.position();
        RenderState.cameraPos = cameraPos;
        RenderState.cameraRotation.set(mainCamera.rotation());
        RenderState.cameraYaw = mainCamera.yRot();
        RenderState.cameraPitch = mainCamera.xRot();
        RenderState.worldProjection.set(capturedProjection);

        Matrix4f position = capturedPosition != null
                ? capturedPosition
                : new Matrix4f().rotation(mainCamera.rotation().conjugate(new org.joml.Quaternionf()));
        RenderState.frustum = new Frustum(position, capturedProjection);
        RenderState.frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
        return true;
    }

    @Unique
    private void havoc$renderHavocWorldAfterPreHandPostProcess(DeltaTracker tickCounter) {
        if (minecraft == null) {
            havoc$renderWorldEngine(tickCounter);
            havoc$renderPostProcessWorld(tickCounter);
            return;
        }

        try (ProfilerPhase.Scope ignored = ProfilerPhase.scope("3d:world_engine_target");
             TracyGpuProfiler.Scope ignoredGpu = TracyGpuProfiler.beginZone("3d:world_engine_target")) {
            int samples = MainConfig.get().getMsaa3dSamples();
            if (ModuleManager.needsEntityOnlyDepth()) {
                samples = 1;
            }
            MsaaWorldTarget.begin(minecraft, samples, false, true);
            try {
                havoc$renderWorldEngine(tickCounter);
                havoc$renderPostProcessWorld(tickCounter);
            } finally {
                MsaaWorldTarget.resolveToMain(minecraft);
            }
        }
    }

    @Override
    public void havoc$renderAfterIrisFinalization(DeltaTracker tickCounter) {
        if (ClickGuiRenderer.suppressesBackgroundRendering() || VisualPreviewRuntime.isActive()) return;
        havoc$renderPreHandPostProcess(tickCounter);
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures(Lnet/minecraft/client/renderer/SubmitNodeStorage;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void havoc$postProcessAfterHand(DeltaTracker tickCounter, CallbackInfo ci) {
        if (ClickGuiRenderer.suppressesBackgroundRendering() || VisualPreviewRuntime.isActive()) return;
        try (ProfilerPhase.Scope profilerScope = ProfilerPhase.scope("3d:post_after_hand");
             TracyGpuProfiler.Scope gpuScope = TracyGpuProfiler.beginZone("3d:post_after_hand")) {
            if (minecraft != null) {
                MsaaWorldTarget.resolveToMain(minecraft);
            }
            try (RenderPhaseScope havoc$postHandPhase = HavocRenderSystem.phase(RenderPhase.WORLD_POST_HAND)) {
                PostProcessManager.renderAll(PostProcessPass.Phase.POST_HAND,
                        tickCounter.getGameTimeDeltaPartialTick(true));
            }
        }
    }

    @Unique
    private void havoc$renderPostProcessWorld(DeltaTracker tickCounter) {
        try (ProfilerPhase.Scope phaseScope = ProfilerPhase.scope("3d:world_post");
             TracyGpuProfiler.Scope gpuScope = TracyGpuProfiler.beginZone("3d:world_post")) {
            float tickDelta = tickCounter.getGameTimeDeltaPartialTick(true);
            float fov = mainCamera.getFov();
            Matrix4f capturedPosition = HavocWorldMatrices.positionMatrix();
            Matrix4f capturedProjection = HavocWorldMatrices.renderProjectionMatrix();
            boolean useCapturedMatrices = capturedPosition != null && capturedProjection != null;
            Matrix4f position = useCapturedMatrices
                    ? capturedPosition
                    : new Matrix4f().rotation(mainCamera.rotation().conjugate(new org.joml.Quaternionf()));

            RenderState.rendering3D = true;
            Vec3 capturedCameraPos = HavocWorldMatrices.cameraPosition();
            RenderState.cameraPos = capturedCameraPos != null ? capturedCameraPos : mainCamera.position();
            RenderState.cameraRotation.set(mainCamera.rotation());
            RenderState.cameraYaw = mainCamera.yRot();
            RenderState.cameraPitch = mainCamera.xRot();

            if (havoc$postRenderer == null) {
                havoc$postRenderer = new Renderer3D(
                        WorldRenderPipelines.WORLD_COLORED_LINES,
                        WorldRenderPipelines.WORLD_COLORED
                );
            }

            if (havoc$postDepthRenderer == null) {
                havoc$postDepthRenderer = new Renderer3D(
                        WorldRenderPipelines.WORLD_COLORED_LINES_DEPTH,
                        WorldRenderPipelines.WORLD_COLORED_DEPTH
                );
            }

            var mv = com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();
            mv.pushMatrix();
            if (IrisHavocFrameHooks.isRenderingAfterIrisFinalization()) {
                mv.identity();
            }
            mv.mul(position);
            havoc$postMatrices.pushPose();
            Matrix4f projection = useCapturedMatrices
                    ? capturedProjection
                    : havoc$buildWorldProjection(fov, havoc$postMatrices, tickDelta);
            MeshRenderer.setProjection(projection);
            HavocRenderSystem.beginFrame(
                    tickDelta,
                    tickCounter.getGameTimeDeltaTicks(),
                    tickCounter.getRealtimeDeltaTicks(),
                    projection,
                    new Matrix4f(mv)
            );
            try (RenderPhaseScope havoc$postWorldPhase = HavocRenderSystem.phase(RenderPhase.WORLD_POST_PRE_HAND)) {
                RenderState.worldProjection.set(projection);
                RenderState.frustum = new Frustum(position, projection);
                RenderState.frustum.prepare(RenderState.cameraPos.x, RenderState.cameraPos.y, RenderState.cameraPos.z);

                if (!useCapturedMatrices) {
                    Matrix4f inverseBob = new Matrix4f(havoc$postMatrices.last().pose()).invert();
                    mv.mul(inverseBob);
                }

                havoc$postRenderer.begin();
                havoc$postDepthRenderer.begin();

                try (ProfilerPhase.Scope phase = ProfilerPhase.scope("3d:post_modules");
                     RenderProfiler3D.Section ignored = RenderProfiler3D.section("modules")) {
                    ModuleManager.renderWorldEngine(WorldPhase.AFTER_POST_PROCESS, havoc$postRenderer, havoc$postDepthRenderer, tickDelta);
                    AddonRenderPipelineManager.render3D(HavocRenderStage.WORLD_POST_PROCESS,
                            WorldPhase.AFTER_POST_PROCESS, havoc$postRenderer, havoc$postDepthRenderer, havoc$postMatrices, tickDelta);
                }

                try (ProfilerPhase.Scope phase = ProfilerPhase.scope("3d:post_flush");
                     TracyGpuProfiler.Scope gpuFlush = TracyGpuProfiler.beginZone("3d:post_flush");
                     RenderProfiler3D.Section ignored = RenderProfiler3D.section("flush")) {
                    havoc$postRenderer.render(havoc$postMatrices);
                    havoc$postDepthRenderer.render(havoc$postMatrices);
                }
            }
            havoc$postMatrices.popPose();
            mv.popMatrix();
            RenderState.rendering3D = false;
        }
    }

    @Unique
    private Matrix4f havoc$buildWorldProjection(float fov, PoseStack viewEffectMatrices, float tickDelta) {
        CameraRenderState cameraRenderState = new CameraRenderState();
        mainCamera.extractRenderState(cameraRenderState, fov);
        Matrix4f projection = new Matrix4f(cameraRenderState.projectionMatrix);
        havoc$applyViewEffectMatrices(viewEffectMatrices, tickDelta);
        projection.mul(viewEffectMatrices.last().pose());
        havoc$applyNauseaProjection(projection, tickDelta);
        return projection;
    }

    @Unique
    private void havoc$applyViewEffectMatrices(PoseStack matrices, float cameraTickProgress) {
        NoRender noRender = Modules.get(NoRender.class);
        CameraRenderState cameraRenderState = new CameraRenderState();
        mainCamera.extractRenderState(cameraRenderState, mainCamera.getFov());
        if (noRender == null || !noRender.off("camera_shake")) {
            bobHurt(cameraRenderState, matrices);
        }

        boolean freecamEnabled = Modules.get(Freecam.class) != null && Modules.get(Freecam.class).isEnabled();
        if (minecraft.options.bobView().get()
                && !freecamEnabled
                && (noRender == null || !noRender.off("view_bob"))) {
            bobView(cameraRenderState, matrices);
        }
    }

    @Unique
    private void havoc$applyNauseaProjection(Matrix4f projection, float tickDelta) {
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        float distortionScale = minecraft.options.screenEffectScale().get().floatValue();
        if (distortionScale <= 0.0f) return;

        float nauseaIntensity = 0.0f;
        if (player instanceof LocalPlayerAccessor accessor) {
            nauseaIntensity = Mth.lerp(
                    tickDelta,
                    accessor.havoc$getLastNauseaIntensity(),
                    accessor.havoc$getNauseaIntensity()
            );
        }

        float nauseaEffect = player.getEffectBlendFactor(MobEffects.NAUSEA, tickDelta);
        float strength = Math.max(nauseaIntensity, nauseaEffect) * (distortionScale * distortionScale);
        if (strength <= 0.0f) return;

        float scale = 5.0f / (strength * strength + 5.0f) - strength * 0.04f;
        scale *= scale;
        Vector3f axis = new Vector3f(0.0f, Mth.SQRT_OF_TWO / 2.0f, Mth.SQRT_OF_TWO / 2.0f);
        float angle = (this.spinningEffectTime + tickDelta * this.spinningEffectSpeed) * Mth.DEG_TO_RAD;
        projection.rotate(angle, axis);
        projection.scale(1.0f / scale, 1.0f, 1.0f);
        projection.rotate(-angle, axis);
    }

    @Inject(method = "renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V", at = @At("HEAD"), cancellable = true)
    private void freecam$hand(CameraRenderState cameraRenderState, float tickDelta, Matrix4fc positionMatrix, CallbackInfo ci) {
        if (ClickGuiRenderer.suppressesBackgroundRendering() || VisualPreviewRuntime.isActive()) {
            ci.cancel();
            return;
        }
        Freecam fc = Modules.get(Freecam.class);
        if (fc != null && fc.isEnabled() && !fc.renderHand()) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures(Lnet/minecraft/client/renderer/SubmitNodeStorage;)V"
            )
    )
    private void havoc$renderChamsHandPreparedFrame(FeatureRenderDispatcher dispatcher,
                                                        SubmitNodeStorage storage,
                                                        Operation<Void> original,
                                                        CameraRenderState cameraRenderState,
                                                        float tickDelta,
                                                        Matrix4fc positionMatrix,
                                                        @Local(ordinal = 0) PoseStack handPoseStack) {
        boolean nativeIrisHand = IrisCompatibilityGuards.suppressIrisHandRendering()
                && IrisRuntime.submitNativeHandScene(tickDelta, handPoseStack, storage);

        Chams module = Modules.get(Chams.class);
        SubmitNodeStorage snapshot = module != null ? module.snapshotPreparedHandScene(storage) : null;

        // Render Minecraft's live hand storage first. renderAllFeatures() consumes it in 26.2.
        if (nativeIrisHand) {
            IrisRuntime.runWithNativeShaderBypass(() -> original.call(dispatcher, storage));
        } else {
            original.call(dispatcher, storage);
        }

        // Imported HAND scenes share the actual first-person phase. They are deliberately submitted
        // after the world/post chain (and after the vanilla hand storage draw) so they can be used as
        // first-person model replacements/overlays without ever leaking into the world instance pass.
        if (minecraft != null && minecraft.gameRenderer != null && cameraRenderState != null) {
            ImportedAssetCompatibilityRenderer.renderHand(
                    minecraft.gameRenderer.mainRenderTarget(),
                    handPoseStack.last().pose(),
                    cameraRenderState.projectionMatrix);
        }

        // Chams renders only the isolated snapshot afterwards; it must never consume the live storage.
        if (module != null && snapshot != null) {
            module.renderPreparedHandScene(snapshot);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("TAIL"))
    private void havoc$flushQueuedButtons(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && ClientScreen.current() != null) {
            if (BetterButtons.hasPending()) {
                BetterButtons.flush();
            }
            BetterTooltips.renderTooltipWithContext(BetterButtons.getLastContext());
        }

        // This is the actual final Havoc 2D drain for the frame. BetterButtons and
        // BetterTooltips intentionally render from this TAIL hook, so draining SCREEN_ABSOLUTE_TOP
        // any earlier (for example immediately after GuiRenderer.render()) still allowed those
        // screen-owned surfaces to cover the debug HUD.
        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.SCREEN_ABSOLUTE_TOP);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void havoc$captureWorldForHudGlass(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        if (!havoc.client.runtime.RuntimeGate.canRunRender()) {
            MenuBackgroundRenderer.clearDeferred();
            return;
        }
        MenuBackgroundRenderer.drainDeferred(minecraft);
        Renderer2D.prepareDeferredUiItems();
        UiBlurResources.captureWorldSource();
        if (AddonRenderPipelineManager.hasActiveCallbacks(HavocRenderStage.SCREEN_BEFORE_VANILLA_GUI)) {
            float tickDelta = tickCounter.getGameTimeDeltaPartialTick(true);
            Renderer2D.COLOR.begin();
            AddonRenderPipelineManager.render2D(HavocRenderStage.SCREEN_BEFORE_VANILLA_GUI,
                    null, Renderer2D.COLOR, TextRenderer.get(), null, tickDelta);
            Renderer2D.COLOR.render();
        }
        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.BEFORE_VANILLA_GUI);
        havoc$drainHudLayersBelowScreenGui();
    }

    /**
     * Gui.extractRenderState() extracts vanilla HUD first and the current Screen after it.
     * Normal gameplay keeps Havoc HUD spliced into that HUD order through HudDeferredGuiElement
     * markers. When a real Screen is open, however, the screen must remain the top GUI owner:
     * HUD/world overlaysare drained before GuiRenderer.render(),
     * preserving their internal order while keeping container/inventory GUI above them.
     * Explicit SCREEN_TOP/AFTER_VANILLA_GUI overlays (Dynamic Island, ClickGui top layer, etc.) are
     * intentionally not drained here.
     */
    @Unique
    private static void havoc$drainHudLayersBelowScreenGui() {
        if (ClientScreen.current() == null) {
            return;
        }

        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.HUD_FIRST);
        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.HUD_BEFORE_MISC_OVERLAYS);
        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.HUD_AFTER_MISC_OVERLAYS);
        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.HUD_AFTER_BOSS_BAR);
        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.HUD_BEFORE_DEMO_TIMER);
        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.HUD_BEFORE_CHAT);
        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.HUD_AFTER_SUBTITLES);
        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.HUD_LAST);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V",
                    shift = At.Shift.AFTER
            )
    )
    private void havoc$debug2dImmediateAfterGui(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        Renderer2D.drainDeferred2D(Renderer2D.Deferred2DLayer.AFTER_VANILLA_GUI);
        if (AddonRenderPipelineManager.hasActiveCallbacks(HavocRenderStage.SCREEN_AFTER_VANILLA_GUI)) {
            float tickDelta = tickCounter.getGameTimeDeltaPartialTick(true);
            Renderer2D.COLOR.begin();
            AddonRenderPipelineManager.render2D(HavocRenderStage.SCREEN_AFTER_VANILLA_GUI,
                    null, Renderer2D.COLOR, TextRenderer.get(), null, tickDelta);
            Renderer2D.COLOR.render();
        }
        UiClipDebugScene.renderAfterGui();
    }

@Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void havoc$disableHurtCamera(CameraRenderState cameraRenderState, PoseStack matrices, CallbackInfo ci) {
        NoRender noRender = Modules.get(NoRender.class);
        if (noRender != null && noRender.off("camera_shake")) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void havoc$disableViewBobbing(CameraRenderState cameraRenderState, PoseStack matrices, CallbackInfo ci) {
        Freecam freecam = Modules.get(Freecam.class);
        if (freecam != null && freecam.isEnabled()) {
            ci.cancel();
            return;
        }

        NoRender noRender = Modules.get(NoRender.class);
        if (noRender != null && noRender.off("view_bob")) {
            ci.cancel();
        }
    }
}
