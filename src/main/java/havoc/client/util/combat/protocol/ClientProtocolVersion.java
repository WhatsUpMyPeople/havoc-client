/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.combat.protocol;

/**
 * Simple protocol version holder (client-side).
 */
public record ClientProtocolVersion(String name, int version) {
}
