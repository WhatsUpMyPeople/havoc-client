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

public final class SoundStoppedEvent extends Event {
    public enum Reason { FINISHED, STOPPED, STOLEN, RESET }

    private final SoundDefinition definition;
    private final SoundInstance instance;
    private final Reason reason;

    public SoundStoppedEvent(SoundDefinition definition, SoundInstance instance, Reason reason) {
        this.definition = definition;
        this.instance = instance;
        this.reason = reason;
    }

    public SoundDefinition definition() { return definition; }
    public SoundInstance instance() { return instance; }
    public Reason reason() { return reason; }
}
