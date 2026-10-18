/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.addon;

import havoc.client.api.v0.addon.HavocAddonRuntimeContext;
import havoc.client.api.v0.client.HavocClientApi;

final class HavocAddonRuntimeContextImpl implements HavocAddonRuntimeContext {
    private final String addonId;

    HavocAddonRuntimeContextImpl(String addonId) {
        this.addonId = addonId;
    }

    @Override
    public String addonId() {
        return addonId;
    }

    @Override
    public HavocClientApi client() {
        return HavocClientApi.get();
    }
}
