/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.player.ClientInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.movement.Sprint;

@Mixin(ClientInput.class)
public abstract class InputMixin {

    @Inject(method = "hasForwardImpulse", at = @At("HEAD"), cancellable = true)
    private void allowAllDirections(CallbackInfoReturnable<Boolean> cir) {
        Sprint module = Modules.get(Sprint.class);
        if (module == null || !module.shouldAllowDirectionalSprint()) return;

        ClientInput input = (ClientInput) (Object) this;
        if (input.keyPresses.forward()
                || input.keyPresses.backward()
                || input.keyPresses.left()
                || input.keyPresses.right()) {
            cir.setReturnValue(true);
        }
    }
}
