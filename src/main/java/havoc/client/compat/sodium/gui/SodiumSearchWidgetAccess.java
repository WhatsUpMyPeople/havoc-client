/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.compat.sodium.gui;

import net.minecraft.client.gui.components.EditBox;

public interface SodiumSearchWidgetAccess {
    String havoc$getQuery();

    EditBox havoc$getSearchBox();
}
