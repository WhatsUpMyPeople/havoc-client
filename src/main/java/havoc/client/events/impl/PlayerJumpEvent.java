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

@Setter
@Getter
public final class PlayerJumpEvent extends Event {
    private float yaw;
    private float motion;

    public PlayerJumpEvent(float yaw, float motion) {
        this.yaw = yaw;
        this.motion = motion;
    }

}
