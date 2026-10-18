/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.material;

/** Diagnostic provenance for contract-first material resolution. Ordered from strongest to weakest. */
public enum MaterialResolutionSource {
    EXPLICIT_DESCRIPTOR,
    TAG,
    LABPBR_METADATA,
    IMPORTED_ASSET,
    VANILLA_FALLBACK,
    UNKNOWN
}
