/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.deferred;

/**
 * Semantic producer identities for final-frame temporal coverage.
 *
 * <p>The producer identity is explicit so a future TAA/TAAU resolve can apply policy without
 * reconstructing object type from color/depth. This enum does not prescribe a filtering algorithm.</p>
 */
public enum DeferredTemporalCoverageProducer {
    OPAQUE_BASE,
    ENTITY,
    BLOCK_ENTITY,
    WATER,
    TRANSLUCENT,
    PARTICLE,
    PORTAL,
    WEATHER,
    EXTENSION
}
