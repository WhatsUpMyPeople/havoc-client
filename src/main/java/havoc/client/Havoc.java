/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client;

import havoc.client.features.map.heuristic.HeuristicRuntime;
import havoc.client.events.UsedImplicitly;
import havoc.client.compat.xaero.XaeroWaypointHudOverlay;
import havoc.client.render.map.PlayerLocationHudOverlay;
import havoc.client.events.impl.GameTickEvent;
import havoc.client.runtime.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import havoc.client.runtime.annotation.ClientBound;
import havoc.client.runtime.annotation.ClientBoundLevel;
import havoc.client.runtime.annotation.RuntimeAssertion;
import havoc.client.runtime.annotation.RuntimeAssertionPhase;
import havoc.client.runtime.annotation.RuntimeResume;
import havoc.client.runtime.annotation.RuntimeSuspend;
import havoc.client.runtime.nativeguard.NativeMemoryGuard;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import havoc.client.addon.AddonManager;
import havoc.client.addon.AddonRenderPipelineManager;
import havoc.client.api.v0.render.HavocRenderStage;
import havoc.client.config.MainConfig;
import havoc.client.events.Events;
import havoc.client.features.account.AccountConfig;
import havoc.client.features.command.CommandManager;
import havoc.client.features.command.VanillaChatRuntimeCleanup;
import havoc.client.features.gui.clickgui.ClickGuiRenderer;
import havoc.client.features.gui.clickgui.settings.I18nPreflightContributors;
import havoc.client.features.gui.hud.HudElements;
import havoc.client.features.gui.hud.HudRenderSpace;
import havoc.client.features.gui.hud.draggable.DraggableHudElementRegistry;
import havoc.client.features.gui.hud.nondraggable.StaticHudElementBootstrap;
import havoc.client.features.gui.hud.nondraggable.StaticHudElementRegistry;
import havoc.client.features.gui.hud.nondraggable.impl.SwapTooltip;
import havoc.client.features.module.HudPhase;
import havoc.client.features.module.ModuleAutoLoader;
import havoc.client.features.module.ModuleManager;
import havoc.client.features.module.WorldPhase;
import havoc.client.mixininterface.IGuiGraphics;
import havoc.client.render.effects.NetherPortalRiftPass;
import havoc.client.render.effects.SleepOverlayPass;
import havoc.client.render.effects.mask.WorldPostProcessMasks;
import havoc.client.render.effects.lens.WorldTargetLenses;
import havoc.client.render.effects.orbit.WorldOrbitGravity;
import havoc.client.render.effects.shockwave.WorldJumpShockwaves;
import havoc.client.render.effects.shockwave.WorldBlastShockwaves;
import havoc.client.render.engine.HavocRenderEngineBootstrap;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.core.RenderPhase;
import havoc.client.render.engine.core.RenderPhaseScope;
import havoc.client.render.engine.core.ViewportContext;
import havoc.client.render.engine.postprocess.PostProcessManager;
import havoc.client.render.engine.profiler.ProfilerPhase;
import havoc.client.render.engine.profiler.RenderProfiler2D;
import havoc.client.render.engine.renderer.FullScreenRenderer;
import havoc.client.render.engine.prewarm.RenderPrewarmContributors;
import havoc.client.render.engine.prewarm.RenderPrewarmManager;
import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.engine.text.TextRenderer;
import havoc.client.render.helpers.SystemCursor;
import havoc.client.render.helpers.TickDelta;
import havoc.client.util.aiming.RotationManager;
import havoc.client.util.block.bed.SelfBedTracker;
import havoc.client.util.click.AttackPressing;
import havoc.client.util.combat.AntiBotTracker;
import havoc.client.util.combat.protocol.CombatProtocolHeuristics;
import havoc.client.util.combat.SprintController;
import havoc.client.util.combat.VulcanReachController;
import havoc.client.util.entity.simulation.BoatSimulationCache;
import havoc.client.render.engine.renderer.ui.runtime.script.JavetRuntimeBootstrap;
import havoc.client.util.media.MediaSessionService;
import havoc.client.features.maplink.runtime.MapLinkRuntime;
import havoc.client.features.map.runtime.MapLocationRuntime;
import havoc.client.util.network.BacktrackController;
import havoc.client.util.network.BlinkManager;
import havoc.client.util.proxy.ProxyBackend;
import havoc.client.util.network.FakeLagController;
import havoc.client.util.player.inventory.InventorySwap;
import havoc.client.util.player.simulation.PlayerSimulationCache;
import havoc.client.util.pvp.PvpTargetTracker;
import havoc.client.util.pvp.PvpTracker;
import havoc.client.util.session.SessionStatisticsTracker;
import havoc.client.util.target.TargetManager;
import havoc.client.util.time.TimerController;

@ClientBound(
        value = ClientBoundLevel.FULL,
        packages = "havoc.client",
        resources = "assets/havoc",
        exposedPackages = "havoc.client.mixins",
        isolatedEntrypoints = "havoc.client.runtime.isolated.HavocIsolatedRuntime",
        isolatedPackages = "havoc.client.runtime.isolated"
)
public class Havoc implements ClientModInitializer {

    @UsedImplicitly
    @RuntimeSuspend(order = 100)
    private void suspendClientBoundRuntime(String reason) {
        ClientRuntime.enterSoftPanic(reason);
    }

    @UsedImplicitly
    @RuntimeResume(order = 100)
    private void resumeClientBoundRuntime(String reason) {
        ClientRuntime.exitSoftPanic(reason);
    }

    @UsedImplicitly
    @RuntimeAssertion(
            phase = RuntimeAssertionPhase.SUSPENDED,
            message = "Havoc runtime did not enter soft panic"
    )
    private boolean assertClientBoundRuntimeSuspended() {
        return ClientRuntime.state() == ClientRuntimeState.SOFT_PANIC;
    }

    @UsedImplicitly
    @RuntimeAssertion(
            phase = RuntimeAssertionPhase.ACTIVE,
            message = "Havoc runtime did not resume"
    )
    private boolean assertClientBoundRuntimeActive() {
        return ClientRuntime.state() == ClientRuntimeState.ACTIVE;
    }

    private static void registerRuntimeLifecycle() {
        AddonManager.registerRuntimeLifecycle();
        ClientRuntime.setCallbackCounter(Events.BUS::listenerCount);
        ClientRuntime.setModuleCounter(ModuleManager::registeredCount);

        ClientRuntime.registerParticipant(new RuntimeShutdownParticipant() {
            @Override
            public String id() {
                return "modules";
            }

            @Override
            public void shutdown(RuntimeShutdownContext context) {
                ModuleManager.shutdownForRuntime(context);
            }
        });
        ClientRuntime.registerResumeParticipant(new RuntimeResumeParticipant() {
            @Override
            public String id() {
                return "modules";
            }

            @Override
            public void resume(String reason) {
                ModuleManager.restoreAfterSoftPanic(reason);
            }
        });
        ClientRuntime.registerParticipant(new RuntimeShutdownParticipant() {
            @Override
            public String id() {
                return "clickgui";
            }

            @Override
            public void shutdown(RuntimeShutdownContext context) {
                ClickGuiRenderer.shutdownForRuntime();
            }
        });
        ClientRuntime.registerParticipant(new RuntimeShutdownParticipant() {
            @Override
            public String id() {
                return "map-duplex";
            }

            @Override
            public void shutdown(RuntimeShutdownContext context) {
                MapLocationRuntime.get().suspend();
                context.increment("duplexStopped", 1);
            }
        });
        ClientRuntime.registerParticipant(new RuntimeShutdownParticipant() {
            @Override
            public String id() {
                return "vanilla_chat";
            }

            @Override
            public void shutdown(RuntimeShutdownContext context) {
                VanillaChatRuntimeCleanup.clearVanillaChatCache();
                context.increment("vanillaChatCleared", 1);
            }
        });
        ClientRuntime.registerParticipant(new RuntimeShutdownParticipant() {
            @Override
            public String id() {
                return "hud";
            }

            @Override
            public void shutdown(RuntimeShutdownContext context) {
                if (context.jarReplacement()) {
                    DraggableHudElementRegistry.clearRuntimeReferences();
                    StaticHudElementRegistry.clearRuntimeReferences();
                }
            }
        });
        ClientRuntime.registerParticipant(new RuntimeShutdownParticipant() {
            @Override
            public String id() {
                return "render";
            }

            @Override
            public void shutdown(RuntimeShutdownContext context) {
                if (context.jarReplacement()) {
                    PostProcessManager.shutdownForRuntime();
                    HavocRenderSystem.shutdown();
                }
            }
        });
    }

    private static void renderWorldPhase(WorldPhase phase,
                                         com.mojang.blaze3d.vertex.PoseStack matrices,
                                         net.minecraft.client.renderer.SubmitNodeCollector consumers) {
        if (!RuntimeGate.canRunRender()) return;

        ModuleManager.renderWorld(
                phase,
                matrices,
                consumers,
                TickDelta.tickProgress(false)
        );
    }

    private static void refreshHudFrameTiming(DeltaTracker tickCounter) {
        HavocRenderSystem.updateFrameTiming(
                TickDelta.tickProgress(tickCounter, false),
                TickDelta.frameDeltaTicks(tickCounter),
                TickDelta.fixedDeltaTicks(tickCounter)
        );
    }

    private static void renderHudPhase(HudPhase phase, GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (ClickGuiRenderer.suppressesBackgroundRendering()) return;
        if (!RuntimeGate.canRunHud()) return;
        float tickProgress = TickDelta.tickProgress(tickCounter, false);
        ModuleManager.renderHud(phase, ctx, tickProgress);
        Renderer2D.Deferred2DLayer layer = deferredLayerForHudPhase(phase);
        if (renderHudEngine(phase, layer, ctx, tickCounter)) {
            appendHudDeferredMarker(layer, ctx);
            DraggableHudElementRegistry.renderNativeHudOverlays(phase, ctx);
        }
    }

    private static boolean renderHudEngine(HudPhase phase,
                                           Renderer2D.Deferred2DLayer layer,
                                           GuiGraphicsExtractor ctx,
                                           DeltaTracker tickCounter) {
        boolean rawMain = hasHudMainPassWork(phase, HudRenderSpace.UNSCALED)
                || AddonRenderPipelineManager.hasActiveCallbacks(HavocRenderStage.HUD_RAW);
        boolean scaledMain = hasHudMainPassWork(phase, HudRenderSpace.SCALED)
                || AddonRenderPipelineManager.hasActiveCallbacks(HavocRenderStage.HUD_SCALED);
        boolean xaeroWaypointHudWork = hasXaeroWaypointHudWork(phase);
        boolean playerLocationHudWork = hasPlayerLocationHudWork(phase);
        boolean logicalMain = hasHudMainPassWork(phase, HudRenderSpace.UNSCALED_LOGICAL)
                || AddonRenderPipelineManager.hasActiveCallbacks(HavocRenderStage.HUD_LOGICAL)
                || xaeroWaypointHudWork
                || playerLocationHudWork;
        boolean rawForeground = hasHudForegroundPassWork(phase, HudRenderSpace.UNSCALED)
                || AddonRenderPipelineManager.hasActiveCallbacks(HavocRenderStage.HUD_RAW_FOREGROUND);
        boolean scaledForeground = hasHudForegroundPassWork(phase, HudRenderSpace.SCALED)
                || AddonRenderPipelineManager.hasActiveCallbacks(HavocRenderStage.HUD_SCALED_FOREGROUND);
        boolean logicalForeground = hasHudForegroundPassWork(phase, HudRenderSpace.UNSCALED_LOGICAL)
                || AddonRenderPipelineManager.hasActiveCallbacks(HavocRenderStage.HUD_LOGICAL_FOREGROUND)
                || xaeroWaypointHudWork
                || playerLocationHudWork;
        boolean moduleRawMain = ModuleManager.hasHudEngineWork(phase, HudRenderSpace.UNSCALED);
        boolean moduleScaledMain = ModuleManager.hasHudEngineWork(phase, HudRenderSpace.SCALED);
        boolean moduleLogicalMain = ModuleManager.hasHudEngineWork(phase, HudRenderSpace.UNSCALED_LOGICAL);
        boolean moduleRawForeground = ModuleManager.hasHudEngineForegroundWork(phase, HudRenderSpace.UNSCALED);
        boolean moduleScaledForeground = ModuleManager.hasHudEngineForegroundWork(phase, HudRenderSpace.SCALED);
        boolean moduleLogicalForeground = ModuleManager.hasHudEngineForegroundWork(phase, HudRenderSpace.UNSCALED_LOGICAL);
        if (!rawMain && !scaledMain && !logicalMain && !rawForeground && !scaledForeground && !logicalForeground) {
            return false;
        }

        float tickProgress = TickDelta.tickProgress(tickCounter, false);
        TextRenderer textRenderer = TextRenderer.get();

        FullScreenRenderer.ensureInit();
        ViewportContext.beginUnscaled(ctx);
        refreshHudFrameTiming(tickCounter);

        Renderer2D.withDeferredLayer(layer, () -> {
            SystemCursor.beginFrame(ctx);
            try (RenderPhaseScope hudPhase = HavocRenderSystem.phase(RenderPhase.HUD_MAIN, "2d:phase:" + phase.name())) {
                // Raw HUD pass
                if (rawMain) {
                    if (moduleRawMain) {
                        Renderer2D.withHudBackdropContribution(() -> {
                            Renderer2D.COLOR.begin();
                            ModuleManager.renderHudEngine(phase, HudRenderSpace.UNSCALED, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                            Renderer2D.COLOR.render();
                        });
                    }
                    Renderer2D.COLOR.begin();
                    StaticHudElementRegistry.renderAllEngine(phase, HudRenderSpace.UNSCALED, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    AddonRenderPipelineManager.render2D(HavocRenderStage.HUD_RAW, phase, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    Renderer2D.COLOR.render();
                }

                // Scaled fixed HUD pass
                if (scaledMain) {
                    ViewportContext.beginScaled(ctx);
                    if (moduleScaledMain) {
                        Renderer2D.withHudBackdropContribution(() -> {
                            Renderer2D.COLOR.begin();
                            ModuleManager.renderHudEngine(phase, HudRenderSpace.SCALED, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                            Renderer2D.COLOR.render();
                        });
                    }
                    Renderer2D.COLOR.begin();
                    StaticHudElementRegistry.renderAllEngine(phase, HudRenderSpace.SCALED, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    AddonRenderPipelineManager.render2D(HavocRenderStage.HUD_SCALED, phase, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    Renderer2D.COLOR.render();
                }

                // Logical HUD pass
                if (logicalMain) {
                    ViewportContext.beginUnscaledLogical(ctx);
                    if (moduleLogicalMain) {
                        Renderer2D.withHudBackdropContribution(() -> {
                            Renderer2D.COLOR.begin();
                            ModuleManager.renderHudEngine(phase, HudRenderSpace.UNSCALED_LOGICAL, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                            Renderer2D.COLOR.render();
                        });
                    }
                    Renderer2D.COLOR.begin();
                    StaticHudElementRegistry.renderAllEngine(phase, HudRenderSpace.UNSCALED_LOGICAL, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    DraggableHudElementRegistry.renderAllEngine(phase, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    if (xaeroWaypointHudWork) {
                        XaeroWaypointHudOverlay.renderBackground(Renderer2D.COLOR, textRenderer, tickProgress);
                    }
                    if (playerLocationHudWork) {
                        PlayerLocationHudOverlay.renderBackground(Renderer2D.COLOR, textRenderer, tickProgress);
                    }
                    AddonRenderPipelineManager.render2D(HavocRenderStage.HUD_LOGICAL, phase, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    Renderer2D.COLOR.render();
                }

                // Raw foreground
                if (rawForeground) {
                    ViewportContext.beginUnscaled(ctx);
                    if (moduleRawForeground) {
                        Renderer2D.withHudBackdropContribution(() -> {
                            Renderer2D.COLOR.begin();
                            ModuleManager.renderHudEngineForeground(phase, HudRenderSpace.UNSCALED, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                            Renderer2D.COLOR.render();
                        });
                    }
                    Renderer2D.COLOR.begin();
                    StaticHudElementRegistry.renderAllEngineForeground(phase, HudRenderSpace.UNSCALED, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    AddonRenderPipelineManager.render2D(HavocRenderStage.HUD_RAW_FOREGROUND, phase, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    Renderer2D.COLOR.render();
                }

                // Scaled fixed HUD foreground
                if (scaledForeground) {
                    ViewportContext.beginScaled(ctx);
                    if (moduleScaledForeground) {
                        Renderer2D.withHudBackdropContribution(() -> {
                            Renderer2D.COLOR.begin();
                            ModuleManager.renderHudEngineForeground(phase, HudRenderSpace.SCALED, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                            Renderer2D.COLOR.render();
                        });
                    }
                    Renderer2D.COLOR.begin();
                    StaticHudElementRegistry.renderAllEngineForeground(phase, HudRenderSpace.SCALED, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    AddonRenderPipelineManager.render2D(HavocRenderStage.HUD_SCALED_FOREGROUND, phase, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    Renderer2D.COLOR.render();
                }

                // Logical foreground
                if (logicalForeground) {
                    ViewportContext.beginUnscaledLogical(ctx);
                    if (moduleLogicalForeground) {
                        Renderer2D.withHudBackdropContribution(() -> {
                            Renderer2D.COLOR.begin();
                            ModuleManager.renderHudEngineForeground(phase, HudRenderSpace.UNSCALED_LOGICAL, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                            Renderer2D.COLOR.render();
                        });
                    }
                    Renderer2D.COLOR.begin();
                    StaticHudElementRegistry.renderAllEngineForeground(phase, HudRenderSpace.UNSCALED_LOGICAL, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    DraggableHudElementRegistry.renderAllEngineForeground(phase, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    AddonRenderPipelineManager.render2D(HavocRenderStage.HUD_LOGICAL_FOREGROUND, phase, Renderer2D.COLOR, textRenderer, ctx, tickProgress);
                    Renderer2D.COLOR.render();
                    if (xaeroWaypointHudWork) {
                        XaeroWaypointHudOverlay.renderForeground(textRenderer);
                    }
                    if (playerLocationHudWork) {
                        PlayerLocationHudOverlay.renderForeground(textRenderer);
                    }
                }
            } finally {
                SystemCursor.endFrame();
                ViewportContext.end(ctx);
            }
        });
        return true;
    }

    private static void renderHudAfterHotbar(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (ClickGuiRenderer.suppressesBackgroundRendering()) return;
        if (!RuntimeGate.canRunHud()) return;
        boolean swapTooltipWork = SwapTooltip.hasWork();
        boolean targetHudWork = DraggableHudElementRegistry.hasEngineWidgetWork("target_hud");
        if (!swapTooltipWork && !targetHudWork) return;

        float tickProgress = TickDelta.tickProgress(tickCounter, false);
        TextRenderer textRenderer = TextRenderer.get();
        Renderer2D.Deferred2DLayer layer = Renderer2D.Deferred2DLayer.HUD_AFTER_MISC_OVERLAYS;

        FullScreenRenderer.ensureInit();
        ViewportContext.beginUnscaled(ctx);
        refreshHudFrameTiming(tickCounter);

        Renderer2D.withDeferredLayer(layer, () -> {
            SystemCursor.beginFrame(ctx);
            try (RenderPhaseScope hudPhase = HavocRenderSystem.phase(RenderPhase.HUD_EFFECTS, "2d:after_hotbar")) {
                // Raw pass
                if (swapTooltipWork) {
                    try (ProfilerPhase.Scope tooltipsScope = ProfilerPhase.scope("2d:after_hotbar:swap_tooltip");
                         RenderProfiler2D.Section ignoredTooltips = RenderProfiler2D.section("swap_tooltip")) {
                        Renderer2D.COLOR.begin();
                        SwapTooltip.render();
                        Renderer2D.COLOR.render();
                    }
                }

                // Logical pass
                if (targetHudWork) {
                    ViewportContext.beginUnscaledLogical(ctx);
                    try (ProfilerPhase.Scope targetHudScope = ProfilerPhase.scope("2d:after_hotbar:target_hud");
                         RenderProfiler2D.Section ignoredTargetHud = RenderProfiler2D.section("target_hud")) {
                        Renderer2D.COLOR.begin();
                        DraggableHudElementRegistry.renderEngineWidget(
                                "target_hud",
                                Renderer2D.COLOR,
                                textRenderer,
                                ctx,
                                tickProgress
                        );
                        Renderer2D.COLOR.render();
                    }
                }
            } finally {
                SystemCursor.endFrame();
                ViewportContext.end(ctx);
            }
        });
        appendHudDeferredMarker(layer, ctx);
    }

    private static void appendHudDeferredMarker(Renderer2D.Deferred2DLayer layer, GuiGraphicsExtractor ctx) {
        if (!(ctx instanceof IGuiGraphics graphics)) return;
        ctx.nextStratum();
        graphics.havoc$addDeferredHudMarker(layer);
        ctx.nextStratum();
    }

    private static Renderer2D.Deferred2DLayer deferredLayerForHudPhase(HudPhase phase) {
        if (phase == null) return Renderer2D.Deferred2DLayer.BEFORE_VANILLA_GUI;
        return switch (phase) {
            case NONE -> Renderer2D.Deferred2DLayer.BEFORE_VANILLA_GUI;
            case FIRST -> Renderer2D.Deferred2DLayer.HUD_FIRST;
            case BEFORE_MISC_OVERLAYS -> Renderer2D.Deferred2DLayer.HUD_BEFORE_MISC_OVERLAYS;
            case AFTER_MISC_OVERLAYS -> Renderer2D.Deferred2DLayer.HUD_AFTER_MISC_OVERLAYS;
            case BEFORE_HOTBAR -> Renderer2D.Deferred2DLayer.HUD_AFTER_MISC_OVERLAYS;
            case AFTER_BOSS_BAR -> Renderer2D.Deferred2DLayer.HUD_AFTER_BOSS_BAR;
            case BEFORE_DEMO_TIMER -> Renderer2D.Deferred2DLayer.HUD_BEFORE_DEMO_TIMER;
            case BEFORE_CHAT -> Renderer2D.Deferred2DLayer.HUD_BEFORE_CHAT;
            case AFTER_SUBTITLES -> Renderer2D.Deferred2DLayer.HUD_AFTER_SUBTITLES;
            case LAST -> Renderer2D.Deferred2DLayer.HUD_LAST;
        };
    }


    private static boolean hasPlayerLocationHudWork(HudPhase phase) {
        // Projected world HUD must remain under the vanilla/custom hotbar. BEFORE_HOTBAR is an
        // explicit extraction slot, so its deferred marker is emitted before CustomHotbar queues
        // its own replacement layer work.
        return phase == HudPhase.BEFORE_HOTBAR && PlayerLocationHudOverlay.hasHudWork();
    }

    private static boolean hasXaeroWaypointHudWork(HudPhase phase) {
        // Keep projected Xaero labels below the hotbar while retaining a dedicated extraction slot
        // after ordinary AFTER_MISC_OVERLAYS content. The BEFORE_HOTBAR marker drains this work
        // before CustomHotbar is extracted even though both use the same deferred HUD layer.
        if (phase != HudPhase.BEFORE_HOTBAR) return false;
        if (!FabricLoader.getInstance().isModLoaded("xaerominimap")) return false;
        return XaeroWaypointHudOverlay.hasHudWork();
    }

    private static boolean hasHudMainPassWork(HudPhase phase, HudRenderSpace space) {
        boolean moduleWork = ModuleManager.hasHudEngineWork(phase, space);
        boolean staticWork = StaticHudElementRegistry.hasEngineWork(phase, space, false);
        boolean draggableWork = space == HudRenderSpace.UNSCALED_LOGICAL && DraggableHudElementRegistry.hasEngineWork(phase, false);
        return moduleWork || staticWork || draggableWork;
    }

    private static boolean hasHudForegroundPassWork(HudPhase phase, HudRenderSpace space) {
        boolean moduleWork = ModuleManager.hasHudEngineForegroundWork(phase, space);
        boolean staticWork = StaticHudElementRegistry.hasEngineWork(phase, space, true);
        boolean draggableWork = space == HudRenderSpace.UNSCALED_LOGICAL && DraggableHudElementRegistry.hasEngineWork(phase, true);
        return moduleWork || staticWork || draggableWork;
    }

    @Override
    public void onInitializeClient() {
        NativeMemoryGuard.initialize();
        HavocRenderEngineBootstrap.init();
        PostProcessManager.register(new NetherPortalRiftPass());
        PostProcessManager.register(new SleepOverlayPass());
        PostProcessManager.register(WorldPostProcessMasks.INSTANCE);
        PostProcessManager.register(WorldJumpShockwaves.INSTANCE);
        PostProcessManager.register(WorldBlastShockwaves.INSTANCE);
        PostProcessManager.register(WorldTargetLenses.INSTANCE);
        PostProcessManager.register(WorldOrbitGravity.INSTANCE);
        MainConfig.get(); // Load global settings (debug flag)
        AccountConfig.get();
        ProxyBackend.init();
        havoc.client.features.gui.chat.diagnostics.FailureDiagnostics.initialize();
        CommandManager.init();
        MediaSessionService.get().init();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> RenderPrewarmManager.onClientStarted());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            // Stop all Havoc callbacks before native-backed subsystems begin one-way shutdown.
            // Minecraft can still extract one final GUI frame while disconnecting.
            ClientRuntime.beginShutdown("client stopping");
            // Restore process-level security state before subsystem shutdown.
            NativeMemoryGuard.shutdown();
            MediaSessionService.get().shutdown();
            JavetRuntimeBootstrap.shutdown();
            MapLocationRuntime.get().shutdown();
            HeuristicRuntime.get().shutdown();
            MapLinkRuntime.get().shutdown();
        });

        // -------- CONFIG + BINDS ----------
        // Enabled addons declare built-in module exclusions before auto-discovery.
        AddonManager.prepareModuleLoading();
        ModuleAutoLoader.load("havoc.client.features.module.modules");
        AddonManager.init();
        ModuleManager.loadAllModuleConfigs();
        AddonManager.notifyClientReady();
        HudElements.init();
        StaticHudElementBootstrap.init(); // init vanilla HUD elements config
        Events.BUS.register(I18nPreflightContributors.INSTANCE);
        Events.BUS.register(RenderPrewarmContributors.INSTANCE);
        Events.BUS.register(RotationManager.INSTANCE);
        Events.BUS.register(AttackPressing.INSTANCE);
        Events.BUS.register(InventorySwap.INSTANCE);
        Events.BUS.register(SprintController.INSTANCE);
        Events.BUS.register(AntiBotTracker.INSTANCE);
        Events.BUS.register(VulcanReachController.INSTANCE);
        Events.BUS.register(CombatProtocolHeuristics.INSTANCE);
        Events.BUS.register(PvpTracker.INSTANCE);
        Events.BUS.register(PvpTargetTracker.INSTANCE);
        Events.BUS.register(SessionStatisticsTracker.INSTANCE);
        Events.BUS.registerIsolated(MapLinkRuntime.get());
        Events.BUS.registerIsolated(MapLocationRuntime.get());
        Events.BUS.register(SelfBedTracker.INSTANCE);
        Events.BUS.register(BoatSimulationCache.INSTANCE);
        Events.BUS.register(PlayerSimulationCache.INSTANCE);
        Events.BUS.register(BlinkManager.INSTANCE);
        Events.BUS.register(FakeLagController.INSTANCE);
        Events.BUS.register(BacktrackController.INSTANCE);
        Events.BUS.register(TimerController.INSTANCE);

        // -------- КЛЮЧЕВЫЕ ИВЕНТЫ --------

        // === ТИКИ ===


        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (!RuntimeGate.canRunClientLogic()) return;
            try (ProfilerPhase.Scope tickScope = ProfilerPhase.scope("havoc:client_tick:start")) {
                Events.BUS.post(new GameTickEvent());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Keep startup/reload warmup moving even on title/menu screens where no player exists.
            RenderPrewarmManager.pumpDeferred();

            if (!ProfilerPhase.isActive()) {
                ModuleManager.tickRuntimeControllers();
                if (!RuntimeGate.canRunClientLogic()) return;
                if (!ClickGuiRenderer.isBlockingModuleKeybinds()) {
                    ModuleManager.handleModuleKeybinds();
                }
                TargetManager.tick();
                ModuleManager.tickAll();
                DraggableHudElementRegistry.tickAll();
                StaticHudElementRegistry.tickAll();
                return;
            }

            try (ProfilerPhase.Scope tickScope = ProfilerPhase.scope("havoc:client_tick:end")) {
                try (ProfilerPhase.Scope ignored = ProfilerPhase.scope("runtime_controllers")) {
                    ModuleManager.tickRuntimeControllers();
                }
                if (!RuntimeGate.canRunClientLogic()) return;

                if (!ClickGuiRenderer.isBlockingModuleKeybinds()) {
                    try (ProfilerPhase.Scope ignored = ProfilerPhase.scope("module_keybinds")) {
                        ModuleManager.handleModuleKeybinds();
                    }
                }
                try (ProfilerPhase.Scope ignored = ProfilerPhase.scope("target_manager")) {
                    TargetManager.tick();
                }
                ModuleManager.tickAll();
                DraggableHudElementRegistry.tickAll();
                StaticHudElementRegistry.tickAll();
            }
        });

        /* ====================== WORLD RENDER ====================== */

        LevelRenderEvents.COLLECT_SUBMITS.register(ctx -> renderWorldPhase(
                WorldPhase.BEFORE_ENTITIES,
                ctx.poseStack(),
                ctx.submitNodeCollector()
        ));

        LevelRenderEvents.AFTER_SOLID_FEATURES.register(ctx -> renderWorldPhase(
                WorldPhase.AFTER_ENTITIES,
                ctx.poseStack(),
                ctx.submitNodeCollector()
        ));

        LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(ctx -> renderWorldPhase(
                WorldPhase.BEFORE_TRANSLUCENT,
                ctx.poseStack(),
                ctx.submitNodeCollector()
        ));

        LevelRenderEvents.END_MAIN.register(ctx -> renderWorldPhase(
                WorldPhase.END_MAIN,
                ctx.poseStack(),
                ctx.submitNodeCollector()
        ));


        // === HUD 2D OVERLAYS ===

        HudElementRegistry.addFirst(
                Identifier.fromNamespaceAndPath("havoc", "hud_first"),
                (ctx, tc) -> renderHudPhase(HudPhase.FIRST, ctx, tc)
        );

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("havoc", "hud_last"),
                (ctx, tc) -> renderHudPhase(HudPhase.LAST, ctx, tc)
        );

        // остальные позиции HUD:
        HudElementRegistry.attachElementBefore(
                net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("havoc", "hud_before_misc"),
                (ctx, tc) -> renderHudPhase(HudPhase.BEFORE_MISC_OVERLAYS, ctx, tc)
        );

        HudElementRegistry.attachElementAfter(
                net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("havoc", "hud_after_misc"),
                (ctx, tc) -> renderHudPhase(HudPhase.AFTER_MISC_OVERLAYS, ctx, tc)
        );

        HudElementRegistry.attachElementBefore(
                net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath("havoc", "hud_before_hotbar"),
                (ctx, tc) -> renderHudPhase(HudPhase.BEFORE_HOTBAR, ctx, tc)
        );

        HudElementRegistry.attachElementAfter(
                net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.SLEEP,
                Identifier.fromNamespaceAndPath("havoc", "hud_after_boss"),
                (ctx, tc) -> renderHudPhase(HudPhase.AFTER_BOSS_BAR, ctx, tc)
        );

        HudElementRegistry.attachElementBefore(
                net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.DEMO_TIMER,
                Identifier.fromNamespaceAndPath("havoc", "hud_before_demo"),
                (ctx, tc) -> renderHudPhase(HudPhase.BEFORE_DEMO_TIMER, ctx, tc)
        );

        HudElementRegistry.attachElementBefore(
                net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("havoc", "hud_before_chat"),
                (ctx, tc) -> renderHudPhase(HudPhase.BEFORE_CHAT, ctx, tc)
        );

        HudElementRegistry.attachElementAfter(
                net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.SUBTITLES,
                Identifier.fromNamespaceAndPath("havoc", "hud_after_subtitles"),
                (ctx, tc) -> renderHudPhase(HudPhase.AFTER_SUBTITLES, ctx, tc)
        );

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath("havoc", "hud_after_hotbar"),
                (ctx, tc) -> renderHudAfterHotbar(ctx, tc)
        );

        registerRuntimeLifecycle();
        ClientRuntime.markActive("client initialized");
        RenderPrewarmManager.prewarm("client initialized");
    }
}
