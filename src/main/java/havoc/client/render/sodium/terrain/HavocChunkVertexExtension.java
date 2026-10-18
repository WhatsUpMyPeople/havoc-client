/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.sodium.terrain;

public interface HavocChunkVertexExtension {
    void havoc$setSurfaceFlags(int surfaceFlags);

    int havoc$getSurfaceFlags();

    void havoc$setMaterialData(int materialId, int mapPresenceMask, int featureMask, int packedScalarSurface);

    int havoc$getMaterialId();

    int havoc$getMaterialMapPresenceMask();

    int havoc$getMaterialFeatureMask();

    int havoc$getPackedScalarSurface();

    void havoc$copyData(HavocChunkVertexExtension dest);
}
