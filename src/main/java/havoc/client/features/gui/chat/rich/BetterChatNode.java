/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.gui.chat.rich;

/**
 * One independently laid out node in a BetterChat message.
 *
 * <p>Nodes are intentionally data-only. Input policy and rendering live at the
 * BetterChat surface, so message producers never depend on renderer internals.</p>
 */
public sealed interface BetterChatNode permits TextNode, ItemNode {
    /** Accessible/searchable representation of this node. */
    String plainText();
}
