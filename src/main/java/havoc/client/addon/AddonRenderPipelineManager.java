/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.addon;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import havoc.client.api.v0.render.HavocPostProcessCallback;
import havoc.client.api.v0.render.HavocPostProcessContext;
import havoc.client.api.v0.render.HavocRenderCallback;
import havoc.client.api.v0.render.HavocRenderContext;
import havoc.client.api.v0.render.HavocRenderStage;
import havoc.client.features.module.HudPhase;
import havoc.client.features.module.WorldPhase;
import havoc.client.render.engine.postprocess.PostProcessManager;
import havoc.client.render.engine.postprocess.PostProcessPass;
import havoc.client.render.engine.postprocess.PostProcessExecutionContext;
import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.engine.renderer.Renderer3D;
import havoc.client.render.engine.text.TextRenderer;
import havoc.client.util.logging.DebugLog;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum AddonRenderPipelineManager {
    ;

    private static final Map<HavocRenderStage, List<RegisteredRenderCallback>> CALLBACKS =
            new EnumMap<>(HavocRenderStage.class);

    static {
        for (HavocRenderStage stage : HavocRenderStage.values()) {
            CALLBACKS.put(stage, new ArrayList<>());
        }
    }

    public static synchronized boolean register(String addonId,
                                                String callbackId,
                                                HavocRenderStage stage,
                                                HavocRenderCallback callback) {
        String normalizedAddonId = normalize(addonId);
        String normalizedCallbackId = normalize(callbackId);
        if (normalizedAddonId.isBlank() || normalizedCallbackId.isBlank() || stage == null || callback == null) {
            return false;
        }
        List<RegisteredRenderCallback> callbacks = CALLBACKS.get(stage);
        for (RegisteredRenderCallback registered : callbacks) {
            if (registered.addonId().equals(normalizedAddonId) && registered.callbackId().equals(normalizedCallbackId)) {
                return false;
            }
        }
        callbacks.add(new RegisteredRenderCallback(normalizedAddonId, normalizedCallbackId, callback));
        return true;
    }

    public static boolean hasActiveCallbacks(HavocRenderStage stage) {
        if (stage == null) return false;
        List<RegisteredRenderCallback> callbacks;
        synchronized (AddonRenderPipelineManager.class) {
            callbacks = List.copyOf(CALLBACKS.get(stage));
        }
        for (RegisteredRenderCallback callback : callbacks) {
            if (AddonManager.isActive(callback.addonId())) return true;
        }
        return false;
    }

    public static boolean registerPostProcess(String addonId,
                                              String passId,
                                              PostProcessPass.Phase phase,
                                              int priority,
                                              HavocPostProcessCallback callback) {
        String normalizedAddonId = normalize(addonId);
        String normalizedPassId = normalize(passId);
        if (normalizedAddonId.isBlank() || normalizedPassId.isBlank() || phase == null || callback == null) {
            return false;
        }
        PostProcessManager.register(new AddonPostProcessPass(normalizedAddonId, normalizedPassId, phase, priority, callback));
        return true;
    }

    public static void render2D(HavocRenderStage stage,
                                HudPhase hudPhase,
                                Renderer2D renderer,
                                TextRenderer textRenderer,
                                GuiGraphicsExtractor guiContext,
                                float tickDelta) {
        render(stage, hudPhase, null, renderer, null, null, textRenderer, guiContext, null, tickDelta);
    }

    public static void render3D(HavocRenderStage stage,
                                WorldPhase worldPhase,
                                Renderer3D renderer,
                                Renderer3D depthRenderer,
                                PoseStack poseStack,
                                float tickDelta) {
        render(stage, null, worldPhase, null, renderer, depthRenderer, null, null, poseStack, tickDelta);
    }

    public static synchronized void unregisterAddon(String addonId) {
        String normalizedAddonId = normalize(addonId);
        for (List<RegisteredRenderCallback> callbacks : CALLBACKS.values()) {
            callbacks.removeIf(callback -> callback.addonId().equals(normalizedAddonId));
        }
    }

    private static void render(HavocRenderStage stage,
                               HudPhase hudPhase,
                               WorldPhase worldPhase,
                               Renderer2D renderer2D,
                               Renderer3D renderer3D,
                               Renderer3D depthRenderer3D,
                               TextRenderer textRenderer,
                               GuiGraphicsExtractor guiContext,
                               PoseStack poseStack,
                               float tickDelta) {
        List<RegisteredRenderCallback> callbacks;
        synchronized (AddonRenderPipelineManager.class) {
            callbacks = List.copyOf(CALLBACKS.get(stage));
        }
        for (RegisteredRenderCallback registered : callbacks) {
            if (!AddonManager.isActive(registered.addonId())) continue;
            try {
                registered.callback().render(new HavocRenderContext(
                        registered.addonId(),
                        registered.callbackId(),
                        stage,
                        hudPhase,
                        worldPhase,
                        renderer2D,
                        renderer3D,
                        depthRenderer3D,
                        textRenderer,
                        guiContext,
                        poseStack,
                        tickDelta
                ));
            } catch (Throwable t) {
                DebugLog.error("[Addons] Render callback failed: addon=%s id=%s",
                        t, registered.addonId(), registered.callbackId());
            }
        }
    }

    private static String normalize(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    private record RegisteredRenderCallback(String addonId,
                                            String callbackId,
                                            HavocRenderCallback callback) {
    }

    private record AddonPostProcessPass(String addonId,
                                        String passId,
                                        PostProcessPass.Phase phase,
                                        int priority,
                                        HavocPostProcessCallback callback) implements PostProcessPass {
        @Override
        public boolean isActive() {
            return AddonManager.isActive(addonId);
        }

        @Override
        public boolean render(PostProcessExecutionContext execution) {
            if (!isActive() || execution == null || execution.context() == null) return false;
            GpuTextureView src = execution.source();
            GpuTextureView dst = execution.destination();
            float tickDelta = execution.context().tickDelta();
            try {
                return callback.render(new HavocPostProcessContext(addonId, passId, phase, src, dst, tickDelta));
            } catch (Throwable t) {
                DebugLog.error("[Addons] Post-process callback failed: addon=%s id=%s", t, addonId, passId);
                return false;
            }
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public Phase getPhase() {
            return phase;
        }
    }
}
