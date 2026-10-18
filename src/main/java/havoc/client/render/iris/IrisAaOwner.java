/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.iris;

/** Effective owner of world anti-aliasing for the current renderer/pack epoch. */
public enum IrisAaOwner {
    NONE,
    HAVOC_MSAA,
    HAVOC_TAA,
    SHADERPACK_FALLBACK
}
