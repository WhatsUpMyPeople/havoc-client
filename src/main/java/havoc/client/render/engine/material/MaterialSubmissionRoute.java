/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.material;

/** Explicit renderer ownership for a surface. */
public enum MaterialSubmissionRoute {
    DEFERRED_GBUFFER,
    FORWARD_TRANSLUCENT,
    EXTRACTED_PATCH,
    FORWARD_SPECIAL,
    COMPATIBILITY
}
