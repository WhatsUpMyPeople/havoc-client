/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.features.maplink.net;

import java.io.IOException;

public final class MapLinkHttpException extends IOException {
    private final int statusCode;

    public MapLinkHttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
