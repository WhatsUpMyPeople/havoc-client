/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import havoc.client.features.module.ModuleManager;
import havoc.client.util.screen.ClientScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.features.gui.clickgui.ClickGuiEditorScreen;
import havoc.client.features.gui.clickgui.ClickGuiPickerScreen;
import havoc.client.features.gui.clickgui.ClickGuiRenderer;
import havoc.client.features.gui.clickgui.ClickGuiScreen;
import havoc.client.features.gui.preview.VisualPreviewRenderer;
import havoc.client.features.gui.preview.VisualPreviewRuntime;
import havoc.client.features.gui.preview.VisualPreviewScreen;
import havoc.client.features.gui.hud.nondraggable.impl.CustomBar;
import havoc.client.features.gui.hud.nondraggable.impl.DynamicIsland;
import havoc.client.features.gui.hud.nondraggable.impl.DebugHudReplacement;
import havoc.client.features.module.modules.misc.ClickGui;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.core.RenderPhase;
import havoc.client.render.engine.core.RenderPhaseScope;
import havoc.client.render.engine.renderer.Renderer2D;

@Mixin(Gui.class)
public abstract class GuiScreenMixin {

    @Unique
    private static void havoc$extractCustomBarHeads(GuiGraphicsExtractor drawContext, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        if (ClientScreen.current() != null) return;

        CustomBar bar = CustomBar.get();
        if (bar == null || !bar.isHudBarEnabled() || !bar.isHotbarXpBarEnabled()) return;
        if (CustomBar.shouldUseJumpBar(mc) || !CustomBar.shouldUseLocator(mc, bar)) return;

        HavocRenderSystem.ensureFrameContext();
        try (RenderPhaseScope ignored = HavocRenderSystem.phase(RenderPhase.SCREEN_TOP, "2d:hud:custom_bar_heads")) {
            Renderer2D.withDeferredLayer(Renderer2D.Deferred2DLayer.AFTER_VANILLA_GUI, () -> {
                CustomBar.renderLocatorHeads(drawContext, tickCounter, mc);
                Renderer2D.COLOR.render();
            });
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void havoc$closeClickGuiOnScreen(Screen screen, CallbackInfo ci) {
        if (!ModuleManager.isEnabled("clickgui")) {
            return;
        }

        // A real replacement screen (especially DeathScreen) must always win over ClickGUI input.
        // Keeping the module alive while waiting for a bind makes MouseMixin consume that screen's
        // clicks, including the respawn button.
        if (ClickGuiRenderer.waitingForKey && screen == null) {
            return;
        }

        if (screen instanceof ClickGuiScreen
                || screen instanceof ClickGuiPickerScreen
                || screen instanceof ClickGuiEditorScreen
                || screen instanceof VisualPreviewScreen) {
            return;
        }

        if (screen != null) {
            ClickGui.setSuppressScreenClose(true);
            try {
                ModuleManager.setEnabled("clickgui", false);
            } finally {
                ClickGui.setSuppressScreenClose(false);
                // Gui#setScreen replaces one non-null screen with another, so vanilla assumes the
                // cursor is already released. ClickGUI's immediate shutdown disables it; restore
                // the normal screen cursor explicitly for DeathScreen and every other GUI.
                ClickGuiRenderer.ensureCursorShown();
            }
        }
    }

    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;applyCursor(Lcom/mojang/blaze3d/platform/Window;)V"
            )
    )
    private void havoc$extractTopLayer(DeltaTracker tickCounter, boolean renderLevel, boolean renderScreens, CallbackInfo ci,
                                           @Local(ordinal = 0) GuiGraphicsExtractor drawContext) {
        if (drawContext == null) {
            return;
        }

        drawContext.nextStratum();
        havoc$extractCustomBarHeads(drawContext, tickCounter);
        VisualPreviewRenderer.renderTopLayer(drawContext, tickCounter.getGameTimeDeltaTicks());
        DynamicIsland.renderScreenOverlay(drawContext, tickCounter.getGameTimeDeltaTicks());
        if (!VisualPreviewRuntime.isActive()) {
            ClickGuiRenderer.renderTopLayer(drawContext, tickCounter.getGameTimeDeltaTicks());
        }
        DebugHudReplacement.renderTopLayer(drawContext);
    }

}
