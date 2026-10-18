/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.events.impl;

import havoc.client.events.Event;

import java.util.List;

public class CombatProtocolBossbarEvent extends Event {
    public final List<String> names;
    public final long timeMs;

    public CombatProtocolBossbarEvent(List<String> names) {
        this(names, System.currentTimeMillis());
    }

    public CombatProtocolBossbarEvent(List<String> names, long timeMs) {
        this.names = names == null ? List.of() : List.copyOf(names);
        this.timeMs = timeMs;
    }
}
