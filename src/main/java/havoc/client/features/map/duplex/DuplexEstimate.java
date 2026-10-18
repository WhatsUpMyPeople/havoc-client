/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.features.map.duplex;

import java.util.UUID;

public record DuplexEstimate(
        UUID targetUuid,
        double x,
        double z,
        double uncertaintyRadius,
        double crossingAngleRadians,
        double confidence,
        long observedAtMs,
        long sourceRevision
) {}
