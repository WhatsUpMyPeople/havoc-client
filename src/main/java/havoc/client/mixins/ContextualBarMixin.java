/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.features.gui.hud.nondraggable.impl.CustomBar;
import havoc.client.runtime.RuntimeGate;

@Mixin(ContextualBar.class)
public interface ContextualBarMixin {

    @Inject(method = "extractExperienceLevel", at = @At("HEAD"), cancellable = true)
    private static void havoc$drawExperienceLevel(GuiGraphicsExtractor context,
                                                      Font textRenderer,
                                                      int level,
                                                      CallbackInfo ci) {
        if (!RuntimeGate.canRunHud()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        CustomBar bar = CustomBar.get();
        if (bar == null) return;
        if (!bar.isHudBarEnabled() || !bar.isHotbarXpBarEnabled()) return;

        ci.cancel();
    }
}

