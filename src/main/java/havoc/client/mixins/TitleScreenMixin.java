/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import havoc.client.config.MainConfig;
import havoc.client.util.screen.ClientScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.features.gui.mainmenu.HavocMainMenuScreen;
import havoc.client.runtime.RuntimeGate;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void havoc$replaceTitleScreen(CallbackInfo ci) {
        if (!RuntimeGate.canRunRender()) return;
        if (!MainConfig.get().isHavocMainMenuEnabled()) return;
        if (HavocMainMenuScreen.shouldUseVanillaTitleScreen()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        if (ClientScreen.current() instanceof HavocMainMenuScreen) return;
        ClientScreen.show(mc, new HavocMainMenuScreen());
        ci.cancel();
    }
}




