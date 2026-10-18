/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.deferred;

/** Secondary world-view families produced by shadow/reflection/probe systems. */
public enum DeferredViewFamily {
    SHADOW_CASCADE,
    LOCAL_LIGHT_SHADOW,
    REFLECTION_CASCADE,
    REFLECTION_PROBE,
    CUSTOM
}
