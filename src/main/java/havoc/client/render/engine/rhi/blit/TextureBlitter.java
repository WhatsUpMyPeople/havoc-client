/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rhi.blit;

import com.mojang.blaze3d.textures.GpuTextureView;

public interface TextureBlitter {
    /** Attempts the selected non-shader path and reports whether it was submitted. */
    boolean copy(RhiCopyRequest request);

    /**
     * Returns true if the copy was handled by a backend texture-copy command.
     */
    default boolean copyFast(GpuTextureView src, GpuTextureView dst) {
        return copy(RhiCopyRequest.exact(src, dst));
    }
}
