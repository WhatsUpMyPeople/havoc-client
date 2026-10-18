/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.api.v0.addon;

import havoc.client.api.v0.client.HavocClientApi;

public interface HavocAddonRuntimeContext {
    String addonId();

    HavocClientApi client();
}
