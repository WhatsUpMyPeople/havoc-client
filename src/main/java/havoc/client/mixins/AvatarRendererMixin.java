/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.visuals.ViewModel;
import havoc.client.features.playeranimator.PlayerAnimator;
import havoc.client.features.playeranimator.PlayerRigRenderState;
import havoc.client.render.helpers.TickDelta;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Builds the rig only after AvatarRenderer has populated the complete Humanoid/Avatar render state.
 * Doing this from LivingEntityRenderer.extractRenderState is too early: swim, arm-pose and flight
 * fields are filled by AvatarRenderer only after its super call returns.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL")
    )
    private void havoc$extractPlayerRig(Avatar entity, AvatarRenderState state,
                                            float tickProgress, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayer player) || !(state instanceof PlayerRigRenderState rigState)) {
            return;
        }
        ViewModel viewModel = Modules.get(ViewModel.class);
        if (viewModel == null || !viewModel.isPlayerRigActive()) {
            rigState.havoc$setPlayerRig(null);
            return;
        }
        rigState.havoc$setPlayerRig(PlayerAnimator.animate(
                player,
                state,
                tickProgress,
                TickDelta.frameDeltaSeconds(),
                viewModel.playerRigStyle(),
                viewModel.playerRigStrength()
        ));
    }
}
