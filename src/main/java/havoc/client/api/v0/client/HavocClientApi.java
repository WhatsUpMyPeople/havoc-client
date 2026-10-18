/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.api.v0.client;

import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleManager;

import java.util.List;

public final class HavocClientApi {
    private static final HavocClientApi INSTANCE = new HavocClientApi();

    private HavocClientApi() {
    }

    public static HavocClientApi get() {
        return INSTANCE;
    }

    public List<Module> modules() {
        return ModuleManager.getModules();
    }

    public Module module(String id) {
        return ModuleManager.get(id);
    }

    public boolean isModuleEnabled(String id) {
        return ModuleManager.isEnabled(id);
    }

    public void setModuleEnabled(String id, boolean enabled) {
        ModuleManager.setEnabled(id, enabled);
    }
}
