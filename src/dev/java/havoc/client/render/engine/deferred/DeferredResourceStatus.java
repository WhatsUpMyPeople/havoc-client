/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.deferred;

/** Semantic producer result. GPU allocation/logical validity is tracked separately. */
public enum DeferredResourceStatus {
    PRODUCED,
    FALLBACK,
    DISABLED,
    MISSING_INPUT,
    FAILED,
    STALE,
    UNAVAILABLE
}
