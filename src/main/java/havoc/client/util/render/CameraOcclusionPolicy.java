/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.render;

import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.visuals.CameraClip;
import havoc.client.features.module.modules.visuals.FullBright;
import havoc.client.features.module.modules.visuals.NoRender;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Keeps section occlusion from treating the block containing an intentionally
 * unobstructed camera as a closed visibility cell.
 */
public enum CameraOcclusionPolicy {
    ;

    public static boolean shouldDisableSectionOcclusion(Camera camera) {
        if (camera == null) return false;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null) return false;

        BlockState state = minecraft.level.getBlockState(camera.blockPosition());
        if (!state.isSolidRender()) return false;

        CameraClip cameraClip = Modules.get(CameraClip.class);
        if (cameraClip != null && cameraClip.isEnabled()) return true;

        FullBright fullBright = Modules.get(FullBright.class);
        if (fullBright != null && fullBright.isEnabled()) return true;

        NoRender noRender = Modules.get(NoRender.class);
        return noRender != null && noRender.blockOverlayDisabled();
    }
}
