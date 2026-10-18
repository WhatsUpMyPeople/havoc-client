/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.accessors;

import net.minecraft.client.gui.components.CycleButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(CycleButton.class)
public interface CyclingButtonWidgetAccessor<T> {
    @Accessor("spriteSupplier")
    CycleButton.SpriteSupplier<T> havoc$getIcon();

    @Accessor("displayState")
    CycleButton.DisplayState havoc$getLabelType();

    @Accessor("valueStringifier")
    Function<T, net.minecraft.network.chat.Component> havoc$getValueToText();
}


