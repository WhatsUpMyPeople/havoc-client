/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.config.common.impl;

import havoc.client.config.common.CommonBooleanGroupSchema;
import havoc.client.util.combat.protocol.CombatProtocolHeuristics;

import java.util.Map;

public final class CombatProtocolHeuristicSources implements CommonBooleanGroupSchema {
    @Override
    public String commonI18nKey() {
        return "combat.protocol_heuristics";
    }

    @Override
    public Map<String, Boolean> defaults() {
        return CombatProtocolHeuristics.defaultSourceToggles();
    }
}
