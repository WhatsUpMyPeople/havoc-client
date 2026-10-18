/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.sodium;

public record SodiumTerrainInteropStatsSnapshot(long terrainUpdatesScheduled,
                                                long rebuildsScheduled,
                                                long observedOpaqueDraws,
                                                long observedCutoutDraws,
                                                long observedTranslucentDraws,
                                                long observedUnknownDraws,
                                                long interopErrors) {
    public static final SodiumTerrainInteropStatsSnapshot EMPTY =
            new SodiumTerrainInteropStatsSnapshot(0, 0, 0, 0, 0, 0, 0);

    public long observedDraws() {
        return observedOpaqueDraws + observedCutoutDraws + observedTranslucentDraws + observedUnknownDraws;
    }
}
