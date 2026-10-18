/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixininterface;

import havoc.client.render.engine.temporal.TemporalMotionState;
import org.jetbrains.annotations.Nullable;

/** Carries producer-side temporal motion metadata with a vanilla render state. */
public interface ITemporalMotionRenderState {
    @Nullable TemporalMotionState havoc$getTemporalMotionState();
    void havoc$setTemporalMotionState(@Nullable TemporalMotionState state);
}
