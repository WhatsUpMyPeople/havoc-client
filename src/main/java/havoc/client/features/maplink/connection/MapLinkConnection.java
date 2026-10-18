/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.features.maplink.connection;

import havoc.client.features.maplink.model.MapLinkFetchContext;
import havoc.client.features.maplink.model.MapLinkFetchResult;
import havoc.client.features.maplink.model.MapLinkProviderType;

import java.io.IOException;

/** Fetches player locations from a web-map provider. */
public interface MapLinkConnection {
    MapLinkProviderType type();

    MapLinkFetchResult fetchPlayers(MapLinkFetchContext context) throws IOException, InterruptedException;
}
