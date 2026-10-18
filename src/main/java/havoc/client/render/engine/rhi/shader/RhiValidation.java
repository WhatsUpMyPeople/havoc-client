/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.rhi.shader;

import havoc.client.util.logging.DebugLog;
import net.fabricmc.loader.api.FabricLoader;

/** Central switch for expensive/strict RHI contract validation. */
public final class RhiValidation {
    private RhiValidation() {}

    public static boolean enabled() {
        return Boolean.getBoolean("havoc.rhi.validation")
                || FabricLoader.getInstance().isDevelopmentEnvironment()
                || DebugLog.isEnabled();
    }

    public static void doubleDestroy(String resourceKind, String label) {
        if (enabled()) {
            throw new IllegalStateException("Double destroy of " + resourceKind + ": " + label);
        }
    }
}
