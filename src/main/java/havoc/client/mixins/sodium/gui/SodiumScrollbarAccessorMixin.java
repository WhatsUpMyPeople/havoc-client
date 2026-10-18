/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.sodium.gui;

import havoc.client.compat.sodium.gui.SodiumScrollbarAccess;
import net.caffeinemc.mods.sodium.client.gui.widgets.ScrollbarWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ScrollbarWidget.class, remap = false)
public interface SodiumScrollbarAccessorMixin extends SodiumScrollbarAccess {
    @Accessor("horizontal")
    @Override boolean havoc$isHorizontal();

    @Accessor("visible")
    @Override int havoc$getVisibleAmount();

    @Accessor("total")
    @Override int havoc$getTotalAmount();

    @Accessor("dragging")
    @Override boolean havoc$isDragging();
}
