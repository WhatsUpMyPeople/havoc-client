/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.sound.event;

import havoc.client.events.Event;
import havoc.client.util.sound.SoundDefinition;

public final class SoundErrorEvent extends Event {
    public enum Operation { LOAD, PLAY, CONTROL, RESET }

    private final Operation operation;
    private final SoundDefinition definition;
    private final Throwable error;

    public SoundErrorEvent(Operation operation, SoundDefinition definition, Throwable error) {
        this.operation = operation;
        this.definition = definition;
        this.error = error;
    }

    public Operation operation() { return operation; }
    public SoundDefinition definition() { return definition; }
    public Throwable error() { return error; }
}
