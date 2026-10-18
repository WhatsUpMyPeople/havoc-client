/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.compat.sodium.gui;

import net.minecraft.network.chat.FormattedText;

import java.util.List;

public interface SodiumPromptAccess {
    List<FormattedText> havoc$getText();
    int havoc$getWidth();
    int havoc$getHeight();
}
