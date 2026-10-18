/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.player;

import havoc.client.config.values.BooleanValue;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.combat.KillAura;

@ModuleInfo(
        id = "nointeract",
        displayName = "NoInteract",
        category = ModuleCategory.PLAYER, subcategory = ModuleSubcategory.AUTOMATION,
        description = "module.nointeract.description")
public final class NoInteract extends Module {
    private final BooleanValue onlyKillAura = bool("only_kill_aura", true);

    public boolean shouldBlockBlockInteraction() {
        KillAura killAura = Modules.get(KillAura.class);
        return shouldBlockBlockInteraction(
                isEnabled(),
                onlyKillAura.get(),
                killAura != null && killAura.isEnabled()
        );
    }

    static boolean shouldBlockBlockInteraction(
            boolean noInteractEnabled,
            boolean onlyKillAura,
            boolean killAuraEnabled
    ) {
        return noInteractEnabled && (!onlyKillAura || killAuraEnabled);
    }
}
