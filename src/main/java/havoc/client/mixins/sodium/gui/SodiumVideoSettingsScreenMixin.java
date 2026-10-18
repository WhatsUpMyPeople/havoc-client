/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.sodium.gui;

import havoc.client.compat.sodium.gui.SodiumGraphicsGuiRenderer;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptions;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPrompt;
import net.caffeinemc.mods.sodium.client.gui.widgets.DonationButtonWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.KeyBoundButtonWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.OptionListWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.PageListWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.ScrollableTooltip;
import net.caffeinemc.mods.sodium.client.gui.widgets.SearchWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Keeps Sodium's complete widget/input/state graph and replaces only presentation extraction.
 * The hook becomes a no-op in RuntimeGate panic or when the VisualConfig switch is disabled.
 */
@Mixin(value = VideoSettingsScreen.class, remap = false)
public abstract class SodiumVideoSettingsScreenMixin {
    @Unique private boolean havoc$donationSuppressed;
    @Unique private boolean havoc$suppressEscapeRelease;

    @Shadow private PageListWidget pageList;
    @Shadow private SearchWidget searchWidget;
    @Shadow private OptionListWidget optionList;
    @Shadow private KeyBoundButtonWidget applyButton;
    @Shadow private KeyBoundButtonWidget closeButton;
    @Shadow private KeyBoundButtonWidget undoButton;
    @Shadow private List<KeyBoundButtonWidget> shortcutButtons;
    @Shadow private DonationButtonWidget donateButton;
    @Shadow private boolean hasPendingChanges;
    @Shadow @Final private ScrollableTooltip tooltip;
    @Shadow private ScreenPrompt prompt;

    @Shadow
    private void updateControls(int mouseX, int mouseY) {
        throw new AssertionError();
    }

    @Shadow
    private void updateSearchWidgetWidth() {
        throw new AssertionError();
    }

    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private void havoc$guardEscapeReleaseAfterChildReturn(CallbackInfo ci) {
        if (!SodiumGraphicsGuiRenderer.shouldUseModernUi()) return;
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) return;

        // A child screen (notably Iris' ShaderPackScreen) closes on ESC key-press and restores
        // this same Sodium screen before that physical key is released. Sodium normally closes
        // itself from keyReleased(ESC), so without this one-shot guard the return immediately
        // falls through one more screen. Only arm while ESC is physically held during re-init.
        havoc$suppressEscapeRelease = GLFW.glfwGetKey(
                client.getWindow().handle(), GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;
    }

    @Inject(method = "rebuild", at = @At("TAIL"), remap = false)
    private void havoc$removeDonationUi(CallbackInfo ci) {
        if (!SodiumGraphicsGuiRenderer.shouldUseModernUi() || donateButton == null) return;
        donateButton.updateDisplay((VideoSettingsScreen) (Object) this, false);
        havoc$donationSuppressed = true;
        updateSearchWidgetWidth();
    }

    @Inject(method = "openDonationPrompt", at = @At("HEAD"), cancellable = true, remap = false)
    private void havoc$removeDonationPrompt(SodiumOptions options, CallbackInfo ci) {
        if (SodiumGraphicsGuiRenderer.shouldUseModernUi()) ci.cancel();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true, remap = false)
    private void havoc$renderModernSodiumGui(GuiGraphicsExtractor ctx,
                                                  int mouseX,
                                                  int mouseY,
                                                  float delta,
                                                  CallbackInfo ci) {
        if (!SodiumGraphicsGuiRenderer.shouldUseModernUi()) {
            havoc$restoreNativeDonationState();
            return;
        }
        havoc$suppressDonationUi();

        // This side effect is part of Sodium's normal extractRenderState path: it owns pending
        // state, action button enablement and tooltip hover selection. Keep it before presentation.
        updateControls(mouseX, mouseY);

        if (SodiumGraphicsGuiRenderer.render(
                (VideoSettingsScreen) (Object) this,
                ctx,
                mouseX,
                mouseY,
                delta,
                pageList,
                searchWidget,
                optionList,
                applyButton,
                closeButton,
                undoButton,
                shortcutButtons,
                tooltip,
                prompt,
                hasPendingChanges
        )) {
            ci.cancel();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, remap = false)
    private void havoc$modernSearchShortcut(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!SodiumGraphicsGuiRenderer.shouldUseModernUi() || prompt != null || event == null) return;

        // SearchWidget consumes ESC on key-press by clearing/unfocusing itself. Sodium also has
        // an ESC close path on key-release, so remember that this physical key belongs to the
        // search state and consume only its matching release below.
        if (event.key() == GLFW.GLFW_KEY_ESCAPE && searchWidget != null && searchWidget.isSearching()) {
            havoc$suppressEscapeRelease = true;
        }

        boolean control = (event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
        if (control && event.key() == GLFW.GLFW_KEY_F
                && SodiumGraphicsGuiRenderer.focusSearch((VideoSettingsScreen) (Object) this, searchWidget)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true, remap = false)
    private void havoc$consumeOwnedEscapeRelease(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!SodiumGraphicsGuiRenderer.shouldUseModernUi() || event == null) return;
        if (event.key() != GLFW.GLFW_KEY_ESCAPE || !havoc$suppressEscapeRelease) return;

        havoc$suppressEscapeRelease = false;
        cir.setReturnValue(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void havoc$modernMouseClicked(MouseButtonEvent event,
                                               boolean doubleClick,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (!SodiumGraphicsGuiRenderer.shouldUseModernUi()) return;
        if (SodiumGraphicsGuiRenderer.mouseClicked(
                (VideoSettingsScreen) (Object) this,
                event,
                doubleClick,
                pageList,
                searchWidget,
                optionList,
                applyButton,
                closeButton,
                undoButton,
                prompt
        )) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true, remap = false)
    private void havoc$modernMouseScrolled(double mouseX,
                                                double mouseY,
                                                double horizontal,
                                                double vertical,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (!SodiumGraphicsGuiRenderer.shouldUseModernUi()) return;

        // Sodium intentionally binds Ctrl+wheel to its own GUI-scale setting. Preserve that API
        // behavior; the Havoc presentation remains fixed-size regardless of the resulting
        // vanilla scale value.
        if (Minecraft.getInstance().hasControlDown()) return;

        if (SodiumGraphicsGuiRenderer.mouseScrolled(
                (VideoSettingsScreen) (Object) this,
                mouseX,
                mouseY,
                horizontal,
                vertical,
                pageList,
                optionList
        )) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void havoc$suppressDonationUi() {
        if (donateButton == null || havoc$donationSuppressed) return;
        donateButton.updateDisplay((VideoSettingsScreen) (Object) this, false);
        havoc$donationSuppressed = true;
        updateSearchWidgetWidth();
    }

    @Unique
    private void havoc$restoreNativeDonationState() {
        if (donateButton == null || !havoc$donationSuppressed) return;
        boolean visible = !SodiumClientMod.options().notifications.hasClearedDonationButton;
        donateButton.updateDisplay((VideoSettingsScreen) (Object) this, visible);
        havoc$donationSuppressed = false;
        updateSearchWidgetWidth();
    }

}
