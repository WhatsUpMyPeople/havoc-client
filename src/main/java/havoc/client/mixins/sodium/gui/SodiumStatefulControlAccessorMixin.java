/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.sodium.gui;

import havoc.client.compat.sodium.gui.SodiumStatefulControlAccess;
import net.caffeinemc.mods.sodium.client.gui.options.control.StatefulControlElement;
import net.caffeinemc.mods.sodium.client.gui.widgets.ResetButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StatefulControlElement.class, remap = false)
public interface SodiumStatefulControlAccessorMixin extends SodiumStatefulControlAccess {
    @Accessor("resetButton")
    @Override ResetButton havoc$getResetButton();
}
