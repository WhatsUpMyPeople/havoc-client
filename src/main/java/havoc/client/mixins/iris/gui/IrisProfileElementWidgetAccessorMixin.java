/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.iris.gui;

import havoc.client.compat.iris.gui.IrisProfileOptionWidgetAccess;
import net.irisshaders.iris.gui.element.widget.ProfileElementWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ProfileElementWidget.class, remap = false)
public interface IrisProfileElementWidgetAccessorMixin extends IrisProfileOptionWidgetAccess {
    @Accessor("profileLabel")
    @Override Component havoc$getProfileLabel();
}
