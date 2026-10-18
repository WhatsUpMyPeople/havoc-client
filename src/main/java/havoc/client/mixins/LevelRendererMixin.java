/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import havoc.client.config.MainConfig;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.features.module.ModuleManager;
import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.visuals.BlockHighlight;
import havoc.client.features.module.modules.visuals.MotionBlur;
import havoc.client.features.module.modules.visuals.ReimaginedVisual;
import havoc.client.features.module.modules.visuals.WorldTweaks;
import havoc.client.mixins.accessors.GameRendererAccessor;
import havoc.client.mixins.accessors.LevelRendererAccessor;
import havoc.client.render.engine.core.HavocWorldMatrices;
import havoc.client.render.engine.RenderState;
import havoc.client.render.engine.asset.gltf.render.ImportedAssetCompatibilityRenderer;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.core.RenderPhase;
import havoc.client.render.engine.core.RenderPhaseScope;
import havoc.client.render.engine.depth.PreTranslucentDepth;
import havoc.client.render.engine.depth.EntityDepthLayer;
import havoc.client.render.engine.depth.WorldSceneDepth;
import havoc.client.render.engine.renderer.MeshRenderer;
import havoc.client.render.iris.IrisRuntime;
import havoc.client.render.sky.CustomSkyboxRenderer;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {


    @Shadow
    @Final
    private LevelTargetBundle targets;

    @WrapOperation(
            method = "addSkyPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;mainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"
            )
    )
    private RenderTarget havoc$stableSkyRendererTarget(GameRenderer instance, Operation<RenderTarget> original) {
        return ((GameRendererAccessor) instance).havoc$getMainRenderTargetRaw();
    }

    @Unique
    private static void havoc$dependsOn(FramePass pass, ResourceHandle<RenderTarget> handle) {
        if (pass != null && handle != null) {
            pass.reads(handle);
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static ResourceHandle<RenderTarget> havoc$asRenderTargetHandle(ResourceHandle<?> handle) {
        return (ResourceHandle<RenderTarget>) handle;
    }

    @Unique
    private static RenderTarget havoc$getFramebuffer(ResourceHandle<RenderTarget> handle) {
        if (handle == null) {
            return null;
        }
        try {
            return handle.get();
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Unique
    private static boolean havoc$needsWorldSceneDepthCapture() {
        ReimaginedVisual visual = Modules.get(ReimaginedVisual.class);
        return (visual != null && visual.needsWorldSceneDepthCapture())
                || MotionBlur.isActiveStatic()
                || MainConfig.get().isTaaRuntimeActive();
    }

    @Unique
    private static boolean shouldSkipSky(net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState) {
        FogType submersion = cameraRenderState.fogType;
        if (submersion == FogType.POWDER_SNOW || submersion == FogType.LAVA) {
            return true;
        }
        return cameraRenderState.entityRenderState != null
                && cameraRenderState.entityRenderState.doesMobEffectBlockSky;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void havoc$captureIrisModelView(
            GraphicsResourceAllocator allocator,
            DeltaTracker tickCounter,
            boolean renderBlockOutline,
            net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState,
            org.joml.Matrix4fc positionMatrix,
            GpuBufferSlice fogBuffer,
            org.joml.Vector4f fogColor,
            boolean renderSky,
            CallbackInfo ci
    ) {
        if (positionMatrix == null) {
            return;
        }

        if (HavocWorldMatrices.isValid()) {
            HavocWorldMatrices.capturePosition(
                    positionMatrix,
                    cameraRenderState != null && cameraRenderState.pos != null
                            ? cameraRenderState.pos
                            : null
            );
        } else if (cameraRenderState != null && cameraRenderState.projectionMatrix != null) {
            HavocWorldMatrices.capture(
                    positionMatrix,
                    cameraRenderState.projectionMatrix,
                    cameraRenderState.projectionMatrix,
                    cameraRenderState.pos
            );
        }

    }

    @Inject(
            method = "submitBlockOutline",
            at = @At("HEAD"),
            cancellable = true
    )
    private void havoc$disableVanillaOutline(PoseStack matrices,
                                                 SubmitNodeCollector consumers,
                                                 LevelRenderState renderState,
                                                 CallbackInfo ci) {
        BlockHighlight blockHighlight = Modules.get(BlockHighlight.class);
        if (blockHighlight != null && blockHighlight.isOutlineActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "submitFeatures", at = @At("HEAD"))
    private void havoc$snapshotEntityStates(LevelRenderState state,
                                                SubmitNodeCollector collector,
                                                boolean special, CallbackInfo ci) {
        if (ModuleManager.needsEntityOnlyDepth()) {
            EntityDepthLayer.snapshotEntities(state.entityRenderStates);
        } else {
            EntityDepthLayer.reset();
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void havoc$captureWorldSceneDepth(
            GraphicsResourceAllocator allocator,
            DeltaTracker tickCounter,
            boolean renderBlockOutline,
            net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState,
            org.joml.Matrix4fc positionMatrix,
            GpuBufferSlice fogBuffer,
            org.joml.Vector4f fogColor,
            boolean renderSky,
            CallbackInfo ci,
            @Local FrameGraphBuilder frameGraphBuilder
    ) {
        if (frameGraphBuilder == null || targets == null || targets.main == null) {
            EntityDepthLayer.reset();
            return;
        }

        ResourceHandle<RenderTarget> main = targets.main;
        if (ModuleManager.needsEntityOnlyDepth()) {
            FramePass entityDepthPass = frameGraphBuilder.addPass("havoc_entity_only_depth");
            havoc$dependsOn(entityDepthPass, main);
            entityDepthPass.disableCulling();
            entityDepthPass.executes(() -> {
                RenderTarget mainFramebuffer = havoc$getFramebuffer(main);
                Minecraft client = Minecraft.getInstance();
                RenderTarget fallbackFramebuffer = client != null ? client.gameRenderer.mainRenderTarget() : null;
                int width = mainFramebuffer != null ? mainFramebuffer.width : fallbackFramebuffer != null ? fallbackFramebuffer.width : 1;
                int height = mainFramebuffer != null ? mainFramebuffer.height : fallbackFramebuffer != null ? fallbackFramebuffer.height : 1;
                LevelRenderState state = ((LevelRendererAccessor) (Object) this).havoc$getWorldRenderState();
                EntityDepthLayer.capture((LevelRenderer) (Object) this, state, width, height);
            });
        } else {
            EntityDepthLayer.reset();
        }

        if (IrisRuntime.isShaderpackRendererActive()) {
            WorldSceneDepth.reset();
            return;
        }
        if (!havoc$needsWorldSceneDepthCapture()) {
            WorldSceneDepth.reset();
            return;
        }

        ResourceHandle<RenderTarget> translucent = targets.translucent;
        ResourceHandle<RenderTarget> itemEntity = targets.itemEntity;
        ResourceHandle<RenderTarget> particles = targets.particles;
        ResourceHandle<RenderTarget> weather = targets.weather;

        FramePass pass = frameGraphBuilder.addPass("havoc_scene_depth_capture");
        havoc$dependsOn(pass, main);
        havoc$dependsOn(pass, translucent);
        havoc$dependsOn(pass, itemEntity);
        havoc$dependsOn(pass, particles);
        havoc$dependsOn(pass, weather);
        pass.disableCulling();
        pass.executes(() -> {
            RenderTarget mainFramebuffer = havoc$getFramebuffer(main);
            Minecraft client = Minecraft.getInstance();
            RenderTarget fallbackFramebuffer = client != null ? client.gameRenderer.mainRenderTarget() : null;
            int width = mainFramebuffer != null ? mainFramebuffer.width : fallbackFramebuffer != null ? fallbackFramebuffer.width : 1;
            int height = mainFramebuffer != null ? mainFramebuffer.height : fallbackFramebuffer != null ? fallbackFramebuffer.height : 1;

            WorldSceneDepth.beginFrame(width, height);
            WorldSceneDepth.captureMain(mainFramebuffer);
            WorldSceneDepth.captureTranslucent(havoc$getFramebuffer(translucent));
            WorldSceneDepth.captureItemEntity(havoc$getFramebuffer(itemEntity));
            WorldSceneDepth.captureParticles(havoc$getFramebuffer(particles));
            WorldSceneDepth.captureWeather(havoc$getFramebuffer(weather));
        });
    }

    @Inject(method = "addSkyPass", at = @At("HEAD"), cancellable = true)
    private void havoc$renderSky(FrameGraphBuilder frameGraphBuilder,
                                     net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState,
                                     GpuBufferSlice fog, CallbackInfo ci) {
        if (IrisRuntime.isModLoaded() || shouldSkipSky(cameraRenderState)) return;
        LevelRendererAccessor accessor = (LevelRendererAccessor) this;
        SkyRenderState sky = accessor.havoc$getWorldRenderState().skyRenderState;
        if (sky == null || sky.skybox != DimensionType.Skybox.OVERWORLD) return;
        WorldTweaks worldTweaks = Modules.get(WorldTweaks.class);
        if (worldTweaks != null) worldTweaks.applySkyOverrides(sky);
        ReimaginedVisual visual = Modules.get(ReimaginedVisual.class);
        if (visual == null || !visual.isEnabled()) return;
        Minecraft client = Minecraft.getInstance();
        net.minecraft.client.Camera camera = client != null && client.gameRenderer != null
                ? client.gameRenderer.mainCamera() : null;
        if (camera == null) return;
        FramePass pass = frameGraphBuilder.addPass("havoc_sky");
        targets.main = pass.readsAndWrites(targets.main);
        pass.executes(() -> CustomSkyboxRenderer.render(camera, fog, sky, accessor.havoc$getSkyRendering()));
        ci.cancel();
    }

    @Inject(
            method = "lambda$addMainPass$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            ),
            remap = false
    )
    private void havoc$capturePreWaterScene(
            GpuBufferSlice fogBuffer,
            LevelRenderState worldRenderState,
            net.minecraft.util.profiling.ProfilerFiller profiler,
            net.minecraft.client.renderer.chunk.ChunkSectionsToRender chunkSections,
            com.mojang.blaze3d.resource.ResourceHandle<?> entityOutlineHandle,
            net.minecraft.client.renderer.feature.FeatureRenderDispatcher.PreparedFrame preparedFrame,
            com.mojang.blaze3d.resource.ResourceHandle<?> translucentFramebufferHandle,
            com.mojang.blaze3d.resource.ResourceHandle<?> mainFramebufferHandle,
            com.mojang.blaze3d.resource.ResourceHandle<?> itemEntityFramebufferHandle,
            com.mojang.blaze3d.resource.ResourceHandle<?> particlesFramebufferHandle,
            CallbackInfo ci
    ) {
        if (IrisRuntime.isShaderpackRendererActive()) {
            return;
        }
        RenderTarget mainFramebuffer = havoc$getFramebuffer(havoc$asRenderTargetHandle(mainFramebufferHandle));
        if (mainFramebuffer == null) {
            Minecraft mc = Minecraft.getInstance();
            mainFramebuffer = mc != null && mc.gameRenderer != null ? mc.gameRenderer.mainRenderTarget() : null;
        }
        PreTranslucentDepth.captureFrom(mainFramebuffer);
    }

    @Inject(
            method = "lambda$addMainPass$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeTranslucent()V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void havoc$renderImportedWorldEntities(
            GpuBufferSlice fogBuffer,
            LevelRenderState worldRenderState,
            net.minecraft.util.profiling.ProfilerFiller profiler,
            net.minecraft.client.renderer.chunk.ChunkSectionsToRender chunkSections,
            com.mojang.blaze3d.resource.ResourceHandle<?> entityOutlineHandle,
            net.minecraft.client.renderer.feature.FeatureRenderDispatcher.PreparedFrame preparedFrame,
            com.mojang.blaze3d.resource.ResourceHandle<?> translucentFramebufferHandle,
            com.mojang.blaze3d.resource.ResourceHandle<?> mainFramebufferHandle,
            com.mojang.blaze3d.resource.ResourceHandle<?> itemEntityFramebufferHandle,
            com.mojang.blaze3d.resource.ResourceHandle<?> particlesFramebufferHandle,
            CallbackInfo ci
    ) {
        if (IrisRuntime.isShaderpackRendererActive() || !HavocWorldMatrices.isValid()) {
            return;
        }
        RenderTarget mainFramebuffer = havoc$getFramebuffer(
                havoc$asRenderTargetHandle(mainFramebufferHandle));
        RenderTarget entityTranslucentFramebuffer = havoc$getFramebuffer(
                havoc$asRenderTargetHandle(itemEntityFramebufferHandle));
        RenderTarget renderTarget = entityTranslucentFramebuffer != null
                ? entityTranslucentFramebuffer : mainFramebuffer;
        org.joml.Matrix4f position = HavocWorldMatrices.positionMatrix();
        org.joml.Matrix4f projection = HavocWorldMatrices.renderProjectionMatrix();
        if (renderTarget == null || position == null || projection == null) {
            return;
        }

        var modelView = RenderSystem.getModelViewStack();
        boolean previousRendering3D = RenderState.rendering3D;
        modelView.pushMatrix();
        try (RenderPhaseScope ignored = HavocRenderSystem.phase(RenderPhase.WORLD_BEFORE_TRANSLUCENT)) {
            modelView.identity();
            modelView.mul(position);
            RenderState.rendering3D = true;
            RenderState.worldProjection.set(projection);
            MeshRenderer.setProjection(projection);
            ImportedAssetCompatibilityRenderer.renderWorldEntities(renderTarget);
        } finally {
            RenderState.rendering3D = previousRendering3D;
            modelView.popMatrix();
        }
    }
}
