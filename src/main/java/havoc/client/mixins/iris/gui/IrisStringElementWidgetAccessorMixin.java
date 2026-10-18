/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.iris.gui;

import havoc.client.compat.iris.gui.IrisStringOptionWidgetAccess;
import net.irisshaders.iris.gui.element.widget.StringElementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StringElementWidget.class, remap = false)
public interface IrisStringElementWidgetAccessorMixin extends IrisStringOptionWidgetAccess {
    @Accessor("valueIndex")
    @Override int havoc$getValueIndex();

    @Accessor("valueCount")
    @Override int havoc$getValueCount();
}
