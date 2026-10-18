/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;

public final class BlurSource {
    public final GpuTextureView view;
    public final GpuSampler sampler;

    public BlurSource(GpuTextureView view, GpuSampler sampler) {
        this.view = view;
        this.sampler = sampler;
    }
}
