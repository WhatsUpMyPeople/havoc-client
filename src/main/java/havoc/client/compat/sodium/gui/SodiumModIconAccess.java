/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.compat.sodium.gui;

import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.minecraft.resources.Identifier;

public interface SodiumModIconAccess {
    Identifier havoc$getIcon();
    boolean havoc$isIconMonochrome();
    ModOptions havoc$getModOptions();
}
