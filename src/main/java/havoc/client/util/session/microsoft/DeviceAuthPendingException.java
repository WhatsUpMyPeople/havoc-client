/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.session.microsoft;

public final class DeviceAuthPendingException extends RuntimeException {
    public DeviceAuthPendingException(String message) {
        super(message);
    }

    public DeviceAuthPendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
