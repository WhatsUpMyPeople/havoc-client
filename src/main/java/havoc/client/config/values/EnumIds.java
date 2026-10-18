/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.config.values;

import java.util.Locale;

public enum EnumIds {
    ;

    public static String defaultId(Enum<?> value) {
        if (value == null) return "";
        return value.name().toLowerCase(Locale.ROOT);
    }
}
