/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Portions of this file are adapted from GFBS-glTF 1.5.1
 * (commit 8906d4083c23b5d5bbf5191a0555d149a5272900).
 * Copyright (c) 2026 LytharaLab. Original portions are licensed under the MIT License.
 * See THIRD_PARTY_LICENSES/GFBS-glTF-MIT.txt.
 *
 * Havoc modifications are distributed under GNU GPL v3.0 as part of Havoc.
 */
package havoc.client.render.engine.asset.gltf.io;

import net.minecraft.resources.Identifier;
import havoc.client.render.engine.asset.gltf.io.GltfResolver;
import havoc.client.render.engine.asset.gltf.io.ModelImporter;
import havoc.client.render.engine.asset.gltf.model.GltfAsset;

import java.io.IOException;
import java.util.List;

public final class GltfAssetImporter implements ModelImporter {
    @Override
    public List<String> extensions() {
        return List.of("gltf", "glb");
    }

    @Override
    public GltfAsset load(Identifier source, GltfResolver resolver) throws IOException {
        return new JgltfAssetLoader().load(source, resolver::open);
    }
}
