/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ChatComponent.DisplayMode;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import havoc.client.features.command.CommandManager;
import havoc.client.features.gui.chat.BetterChatRenderer;
import havoc.client.features.gui.hud.draggable.impl.BetterChat;
import havoc.client.mixins.accessors.ChatScreenSuggestorAccessor;
import havoc.client.runtime.RuntimeGate;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"), cancellable = true)
    private void havoc$betterChat$render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!RuntimeGate.canRunHud()) return;
        if (BetterChat.isActive()) {
            BetterChat cfg = BetterChat.get();
            if (cfg != null && cfg.hideVanilla()) {
                // Stash suggestion context; BetterChatRenderer will draw it in correct Z-order.
                BetterChatRenderer.setSuggestionContext((ChatScreen) (Object) this, context, mouseX, mouseY);
                ci.cancel();
            }
        }
    }

    @Redirect(
            method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;captureClickableText(Lnet/minecraft/client/gui/ActiveTextCollector;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V"
            )
    )
    private void havoc$betterChat$disableVanillaChatClickHitbox(ChatComponent chatHud,
                                                                    ActiveTextCollector textConsumer,
                                                                    int windowHeight,
                                                                    int currentTick,
                                                                    DisplayMode displayMode) {
        if (havoc$betterChat$shouldHideVanillaChat()) {
            return;
        }
        chatHud.captureClickableText(textConsumer, windowHeight, currentTick, displayMode);
    }

    @Inject(method = "mouseScrolled(DDDD)Z", at = @At("HEAD"), cancellable = true)
    private void havoc$betterChat$disableVanillaChatScroll(double mouseX,
                                                               double mouseY,
                                                               double horizontalAmount,
                                                               double verticalAmount,
                                                               CallbackInfoReturnable<Boolean> cir) {
        if (havoc$betterChat$shouldHideVanillaChat()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"))
    private void havoc$betterChat$allowEscape(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (!RuntimeGate.canRunHud()) return;
        if (!BetterChat.isActive()) return;
        if (!input.isEscape()) return;
        CommandSuggestions suggestor = ((ChatScreenSuggestorAccessor) this).getHavoc$suggestor();
        if (suggestor != null) {
            suggestor.setAllowSuggestions(false);
            suggestor.hide();
        }
    }

    @Inject(method = "handleChatInput(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void havoc$commands$sendMessage(String message, boolean addToHistory, CallbackInfo ci) {
        if (!RuntimeGate.canRunClientLogic()) return;
        if (CommandManager.handle(message)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean havoc$betterChat$shouldHideVanillaChat() {
        if (!RuntimeGate.canRunHud()) return false;
        if (!BetterChat.isActive()) return false;
        BetterChat cfg = BetterChat.get();
        return cfg != null && cfg.hideVanilla();
    }
}
