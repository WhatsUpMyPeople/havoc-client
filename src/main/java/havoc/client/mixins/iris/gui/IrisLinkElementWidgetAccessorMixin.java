/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.iris.gui;

import havoc.client.compat.iris.gui.IrisLinkElementWidgetAccess;
import net.irisshaders.iris.gui.element.widget.LinkElementWidget;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LinkElementWidget.class, remap = false)
public interface IrisLinkElementWidgetAccessorMixin extends IrisLinkElementWidgetAccess {
    @Accessor(value = "label", remap = false)
    @Override MutableComponent havoc$getLabel();
}
