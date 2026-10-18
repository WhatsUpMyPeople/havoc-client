/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.compat.sodium.gui;

import net.minecraft.network.chat.Component;

public interface SodiumCenteredWidgetAccess {
    Component havoc$getLabel();
    Component havoc$getSubtitle();
    boolean havoc$isSelected();
    boolean havoc$isEnabled();
    boolean havoc$isVisible();
}
