/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.events.impl;

import lombok.Getter;
import net.minecraft.client.input.KeyEvent;
import havoc.client.events.Event;

@Getter
public final class KeyInputEvent extends Event {
    private final int action;
    private final KeyEvent input;

    public KeyInputEvent(int action, KeyEvent input) {
        this.action = action;
        this.input = input;
    }

}
