/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins;

import havoc.client.mixininterface.ITemporalMotionRenderState;
import havoc.client.render.engine.temporal.TemporalMotionState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateTemporalMixin implements ITemporalMotionRenderState {
    @Unique private @Nullable TemporalMotionState havoc$temporalMotionState;
    @Override public @Nullable TemporalMotionState havoc$getTemporalMotionState() { return havoc$temporalMotionState; }
    @Override public void havoc$setTemporalMotionState(@Nullable TemporalMotionState state) { havoc$temporalMotionState = state; }
}
