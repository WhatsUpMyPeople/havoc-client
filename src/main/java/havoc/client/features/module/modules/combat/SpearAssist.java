/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.combat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import havoc.client.config.values.BooleanMapValue;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;

@ModuleInfo(
        id = "spearassist",
        displayName = "SpearAssist",
        category = ModuleCategory.COMBAT, subcategory = ModuleSubcategory.LEGIT,
        description = "module.spearassist.description")
public class SpearAssist extends Module {

    private static final String SETTING_TOGGLES = "toggles";

    private final BooleanMapValue toggles = group(
            "spearassist_toggles",
            SETTING_TOGGLES,
            new java.util.LinkedHashMap<>() {{
                put("respect_cooldown", true);
            }}
    );

    public static boolean isSpear(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && stack.get(DataComponents.KINETIC_WEAPON) != null;
    }

    public boolean respectCooldownEnabled() {
        return toggles.get("respect_cooldown");
    }
}
