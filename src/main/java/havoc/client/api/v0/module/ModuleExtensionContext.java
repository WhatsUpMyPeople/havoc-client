/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.api.v0.module;

import havoc.client.config.SettingDef;
import havoc.client.config.values.ConfigValue;
import havoc.client.features.module.Module;

public interface ModuleExtensionContext {
    String addonId();

    String moduleId();

    Module module();

    void addSetting(SettingDef setting);

    ConfigValue<?> getConfigValue(String configName);

    boolean addModeOption(String configName, String option);
}
