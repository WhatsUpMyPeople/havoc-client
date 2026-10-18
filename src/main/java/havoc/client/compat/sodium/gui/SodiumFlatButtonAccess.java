/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.compat.sodium.gui;

import net.minecraft.network.chat.Component;

public interface SodiumFlatButtonAccess {
    Component havoc$getLabel();
    boolean havoc$isSelected();
    boolean havoc$isEnabled();
    boolean havoc$isVisible();
    boolean havoc$isLeftAligned();
}
