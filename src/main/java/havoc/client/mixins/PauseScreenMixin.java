/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.combat.PvpCooldowns;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
    @Shadow
    @Nullable
    private Button disconnectButton;

    @Unique
    private Button havoc$lastExitButton;
    @Unique
    private boolean havoc$disconnectBlocked;
    @Unique
    private boolean havoc$previousExitActive = true;

    @Inject(method = "init", at = @At("TAIL"))
    private void havoc$blockDisconnectOnInit(CallbackInfo ci) {
        havoc$syncDisconnectButton();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void havoc$blockDisconnectOnRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        havoc$syncDisconnectButton();
    }

    @Unique
    private void havoc$syncDisconnectButton() {
        Button button = disconnectButton;
        if (button == null) {
            havoc$lastExitButton = null;
            havoc$disconnectBlocked = false;
            return;
        }

        if (button != havoc$lastExitButton) {
            havoc$lastExitButton = button;
            havoc$disconnectBlocked = false;
            havoc$previousExitActive = button.active;
        }

        PvpCooldowns cooldowns = Modules.get(PvpCooldowns.class);
        boolean shouldBlock = cooldowns != null && cooldowns.shouldBlockDisconnect();
        if (shouldBlock) {
            if (!havoc$disconnectBlocked) {
                havoc$previousExitActive = button.active;
            }
            havoc$disconnectBlocked = true;
            button.active = false;
            button.setTooltip(Tooltip.create(cooldowns.getDisconnectBlockTooltip()));
            return;
        }

        if (havoc$disconnectBlocked) {
            button.active = havoc$previousExitActive;
            button.setTooltip(null);
            havoc$disconnectBlocked = false;
        }
    }
}
