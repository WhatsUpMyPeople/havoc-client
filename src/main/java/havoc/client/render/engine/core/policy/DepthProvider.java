/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.core.policy;

import com.mojang.blaze3d.textures.GpuTextureView;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface DepthProvider {
    DepthProvider NONE = () -> null;

    @Nullable GpuTextureView depthAttachment();
}
