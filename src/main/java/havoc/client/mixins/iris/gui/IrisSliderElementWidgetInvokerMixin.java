/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.iris.gui;

import havoc.client.compat.iris.gui.IrisSliderElementWidgetAccess;
import net.irisshaders.iris.gui.element.widget.SliderElementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = SliderElementWidget.class, remap = false)
public interface IrisSliderElementWidgetInvokerMixin extends IrisSliderElementWidgetAccess {
    @Invoker("whileDragging")
    @Override void havoc$whileDragging(int mouseX);

    @Invoker("onReleased")
    @Override void havoc$onReleased();
}
