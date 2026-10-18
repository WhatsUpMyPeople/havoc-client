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

@Getter
public class PlayerVelocityStrafe extends Event {

    private final Vec3 movementInput;
    private final float speed;
    private final float yaw;
    @Setter
    private Vec3 velocity;

    public PlayerVelocityStrafe(Vec3 movementInput, float speed, float yaw, Vec3 velocity) {
        this.movementInput = movementInput;
        this.speed = speed;
        this.yaw = yaw;
        this.velocity = velocity;
    }

}
