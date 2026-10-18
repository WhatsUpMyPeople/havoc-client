/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.rhi.shader;

/** Resource classes exposed by Havoc-native shader stages. */
public enum ShaderResourceKind {
    STORAGE_BUFFER,
    SAMPLED_TEXTURE,
    SAMPLED_VOLUME,
    STORAGE_IMAGE,
    STORAGE_VOLUME
}
