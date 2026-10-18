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

/**
 * Keeps items in the 2x2 crafting grid or cursor when closing inventory (no drops).
 */
@ModuleInfo(
        id = "xcarry",
        displayName = "XCarry",
        category = ModuleCategory.PLAYER, subcategory = ModuleSubcategory.EXPLOIT,
        description = "module.xcarry.description")
public class XCarry extends Module {
}
