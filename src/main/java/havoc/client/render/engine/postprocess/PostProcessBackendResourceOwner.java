/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.postprocess;

import havoc.client.render.engine.rhi.HavocRhi;

/** Optional hook for post-process passes that own native backend resources. */
public interface PostProcessBackendResourceOwner {
    void releaseBackendResources(HavocRhi owner);
}
