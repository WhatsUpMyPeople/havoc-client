/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.sodium.gui;

import havoc.client.compat.sodium.gui.SodiumModIconAccess;
import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.widgets.PageListWidget$HeaderEntryWidget", remap = false)
public interface SodiumPageModHeaderIconAccessorMixin extends SodiumModIconAccess {
    @Accessor("icon")
    @Override Identifier havoc$getIcon();

    @Accessor("iconMonochrome")
    @Override boolean havoc$isIconMonochrome();

    @Accessor("modOptions")
    @Override ModOptions havoc$getModOptions();
}
