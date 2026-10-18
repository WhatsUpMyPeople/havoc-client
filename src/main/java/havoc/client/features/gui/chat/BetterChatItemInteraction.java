/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.gui.chat;

import havoc.client.features.gui.preview.VisualPreviewScreen;
import havoc.client.features.gui.preview.provider.ItemVisualPreviewProvider;
import net.minecraft.world.item.ItemStack;

public enum BetterChatItemInteraction {
    ;

    public static boolean tryOpenPreview(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        VisualPreviewScreen.open(new ItemVisualPreviewProvider(stack));
        return true;
    }
}
