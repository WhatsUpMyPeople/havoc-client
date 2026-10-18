/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.gui.clickgui.settings;

public interface TextEditorOwner {
    void applyEditorText(String rawText);

    String getEditorText();

    boolean isSingleLine();
}
