/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.accessors;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public interface SuggestionWindowAccessor {
    @Accessor("rect")
    Rect2i getHavoc$area();

    @Accessor("suggestionList")
    List<?> getHavoc$suggestions();

    @Accessor("current")
    int getHavoc$selection();

    @Accessor("current")
    void setHavoc$selection(int selection);

    @Accessor("offset")
    int getHavoc$inWindowIndex();

    @Accessor("offset")
    void setHavoc$inWindowIndex(int index);
}


