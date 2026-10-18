/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.deferred;

import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.rhi.HavocRhi;

/** Dev-source-set owner for the experimental deferred renderer and its frame graph. */
public enum DevDeferredRuntime {
    ;
    private static final DeferredWorldPipeline WORLD = new DeferredWorldPipeline();
    private static final DeferredPassGraph GRAPH = new DeferredPassGraph();

    public static DeferredWorldPipeline world() { return WORLD; }
    public static DeferredPassGraph graph() { return GRAPH; }

    public static void serviceFrameBoundary() { WORLD.serviceRuntimeLifecycle(); }

    public static void backendChanged(HavocRhi previous) {
        WORLD.releasePhysicalResources();
        GRAPH.releaseBackendResources(previous);
        WORLD.onBackendChanged();
    }

    public static void shutdown() {
        WORLD.shutdownRuntime();
        GRAPH.releaseBackendResources(HavocRenderSystem.rhi());
    }
}
