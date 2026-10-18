/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import havoc.client.features.command.CommandManager;
import havoc.client.features.theme.Theme;
import havoc.client.features.gui.hud.HudRenderUtil;
import havoc.client.features.theme.Themes;
import havoc.client.features.gui.hud.nondraggable.impl.BetterButtons;
import havoc.client.features.gui.hud.nondraggable.impl.BetterTooltips;
import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.player.PortalScreens;
import havoc.client.features.module.modules.visuals.NoRender;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.animation.AnimationUtility;
import havoc.client.render.engine.core.ViewportContext;
import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.engine.svg.SvgRenderOptions;
import havoc.client.render.engine.core.RenderPhase;
import havoc.client.render.engine.core.RenderPhaseScope;
import havoc.client.util.NarratorBlocker;
import havoc.client.util.proxy.ProxyBackend;
import havoc.client.util.proxy.ProxyEntry;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Unique
    private RenderPhaseScope havoc$screenPhaseScope;

    @Inject(method = "defaultHandleGameClickEvent", at = @At("HEAD"), cancellable = true)
    private static void havoc$runClientChatCommand(ClickEvent event,
                                                       Minecraft client,
                                                       Screen screen,
                                                       CallbackInfo ci) {
        if (!(event instanceof ClickEvent.RunCommand run)) return;
        String command = run.command();
        if (command == null || !command.startsWith("@")) return;
        if (CommandManager.handle(command)) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldRunNarration", at = @At("HEAD"), cancellable = true)
    private void havoc$disableNarratorUi(CallbackInfoReturnable<Boolean> cir) {
        if (!NarratorBlocker.isBlocked()) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"), cancellable = true)
    private void havoc$noRender$cancelDarkening(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        NoRender module = Modules.get(NoRender.class);
        Minecraft mc = Minecraft.getInstance();
        if (module != null && mc != null && mc.level != null && module.screenDarkeningDisabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTransparentBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("HEAD"), cancellable = true)
    private void havoc$noRender$cancelInGameBackground(GuiGraphicsExtractor context, CallbackInfo ci) {
        NoRender module = Modules.get(NoRender.class);
        Minecraft mc = Minecraft.getInstance();
        if (module != null && mc != null && mc.level != null && module.screenDarkeningDisabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"))
    private void havoc$beginButtonQueue(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BetterTooltips.beginTooltipFrame();
        BetterTooltips.setDrawContext(ctx);
        BetterButtons.beginFrame(ctx);
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"))
    private void havoc$phaseScreenHead(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        String label = "2d:screen:" + self.getClass().getSimpleName();
        HavocRenderSystem.ensureFrameContext();
        havoc$screenPhaseScope = HavocRenderSystem.phase(RenderPhase.SCREEN, label);
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("RETURN"))
    private void havoc$phaseScreenTail(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        String label = "2d:screen:" + self.getClass().getSimpleName();
        if (havoc$screenPhaseScope != null) {
            havoc$screenPhaseScope.close();
            havoc$screenPhaseScope = null;
        }
    }

@Unique
    private static final float HAVOC_PROXY_BUTTON_X = 8f;
    @Unique
    private static final float HAVOC_PROXY_BUTTON_Y = 8f;
    @Unique
    private static final float HAVOC_PROXY_BUTTON_W = 24f;
    @Unique
    private static final float HAVOC_PROXY_BUTTON_H = 24f;
    @Unique
    private float havoc$proxyHover;
    @Unique
    private float havoc$proxyPress;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("RETURN"))
    private void havoc$renderProxyEntry(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof JoinMultiplayerScreen)) return;
        float dt = AnimationUtility.deltaTime();
        boolean hovered = havoc$inside(mouseX, mouseY, HAVOC_PROXY_BUTTON_X, HAVOC_PROXY_BUTTON_Y,
                HAVOC_PROXY_BUTTON_W, HAVOC_PROXY_BUTTON_H);
        havoc$proxyHover = havoc$animate(havoc$proxyHover, hovered ? 1f : 0f, dt, 11f);
        havoc$proxyPress = havoc$animate(havoc$proxyPress, 0f, dt, 8f);

        ViewportContext.beginScaled(context);
        Renderer2D.COLOR.begin();
        try {
            havoc$drawProxyButton();
        } finally {
            Renderer2D.COLOR.render();
            ViewportContext.end(context);
        }
    }

    @Unique
    private void havoc$drawProxyButton() {
        float hover = AnimationUtility.easeOutCubic(havoc$proxyHover);
        float press = AnimationUtility.easeOutCubic(havoc$proxyPress);
        Themes.Theme theme = Theme.theme();
        boolean enabled = ProxyBackend.isEnabled();
        ProxyEntry active = ProxyBackend.activeProxy();
        boolean configured = active.isConfigured();

        float size = HAVOC_PROXY_BUTTON_W * (1.0f + hover * 0.035f - press * 0.025f);
        float x = HAVOC_PROXY_BUTTON_X + (HAVOC_PROXY_BUTTON_W - size) * 0.5f;
        float y = HAVOC_PROXY_BUTTON_Y + (HAVOC_PROXY_BUTTON_H - size) * 0.5f - hover * 0.35f;
        float radius = 5.0f;
        float centerX = x + size * 0.5f;
        float centerY = y + size * 0.5f;
        int accent = enabled && configured ? theme.accent() : 0x8A94A3;
        int baseTop = enabled && configured ? 0x222A20 : 0x1B2028;
        int baseBottom = enabled && configured ? 0x121812 : 0x10141B;
        int fillTop = HudRenderUtil.mixColor(havoc$withAlpha(baseTop, 226), havoc$withAlpha(accent, 96), (enabled && configured ? 0.18f : 0.06f) + hover * 0.12f);
        int fillBottom = HudRenderUtil.mixColor(havoc$withAlpha(baseBottom, 236), havoc$withAlpha(accent, 70), (enabled && configured ? 0.12f : 0.04f) + hover * 0.08f);
        int stroke = HudRenderUtil.mixColor(havoc$withAlpha(0x46505D, 180), havoc$withAlpha(accent, 218), (enabled && configured ? 0.38f : 0.10f) + hover * 0.35f);
        int iconColor = enabled && configured
                ? HudRenderUtil.mixColor(havoc$withAlpha(0xFFFFFF, 245), havoc$withAlpha(theme.accent(), 255), 0.18f)
                : havoc$withAlpha(0xC5CCD6, 235);

        Renderer2D.COLOR.roundedRectSoftShadow(x, y + 0.8f, size, size, radius,
                7.5f, 0.026f + hover * 0.012f, havoc$withAlpha(0x060810, 96));
        Renderer2D.COLOR.roundedRectGradientQuad(x, y, size, size, radius, 1f,
                fillTop, fillTop, fillBottom, fillBottom);
        Renderer2D.COLOR.roundedRectStrokeGradient(x + 0.5f, y + 0.5f, size - 1.0f, size - 1.0f,
                radius - 0.5f, 1f, 0.55f, HudRenderUtil.mixColor(stroke, 0xFFFFFFFF, 0.08f), stroke, 90f);
        Renderer2D.COLOR.svg("shield-user", centerX - 5.6f, centerY - 5.6f, 11.2f, 11.2f, SvgRenderOptions.overrideColor(iconColor));
    }

    @Unique
    private static float havoc$animate(float current, float target, float dt, float speed) {
        float next = AnimationUtility.approach(current, target, dt, speed);
        return AnimationUtility.snap(next, target, 0.001f);
    }

    @Unique
    private static boolean havoc$inside(float mx, float my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Unique
    private static int havoc$withAlpha(int rgb, int alpha) {
        return (rgb & 0x00FFFFFF) | (Mth.clamp(alpha, 0, 255) << 24);
    }
    @Inject(method = "isAllowedInPortal", at = @At("HEAD"), cancellable = true)
    private void havoc$portalScreens$allowInPortal(CallbackInfoReturnable<Boolean> cir) {
        PortalScreens module = Modules.get(PortalScreens.class);
        if (module != null && module.isEnabled()) {
            cir.setReturnValue(true);
        }
    }

}
