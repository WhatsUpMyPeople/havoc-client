/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.events.impl;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;
import havoc.client.events.Event;

@Setter
@Getter
public final class FireworkEvent extends Event {
    private Vec3 vector;

    public FireworkEvent(Vec3 vector) {
        this.vector = vector;
    }

}
