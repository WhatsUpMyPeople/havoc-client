/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.features.maplink.net;

import java.io.IOException;

public final class MapLinkParseException extends IOException {
    public MapLinkParseException(String message) {
        super(message);
    }

    public MapLinkParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
