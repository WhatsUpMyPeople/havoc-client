/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.postprocess;

import com.mojang.blaze3d.textures.GpuTextureView;
import havoc.client.render.engine.rhi.HavocRhi;
import havoc.client.render.engine.rhi.shader.RhiStorageImage;
import org.jetbrains.annotations.Nullable;

/** Per-pass execution view exposing optional storage-capable graph endpoints to modern effects. */
public record PostProcessExecutionContext(
        PostProcessContext context,
        HavocRhi rhi,
        GpuTextureView source,
        GpuTextureView destination,
        @Nullable RhiStorageImage sourceStorage,
        @Nullable RhiStorageImage destinationStorage
) {
}
