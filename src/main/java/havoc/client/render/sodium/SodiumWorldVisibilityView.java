/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.sodium;

import net.minecraft.world.phys.AABB;

/**
 * Version-isolated primary-camera visibility view exposed by the Sodium compat mixin.
 * Consumers never receive Sodium implementation types.
 */
public interface SodiumWorldVisibilityView {
    boolean havoc$isBoxVisible(AABB box);
}
