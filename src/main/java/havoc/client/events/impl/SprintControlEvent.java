/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.events.impl;

import havoc.client.events.Event;
import lombok.Getter;
import lombok.Setter;

public class SprintControlEvent extends Event {
    @Getter
    private final float forward;
    @Getter
    private final float sideways;
    @Getter
    private final Source source;
    @Setter
    private boolean sprint;
    public SprintControlEvent(float forward, float sideways, boolean sprint, Source source) {
        this.forward = forward;
        this.sideways = sideways;
        this.sprint = sprint;
        this.source = source;
    }

    public boolean isMoving() {
        return forward != 0.0f || sideways != 0.0f;
    }

    public boolean shouldSprint() {
        return sprint;
    }

    public enum Source {
        INPUT,
        MOVEMENT_TICK,
        NETWORK
    }
}
