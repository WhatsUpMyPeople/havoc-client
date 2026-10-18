/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.player;

import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;

@ModuleInfo(
        id = "liquidinteract",
        displayName = "LiquidInteract",
        aliases = {"LiquidPlace", "WaterInteract"},
        category = ModuleCategory.PLAYER,
        subcategory = ModuleSubcategory.EXPLOIT,
        description = "module.liquidinteract.description")
public final class LiquidInteract extends Module {
}
