/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.core.policy;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.jetbrains.annotations.Nullable;

public interface FogProvider {
    FogProvider NONE = () -> null;

    @Nullable GpuBufferSlice fogUniformSlice();
}
