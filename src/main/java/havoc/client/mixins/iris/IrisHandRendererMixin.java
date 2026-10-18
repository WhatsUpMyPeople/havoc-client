/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.iris;

import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.render.iris.IrisCompatibilityGuards;

@Pseudo
@Mixin(value = HandRenderer.class, remap = false)
public abstract class IrisHandRendererMixin {
    @Inject(method = "renderSolid", at = @At("HEAD"), cancellable = true, remap = false)
    private void havoc$disableIrisSolidHand(Matrix4fc modelMatrix,
                                                float tickDelta,
                                                Camera camera,
                                                CameraRenderState cameraRenderState,
                                                GameRenderer gameRenderer,
                                                WorldRenderingPipeline pipeline,
                                                CallbackInfo ci) {
        if (IrisCompatibilityGuards.suppressIrisHandRendering()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTranslucent", at = @At("HEAD"), cancellable = true, remap = false)
    private void havoc$disableIrisTranslucentHand(Matrix4fc modelMatrix,
                                                      float tickDelta,
                                                      Camera camera,
                                                      CameraRenderState cameraRenderState,
                                                      GameRenderer gameRenderer,
                                                      WorldRenderingPipeline pipeline,
                                                      CallbackInfo ci) {
        if (IrisCompatibilityGuards.suppressIrisHandRendering()) {
            ci.cancel();
        }
    }
}
