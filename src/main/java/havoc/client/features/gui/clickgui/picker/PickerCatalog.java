/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.gui.clickgui.picker;

import havoc.client.features.gui.clickgui.settings.TextListSetting;

import java.util.List;

@FunctionalInterface
public interface PickerCatalog {
    List<PickerEntryData> entries(TextListSetting owner);
}
