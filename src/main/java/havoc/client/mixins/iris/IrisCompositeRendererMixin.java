/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.iris;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import havoc.client.render.iris.IrisShaderpackMsaaIntegration;
import havoc.client.render.iris.IrisShaderpackTemporalIntegration;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = CompositeRenderer.class, remap = false)
public abstract class IrisCompositeRendererMixin {
    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_drawElements(IIIJ)V"
            ),
            remap = false
    )
    private void havoc$resolvePackedGbufferAfterShading(int mode,
                                                            int count,
                                                            int type,
                                                            long indices,
                                                            Operation<Void> original) {
        Runnable draw = () -> original.call(mode, count, type, indices);
        if (!IrisShaderpackMsaaIntegration.drawDeferredPerSample(draw)) {
            draw.run();
        }
    }

    @Inject(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_drawElements(IIIJ)V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void havoc$finishShaderpackPassDraw(CallbackInfo ci) {
        IrisShaderpackTemporalIntegration.afterPassDraw();
    }

    @Inject(method = "renderAll", at = @At("TAIL"), remap = false)
    private void havoc$finishShaderpackTemporalBoundary(CallbackInfo ci) {
        IrisShaderpackTemporalIntegration.endRenderer();
    }
}
