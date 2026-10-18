/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.deferred;

import havoc.client.render.engine.framegraph.FrameGraphPhysicalResourceDescriptor;

/**
 * Lowers one deferred logical resource into the generic frame-graph physical descriptor contract.
 * Implementations may return texture, volume or buffer descriptors; the frame graph remains the
 * owner regardless of resource kind.
 */
@FunctionalInterface
public interface DeferredPhysicalResourceSpec {
    FrameGraphPhysicalResourceDescriptor descriptor(DeferredResource resource,
                                                     int renderWidth,
                                                     int renderHeight,
                                                     int outputWidth,
                                                     int outputHeight,
                                                     int sceneSamples,
                                                     DeferredRuntimeConfig.Snapshot settings);
}
