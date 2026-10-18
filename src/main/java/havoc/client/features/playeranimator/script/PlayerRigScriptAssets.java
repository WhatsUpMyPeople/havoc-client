/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.playeranimator.script;

import havoc.client.util.resources.asset.ScriptAsset;
import havoc.client.util.resources.asset.ScriptCatalog;

/** Declarative player-animation script stack discovered by {@code AssetAutoLoader}. */
@ScriptCatalog(namespace = "havoc", root = "playeranimator")
public enum PlayerRigScriptAssets {
    @ScriptAsset(value = "player_animation_library.js", order = 0)
    LIBRARY,

    @ScriptAsset(value = "animations", tree = true, order = 100)
    ANIMATIONS,

    @ScriptAsset(value = "rig", tree = true, order = 200)
    RIG,

    @ScriptAsset(value = "player_rig.js", addon = "player_rig_addon.js", order = 300)
    ENTRY
}
