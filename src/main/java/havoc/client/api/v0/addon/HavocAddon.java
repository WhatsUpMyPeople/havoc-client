/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.api.v0.addon;

/**
 * Fabric entrypoint contract for Havoc addons.
 *
 * <p>Declare it in fabric.mod.json under the {@value #ENTRYPOINT_KEY} entrypoint.
 */
public interface HavocAddon {
    String ENTRYPOINT_KEY = "havoc:addon";
    int API_VERSION = 0;

    /**
     * Called before Havoc auto-loads its built-in modules.
     *
     * <p>Use this hook only for startup module exclusions. Normal addon registration
     * still belongs in {@link #onInitialize(HavocAddonContext)}.</p>
     */
    default void onConfigureModules(HavocModuleLoadContext context) {
    }

    void onInitialize(HavocAddonContext context);

    /**
     * Called after all built-in and addon module configs have been loaded and applied.
     * This is the safe startup hook for mutating settings of existing modules.
     */
    default void onClientReady(HavocAddonRuntimeContext context) {
    }

    default void onRuntimeSuspended(HavocAddonRuntimeContext context) {
    }

    default void onRuntimeResumed(HavocAddonRuntimeContext context) {
    }

    default void onShutdown(HavocAddonRuntimeContext context) {
    }
}
