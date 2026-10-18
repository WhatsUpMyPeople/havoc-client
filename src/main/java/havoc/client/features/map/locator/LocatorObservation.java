/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.map.locator;

import java.util.UUID;

public record LocatorObservation(
        UUID targetUuid,
        String targetName,
        LocatorObservationType type,
        double observerX,
        double observerZ,
        double x,
        double y,
        double z,
        double bearingRadians,
        double uncertaintyRadius,
        double minimumHorizontalRange,
        long observedAtMs,
        long sourceRevision,
        long sourceGeneration
) {}
