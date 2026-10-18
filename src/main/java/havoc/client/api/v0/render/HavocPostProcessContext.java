/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.api.v0.render;

import com.mojang.blaze3d.textures.GpuTextureView;
import havoc.client.render.engine.postprocess.PostProcessPass;
import net.minecraft.client.Minecraft;

public record HavocPostProcessContext(
        String addonId,
        String passId,
        PostProcessPass.Phase phase,
        GpuTextureView source,
        GpuTextureView destination,
        float tickDelta
) {
    public Minecraft client() {
        return Minecraft.getInstance();
    }
}
