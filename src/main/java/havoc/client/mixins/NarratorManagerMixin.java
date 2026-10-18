/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import havoc.client.util.NarratorBlocker;

@Mixin(GameNarrator.class)
public class NarratorManagerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "sayChatQueued(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void havoc$muteChat(Component message, CallbackInfo ci) {
        if (shouldMute()) {
            NarratorBlocker.enforce(this.minecraft);
            ci.cancel();
        }
    }

    @Inject(method = "saySystemChatQueued(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void havoc$muteNarrator(Component message, CallbackInfo ci) {
        if (shouldMute()) {
            NarratorBlocker.enforce(this.minecraft);
            ci.cancel();
        }
    }

    @Inject(method = "saySystemQueued(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void havoc$muteSystem(Component message, CallbackInfo ci) {
        if (shouldMute()) {
            NarratorBlocker.enforce(this.minecraft);
            ci.cancel();
        }
    }

    @Inject(method = "saySystemNow(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void havoc$muteImmediateText(Component message, CallbackInfo ci) {
        if (shouldMute()) {
            NarratorBlocker.enforce(this.minecraft);
            ci.cancel();
        }
    }

    @Inject(method = "saySystemNow(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void havoc$muteImmediateString(String message, CallbackInfo ci) {
        if (shouldMute()) {
            NarratorBlocker.enforce(this.minecraft);
            ci.cancel();
        }
    }

    @Inject(method = "narrateMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void havoc$muteSay(String message, boolean interrupt, CallbackInfo ci) {
        if (shouldMute()) {
            NarratorBlocker.enforce(this.minecraft);
            ci.cancel();
        }
    }

    @Inject(method = "updateNarratorStatus", at = @At("HEAD"), cancellable = true)
    private void havoc$blockToggle(NarratorStatus mode, CallbackInfo ci) {
        if (!shouldMute()) return;
        NarratorBlocker.enforce(this.minecraft);
        ci.cancel();
    }

    @Inject(method = "checkStatus", at = @At("HEAD"), cancellable = true)
    private void havoc$skipNarratorLibraryCheck(boolean narratorEnabled, CallbackInfo ci) {
        if (!shouldMute()) return;
        NarratorBlocker.enforce(this.minecraft);
        ci.cancel();
    }

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void havoc$reportInactive(CallbackInfoReturnable<Boolean> cir) {
        if (!shouldMute()) return;
        cir.setReturnValue(false);
    }

    @Unique
    private boolean shouldMute() {
        return NarratorBlocker.isBlocked();
    }
}

