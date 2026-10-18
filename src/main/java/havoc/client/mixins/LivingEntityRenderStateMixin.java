/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import havoc.client.render.ViewObstructionFadeState;
import havoc.client.features.playeranimator.PlayerRigInstance;
import havoc.client.features.playeranimator.PlayerRigRenderState;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements ViewObstructionFadeState, PlayerRigRenderState {
    @Unique
    private boolean havoc$viewObstructionFadeActive;

    @Unique
    private float havoc$viewObstructionFadeAlpha = 1.0f;

    @Unique
    private boolean havoc$seeInvisibleFadeActive;

    @Unique
    private PlayerRigInstance havoc$playerRig;

    @Override
    public boolean havoc$isViewObstructionFadeActive() {
        return havoc$viewObstructionFadeActive;
    }

    @Override
    public float havoc$getViewObstructionFadeAlpha() {
        return havoc$viewObstructionFadeAlpha;
    }

    @Override
    public void havoc$setViewObstructionFadeActive(boolean active) {
        this.havoc$viewObstructionFadeActive = active;
    }

    @Override
    public void havoc$setViewObstructionFadeAlpha(float alpha) {
        this.havoc$viewObstructionFadeAlpha = alpha;
    }

    @Override
    public boolean havoc$isSeeInvisibleFadeActive() {
        return havoc$seeInvisibleFadeActive;
    }

    @Override
    public void havoc$setSeeInvisibleFadeActive(boolean active) {
        this.havoc$seeInvisibleFadeActive = active;
    }

    @Override
    public PlayerRigInstance havoc$getPlayerRig() {
        return havoc$playerRig;
    }

    @Override
    public void havoc$setPlayerRig(PlayerRigInstance rig) {
        this.havoc$playerRig = rig;
    }
}
