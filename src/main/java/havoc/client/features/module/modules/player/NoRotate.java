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
        id = "norotate",
        displayName = "NoRotate",
        aliases = {"NoServerRotation"},
        category = ModuleCategory.PLAYER,
        subcategory = ModuleSubcategory.EXPLOIT,
        description = "module.norotate.description")
public final class NoRotate extends Module {
}
