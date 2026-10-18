/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.map;

import com.mojang.blaze3d.textures.GpuTextureView;

/** Provider-neutral GPU tile payload copied into Havoc-owned atlas storage. */
public record MapTileGpuCopy(GpuTextureView source,
                             int sourceX,
                             int sourceY,
                             int width,
                             int height) {
    public MapTileGpuCopy {
        if (source == null) throw new NullPointerException("source");
        if (sourceX < 0 || sourceY < 0 || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid GPU tile copy region.");
        }
    }

    public int byteSize() {
        return Math.multiplyExact(Math.multiplyExact(width, height), 4);
    }
}
