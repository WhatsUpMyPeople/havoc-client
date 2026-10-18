/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.iris;

import havoc.client.render.iris.HavocIrisUniforms;
import havoc.client.render.iris.geometry.IrisImportedGeometryRenderer;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Finalizes Havoc state after ExtendedShader has bound the real GL program, updated Iris
 * uniforms/custom uniforms and selected the shaderpack framebuffer.
 */
@Pseudo
@Mixin(value = ExtendedShader.class, remap = false)
public abstract class IrisExtendedShaderGeometryMixin {
    @Inject(method = "iris$setupState", at = @At("RETURN"), remap = false)
    private void havoc$finalizeExtendedShaderState(CallbackInfo ci) {
        HavocIrisUniforms.applyAaToCurrentProgram("ExtendedShader.iris$setupState:return");
        IrisImportedGeometryRenderer.applyCurrentDrawUniforms();
    }
}
