/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.iris;

import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.targets.RenderTargets;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.render.engine.depth.PreTranslucentDepth;
import havoc.client.render.iris.IrisHavocFrameHooks;
import havoc.client.render.iris.IrisSceneDepth;
import havoc.client.render.iris.IrisRuntime;
import havoc.client.render.iris.IrisShaderpackMsaaIntegration;
import havoc.client.render.iris.IrisShaderpackTemporalIntegration;

@Pseudo
@Mixin(value = IrisRenderingPipeline.class, remap = false)
public abstract class IrisRenderingPipelineFinalizeMixin {
    @Shadow
    @Final
    private RenderTargets renderTargets;

    @Inject(method = "beginLevelRendering", at = @At("HEAD"), remap = false)
    private void havoc$resetIrisSceneDepth(CallbackInfo ci) {
        IrisRuntime.observePipeline(this);
        IrisShaderpackMsaaIntegration.beforeFrameClear();
        IrisSceneDepth.resetFrame();
    }

    @Inject(method = "onBeginClear", at = @At("HEAD"), remap = false)
    private void havoc$seedMsaaAfterMainClear(CallbackInfo ci) {
        // Iris calls beginLevelRendering while LevelRenderer is only building the frame graph.
        // The actual main color/depth clear executes later, immediately before onBeginClear().
        // Activating the multisample twins at beginLevelRendering TAIL therefore let that real
        // clear hit only Minecraft's single-sample main target while the MS depth/color images
        // retained the previous world. Photon then classified those stale depth samples as
        // geometry instead of sky, which leaked the old world across the whole sky/deferred pass.
        // Seed/activate at the post-clear boundary, before Iris starts the SKY phase/horizon pass.
        IrisShaderpackMsaaIntegration.beginFrame(IrisRuntime.integrationEpoch());
    }

    @Inject(method = "beginHand", at = @At("HEAD"), remap = false)
    private void havoc$renderImportedOpaqueBeforeHand(CallbackInfo ci) {
        // This is the final opaque-world boundary before Iris snapshots depthtex2/no-hand.
        // The first-person hand itself is suppressed in Iris and submitted only after finalization.
        IrisSceneDepth.capture(renderTargets);
        IrisRuntime.renderImportedGeometryPrimary();
        IrisShaderpackMsaaIntegration.resolveDepth();
    }

    @Inject(method = "beginTranslucents", at = @At("HEAD"), remap = false)
    private void havoc$capturePreTranslucentDepth(CallbackInfo ci) {
        // Publish a valid single-sample representative for Iris' depth copies and pre-deferred
        // helper passes. Packed colortex1/2 use sample-0 resolve; d4 shades every real MSAA sample.
        IrisShaderpackMsaaIntegration.resolveWorld();
        IrisSceneDepth.capture(renderTargets);
        PreTranslucentDepth.capture();
    }

    @Inject(method = "beginTranslucents", at = @At("TAIL"), remap = false)
    private void havoc$seedMsaaAfterDeferred(CallbackInfo ci) {
        // Deferred shading has already consumed the opaque multisample G-buffer. Only initialize
        // the targets written by the upcoming forward pass; reseeding all targets here flattened
        // per-sample depth/coverage and reintroduced stale scene data.
        IrisShaderpackMsaaIntegration.seedForward();
        IrisRuntime.renderImportedGeometryTranslucent();
    }

    @Inject(method = "finalizeLevelRendering", at = @At("HEAD"), remap = false)
    private void havoc$captureIrisSceneBeforeFinalPass(CallbackInfo ci) {
        IrisShaderpackMsaaIntegration.resolveForward();
        IrisSceneDepth.capture(renderTargets);
        IrisShaderpackTemporalIntegration.beginFrame(renderTargets, IrisRuntime.integrationEpoch());
    }

    @Inject(method = "finalizeLevelRendering", at = @At("TAIL"), remap = false)
    private void havoc$renderAfterIrisFinalPass(CallbackInfo ci) {
        IrisHavocFrameHooks.renderAfterIrisFinalization();
    }

    @Inject(method = "destroy", at = @At("HEAD"), remap = false)
    private void havoc$shutdownDepth(CallbackInfo ci) {
        IrisRuntime.pipelineDestroyed(this);
        IrisShaderpackMsaaIntegration.shutdown();
        IrisShaderpackTemporalIntegration.shutdown();
        IrisSceneDepth.shutdown();
    }
}
