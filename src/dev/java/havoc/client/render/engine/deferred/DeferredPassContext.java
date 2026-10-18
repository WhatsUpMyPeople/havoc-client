/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.deferred;

import havoc.client.render.engine.core.RenderFrameContext;
import havoc.client.render.engine.rhi.HavocRhi;
import havoc.client.render.engine.rhi.shader.AdvancedShaderBackend;
import havoc.client.render.engine.world.WorldRenderState;

public record DeferredPassContext(
        DeferredStage stage,
        RenderFrameContext frame,
        HavocRhi rhi,
        DeferredResourceBindings resources,
        DeferredSecondaryViewRegistry secondaryViews,
        DeferredPrimaryViewSource primaryView,
        DeferredTemporalHistoryRegistry temporalHistory,
        WorldRenderState worldState,
        DeferredRuntimeConfig.Snapshot settings
) {
    public DeferredPassContext {
        if (temporalHistory == null) temporalHistory = new DeferredTemporalHistoryRegistry();
        if (worldState == null) worldState = WorldRenderState.unknown(0L);
        if (settings == null) settings = DeferredRuntimeConfig.current();
    }

    public AdvancedShaderBackend advancedShaders() {
        return rhi.advancedShaders();
    }

    public boolean featureEnabled(DeferredFeature feature) {
        return DeferredSmokeTestState.global().featureEnabledForFrame(feature, settings);
    }

    public DeferredHistoryDescriptor history() {
        return primaryView.historyDescriptor();
    }

    public DeferredTemporalHistoryDescriptor history(DeferredTemporalHistoryId id) {
        return temporalHistory.descriptor(id);
    }

    /** Persistent history is false here until a successful producer has initialized it. */
    public boolean isValid(DeferredResource resource) {
        return resources.isValid(resource);
    }
}
