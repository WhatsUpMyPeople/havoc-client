/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.iris.patch;

import havoc.client.util.resources.asset.ResourceAsset;
import havoc.client.util.resources.asset.ResourceCatalog;

/** Shader-patch resources resolved through the central asset metadata registry. */
@ResourceCatalog(namespace = "havoc", root = "shaders/iris-patches")
public enum ShaderPatchResources {
    @ResourceAsset("index.json")
    INDEX
}
