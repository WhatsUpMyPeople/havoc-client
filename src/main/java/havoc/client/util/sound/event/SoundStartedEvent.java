/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.sound.event;

import havoc.client.events.Event;
import havoc.client.util.sound.SoundDefinition;
import havoc.client.util.sound.SoundInstance;

public final class SoundStartedEvent extends Event {
    private final SoundDefinition definition;
    private final SoundInstance instance;

    public SoundStartedEvent(SoundDefinition definition, SoundInstance instance) {
        this.definition = definition;
        this.instance = instance;
    }

    public SoundDefinition definition() { return definition; }
    public SoundInstance instance() { return instance; }
}
