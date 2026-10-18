/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.deferred;

/** Explicit producer-side temporal response; never inferred from rendered brightness/color delta. */
public enum DeferredReactiveBehavior {
    STABLE(0.0f),
    REDUCED_HISTORY(0.5f),
    REJECT_HISTORY(1.0f);

    private final float maskValue;

    DeferredReactiveBehavior(float maskValue) {
        this.maskValue = maskValue;
    }

    public float maskValue() {
        return maskValue;
    }
}
