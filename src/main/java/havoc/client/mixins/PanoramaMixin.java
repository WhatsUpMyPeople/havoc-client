/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import havoc.client.config.MainConfig;
import havoc.client.features.gui.mainmenu.HavocMainMenuScreen;
import havoc.client.render.engine.postprocess.MenuBackgroundRenderer;
import havoc.client.runtime.RuntimeGate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Panorama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces every vanilla panorama request with the configured Havoc menu background. */
@Mixin(Panorama.class)
public abstract class PanoramaMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void havoc$replaceVanillaPanorama(GuiGraphicsExtractor context,
                                                   int width,
                                                   int height,
                                                   CallbackInfo ci) {
        if (!RuntimeGate.canRunRender()) return;
        if (HavocMainMenuScreen.shouldUseVanillaTitleScreen()) return;
        if (!MainConfig.get().isHavocMainMenuEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        // Do not create PanoramaRenderState and do not enqueue the vanilla panorama overlay at all.
        MenuBackgroundRenderer.renderConfigured(mc);
        ci.cancel();
    }
}
