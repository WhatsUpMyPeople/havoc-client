/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixininterface;

public interface ILocalPlayer {
    float havoc$getLastYaw();

    float havoc$getLastPitch();

    void havoc$setLastYaw(float yaw);

    void havoc$setLastPitch(float pitch);
}
