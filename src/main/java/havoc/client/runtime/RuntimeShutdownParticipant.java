/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.runtime;

public interface RuntimeShutdownParticipant {
    String id();

    void shutdown(RuntimeShutdownContext context) throws Exception;
}
