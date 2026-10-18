/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.iris;

import net.minecraft.client.DeltaTracker;

public interface IrisFinalizedSceneRenderer {
    void havoc$renderAfterIrisFinalization(DeltaTracker tickCounter);
}
