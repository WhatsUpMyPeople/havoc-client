/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.sodium.gui;

import havoc.client.compat.sodium.gui.SodiumFlatButtonAccess;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FlatButtonWidget.class, remap = false)
public interface SodiumFlatButtonAccessorMixin extends SodiumFlatButtonAccess {
    @Accessor("label")
    @Override Component havoc$getLabel();

    @Accessor("selected")
    @Override boolean havoc$isSelected();

    @Accessor("enabled")
    @Override boolean havoc$isEnabled();

    @Accessor("visible")
    @Override boolean havoc$isVisible();

    @Accessor("leftAlign")
    @Override boolean havoc$isLeftAligned();
}
