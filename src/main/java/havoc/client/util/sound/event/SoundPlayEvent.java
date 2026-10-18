/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.sound.event;

import havoc.client.events.Event;
import havoc.client.util.sound.SoundRequest;

/** Cancellable event fired before a source or buffer is allocated. The request is mutable. */
public final class SoundPlayEvent extends Event {
    private final SoundRequest request;

    public SoundPlayEvent(SoundRequest request) {
        this.request = request;
    }

    public SoundRequest request() {
        return request;
    }
}
