/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins;

import havoc.client.mixininterface.ITemporalMotionRenderState;
import havoc.client.render.engine.temporal.TemporalMotionState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntityRenderState.class)
public class BlockEntityRenderStateTemporalMixin implements ITemporalMotionRenderState {
    @Unique private @Nullable TemporalMotionState havoc$temporalMotionState;
    @Override public @Nullable TemporalMotionState havoc$getTemporalMotionState() { return havoc$temporalMotionState; }
    @Override public void havoc$setTemporalMotionState(@Nullable TemporalMotionState state) { havoc$temporalMotionState = state; }
}
