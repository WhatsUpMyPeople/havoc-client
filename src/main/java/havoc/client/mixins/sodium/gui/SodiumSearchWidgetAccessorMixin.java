/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.sodium.gui;

import havoc.client.compat.sodium.gui.SodiumSearchWidgetAccess;
import net.caffeinemc.mods.sodium.client.gui.widgets.SearchWidget;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SearchWidget.class, remap = false)
public interface SodiumSearchWidgetAccessorMixin extends SodiumSearchWidgetAccess {
    @Accessor("query")
    @Override String havoc$getQuery();

    @Accessor("searchBox")
    @Override EditBox havoc$getSearchBox();
}
