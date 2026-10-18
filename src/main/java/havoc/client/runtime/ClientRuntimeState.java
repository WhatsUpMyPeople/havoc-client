/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.runtime;

public enum ClientRuntimeState {
    BOOTING,
    ACTIVE,
    SOFT_PANIC,
    JAR_REPLACEMENT_PANIC,
    SHUTDOWN_PENDING,
    DEAD
}
