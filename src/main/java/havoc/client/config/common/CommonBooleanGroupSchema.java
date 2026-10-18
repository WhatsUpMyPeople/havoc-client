/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.config.common;

import java.util.Map;

public interface CommonBooleanGroupSchema extends CommonSettingSchema {
    Map<String, Boolean> defaults();
}