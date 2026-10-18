/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.asset.gltf.gpu;

public record GltfGpuResidencyStats(
        int residentAssets,
        int residentPrimitives,
        int residentTextureVariants,
        long textureGpuBytesEstimate,
        long uploads,
        long cacheHits,
        long invalidations
) {
}
