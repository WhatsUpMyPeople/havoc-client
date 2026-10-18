/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import havoc.client.util.aiming.RotationManager;
import havoc.client.util.aiming.RotationUtil;
import havoc.client.util.aiming.data.Rotation;
import havoc.client.util.aiming.features.MovementCorrection;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends InputMixin {

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "NEW",
                    target = "(ZZZZZZZ)Lnet/minecraft/world/entity/player/Input;"
            )
    )
    private Input havoc$applyFreeCorrection(Input original) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return original;
        }

        var target = RotationManager.INSTANCE.getActiveRotationTarget();
        if (target == null
                || target.movementCorrection != MovementCorrection.SILENT
                || !target.freeCorrection) {
            return original;
        }

        Rotation rotation = RotationManager.INSTANCE.getMovementRotation();
        return RotationUtil.correctMovementInput(original, mc.player.getYRot(), rotation.yaw());
    }
}
