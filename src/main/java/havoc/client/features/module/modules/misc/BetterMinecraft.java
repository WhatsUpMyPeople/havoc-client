/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.misc;

import havoc.client.config.values.BooleanValue;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import havoc.client.runtime.RuntimeGate;

@ModuleInfo(
        id = "betterminecraft",
        displayName = "BetterMinecraft",
        category = ModuleCategory.MISC, subcategory = ModuleSubcategory.UTILITY,
        description = "module.betterminecraft.description")
public final class BetterMinecraft extends Module {

    private final BooleanValue hideRecipeBook = bool("hide_recipe_book", true);
    private final BooleanValue tablistRelations = bool("tablist_relations", true);

    public boolean isHideRecipeBookEnabled() {
        return !RuntimeGate.isPanic() && isEnabled() && hideRecipeBook.get();
    }

    public boolean isTablistRelationsEnabled() {
        return !RuntimeGate.isPanic() && isEnabled() && tablistRelations.get();
    }
}
