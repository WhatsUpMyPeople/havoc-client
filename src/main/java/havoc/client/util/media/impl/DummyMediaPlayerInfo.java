/*
 * This file is part of the Havoc Client distribution.
 * Havoc modifications copyright (c) 2026 pivosos2007.
 *
 * This file belongs to Havoc's MediaPlayerInfo integration, based on
 * https://github.com/Redstonecrafter0/MediaPlayerInfo.
 * Copyright (c) Redstonecrafter0 and contributors.
 *
 * Licensed under the GNU Affero General Public License v3.0.
 * See THIRD_PARTY_NOTICES.md for details.
 */

package havoc.client.util.media.impl;

import havoc.client.util.media.IMediaSession;
import havoc.client.util.media.MediaPlayerInfo;

import java.util.List;

public final class DummyMediaPlayerInfo implements MediaPlayerInfo {
    public static final DummyMediaPlayerInfo INSTANCE = new DummyMediaPlayerInfo();

    private DummyMediaPlayerInfo() {
    }

    @Override
    public List<IMediaSession> getMediaSessions() {
        return List.of();
    }
}
