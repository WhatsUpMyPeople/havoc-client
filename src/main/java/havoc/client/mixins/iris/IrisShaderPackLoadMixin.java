/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.iris;

import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import havoc.client.render.iris.patch.ShaderPatchEngine;
import havoc.client.render.iris.IrisRuntime;

@Pseudo
@Mixin(value = Iris.class, remap = false)
public abstract class IrisShaderPackLoadMixin {
    @Inject(method = "loadExternalShaderpack", at = @At("HEAD"), remap = false)
    private static void havoc$beginShaderPatchSession(String shaderPackName, CallbackInfoReturnable<Boolean> cir) {
        IrisRuntime.invalidateIntegration("shaderpack_load");
        ShaderPatchEngine.beginShaderPackLoad(shaderPackName);
    }

    @Inject(method = "loadExternalShaderpack", at = @At("RETURN"), remap = false)
    private static void havoc$endShaderPatchSession(String shaderPackName, CallbackInfoReturnable<Boolean> cir) {
        ShaderPatchEngine.endShaderPackLoad(shaderPackName);
    }
}
