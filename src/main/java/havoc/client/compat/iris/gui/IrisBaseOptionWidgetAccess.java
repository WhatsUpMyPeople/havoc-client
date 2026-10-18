/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.compat.iris.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface IrisBaseOptionWidgetAccess {
    MutableComponent havoc$getUnmodifiedLabel();
    Component havoc$getValueLabel();
}
