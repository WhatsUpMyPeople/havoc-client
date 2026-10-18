/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.sodium.gui;

import havoc.client.compat.sodium.gui.SodiumTooltipAccess;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.gui.widgets.ScrollableTooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ScrollableTooltip.class, remap = false)
public interface SodiumTooltipAccessorMixin extends SodiumTooltipAccess {
    @Accessor("hoveredElement")
    @Override ControlElement havoc$getHoveredElement();
}
