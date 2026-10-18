/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.scene.instance;

public record SceneInstanceStatsSnapshot(
        int instances,
        long frameId,
        long visibleVisits,
        long lodChanges,
        long transformUpdates,
        long spatialUpdates,
        long discontinuities
) {
}
