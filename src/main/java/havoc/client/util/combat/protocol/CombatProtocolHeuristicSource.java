/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.combat.protocol;

public enum CombatProtocolHeuristicSource {
    MESSAGES("messages"),
    OVERLAY("overlay"),
    BOSSBAR("bossbar");

    private final String key;

    CombatProtocolHeuristicSource(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
