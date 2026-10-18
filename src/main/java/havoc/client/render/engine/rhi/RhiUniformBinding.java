/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rhi;

import com.mojang.blaze3d.buffers.GpuBufferSlice;

public record RhiUniformBinding(String name, GpuBufferSlice slice) {
}
