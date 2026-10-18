/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixininterface;

public interface IPlayerAttackCooldown {
    float havoc$getAttackCooldownProgress(float tickDelta);

    boolean havoc$isAttackCharged(float tickDelta);
}
