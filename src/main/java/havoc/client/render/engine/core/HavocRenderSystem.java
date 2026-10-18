/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.core;

import com.mojang.blaze3d.systems.RenderSystem;
import havoc.client.render.engine.compat.immediatelyfast.ImmediatelyFastRuntime;
import havoc.client.render.engine.compat.immediatelyfast.ImmediatelyFastRuntimeSnapshot;
import havoc.client.render.engine.renderer.Renderer3D;
import havoc.client.render.engine.renderer.ui.ItemBatchRenderer;
import havoc.client.render.engine.renderer.ui.UiBlurResources;
import havoc.client.render.engine.postprocess.PostProcessManager;
import havoc.client.render.iris.IrisRuntime;
import havoc.client.render.iris.IrisRuntimeSnapshot;
import havoc.client.render.sodium.SodiumTerrainInteropStatsSnapshot;
import havoc.client.render.sodium.SodiumVisibilityStatsSnapshot;
import org.joml.Matrix4f;
import havoc.client.render.engine.RenderState;
import havoc.client.render.engine.core.policy.LightPolicy;
import havoc.client.render.engine.core.policy.VanillaWorldFogProvider;
import havoc.client.render.engine.depth.WorldSceneDepth;
import havoc.client.render.engine.framegraph.HavocFrameGraph;
import havoc.client.render.engine.profiler.FrameRateLog;
import havoc.client.render.engine.profiler.FrameStutterProfiler;
import havoc.client.render.engine.profiler.RenderFrameProfiler;
import havoc.client.render.engine.profiler.TracyProfiler;
import havoc.client.render.engine.profiler.UiPipelineTelemetry;
import havoc.client.render.engine.rhi.HavocRhi;
import havoc.client.render.engine.rhi.RhiCapabilities;
import havoc.client.render.engine.rhi.RhiStatsSnapshot;
import havoc.client.render.engine.rhi.backend.SodiumGlBackend;
import havoc.client.render.engine.rhi.backend.vulkan.HavocVulkanBackend;
import havoc.client.render.engine.rhi.resource.RenderResourceManager;
import havoc.client.render.engine.rhi.resource.RenderResourceStatsSnapshot;
import havoc.client.render.engine.rhi.uniform.HavocUniformAllocator;
import havoc.client.render.engine.scene.spatial.SceneSpatialRegistry;
import havoc.client.render.engine.scene.instance.SceneInstanceRegistry;
import havoc.client.render.engine.scene.instance.SceneInstanceStatsSnapshot;
import havoc.client.render.engine.asset.gltf.gpu.GltfGpuResidencyManager;
import havoc.client.render.engine.asset.gltf.gpu.GltfGpuResidencyStats;
import havoc.client.render.engine.scene.spatial.SceneSpatialStatsSnapshot;
import havoc.client.render.engine.scene.visibility.SceneViewContext;
import havoc.client.render.engine.scene.visibility.SceneVisibilityService;
import havoc.client.render.engine.scene.visibility.SceneVisibilityStatsSnapshot;
import havoc.client.render.engine.rhi.uniform.UniformAllocatorStatsSnapshot;
import havoc.client.render.engine.text.TextCommandStatsSnapshot;
import havoc.client.render.engine.text.TextRenderSystem;
import havoc.client.render.engine.renderer.ui.clip.UiMsaaClipLayer;
import havoc.client.render.engine.world.WorldRenderStatsSnapshot;
import havoc.client.render.sodium.SodiumFrameContext;
import havoc.client.render.sodium.SodiumRenderBridge;
import havoc.client.util.logging.DebugLog;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single owner of Havoc render lifecycle.
 * - one Havoc frame per Minecraft-presented frame;
 * - many scoped RenderPhase entries inside that frame;
 * - backend submission is flushed before framePresented(), not by random renderer classes;
 * - RenderState is a compatibility shim, not the lifecycle owner.
 */
public enum HavocRenderSystem {
    ;
    private static final Logger LOGGER = LoggerFactory.getLogger("Havoc");
    private static final SodiumRenderBridge SODIUM = new SodiumRenderBridge();
    private static final HavocFrameGraph FRAME_GRAPH = new HavocFrameGraph();
    private static final HavocUniformAllocator UNIFORMS = new HavocUniformAllocator();
    private static final SceneSpatialRegistry SCENE_SPATIAL = new SceneSpatialRegistry();
    private static final SceneVisibilityService SCENE_VISIBILITY = new SceneVisibilityService(SCENE_SPATIAL);
    private static final SceneInstanceRegistry SCENE_INSTANCES = new SceneInstanceRegistry(SCENE_SPATIAL, SCENE_VISIBILITY);

    private static HavocRhi rhi;
    private static BackendKind backendKind = BackendKind.UNKNOWN;
    private static RenderFrameContext currentContext;
    private static SceneViewContext currentSceneView;
    private static long frameId;
    private static boolean initialized;
    private static boolean frameOpen;
    private static boolean submissionEnded;
    private static FrameLifecycle lifecycle = FrameLifecycle.IDLE;

    private enum BackendKind {
        UNKNOWN,
        GL,
        VULKAN
    }

    public static void init() {
        if (initialized) {
            ensureBackendMatchesDevice();
            return;
        }
        BackendKind desired = detectBackendKind();
        backendKind = desired == BackendKind.UNKNOWN ? BackendKind.GL : desired;
        rhi = createBackend(backendKind);
        initialized = true;
        DebugLog.renderThreadOnChange(
                "havoc.rhi.backend",
                backendKind,
                "[HavocRHI] backend initialized: %s",
                backendKind
        );
        logCapabilities();
        ensureBackendMatchesDevice();
    }

    private static HavocRhi createBackend(BackendKind kind) {
        if (kind == BackendKind.VULKAN) {
            return new HavocVulkanBackend();
        }
        return new SodiumGlBackend();
    }

    private static void ensureBackendMatchesDevice() {
        if (!initialized || rhi == null) return;
        BackendKind desired = detectBackendKind();
        if (desired == BackendKind.UNKNOWN || desired == backendKind) return;

        if (frameOpen) {
            DebugLog.warnOnChange(
                    "havoc.rhi.backend.switch.deferred",
                    backendKind + "->" + desired + "|frameOpen",
                    "[HavocRHI] backend switch deferred until frame end: %s -> %s",
                    backendKind,
                    desired
            );
            return;
        }

        HavocRhi previous = rhi;
        BackendKind previousKind = backendKind;
        HavocRhi next;
        try {
            next = createBackend(desired);
        } catch (Throwable t) {
            DebugLog.error("[HavocRHI] failed to create backend " + desired, t);
            return;
        }

        rhi = next;
        backendKind = desired;
        try {
            UiBlurResources.onBackendChanged();
            PostProcessManager.releaseBackendResources(previous);
            GltfGpuResidencyManager.global().releaseBackend(previous);
            IrisRuntime.releaseImportedGeometryBackend(previous);
            previous.close();
        } catch (Throwable t) {
            DebugLog.warnOnChange(
                    "havoc.rhi.backend.old.close.failed",
                    previousKind + "|" + desired + "|" + t.getClass().getSimpleName(),
                    "[HavocRHI] old backend close failed during switch %s -> %s: %s: %s",
                    previousKind,
                    desired,
                    t.getClass().getSimpleName(),
                    t.getMessage()
            );
        }
        DebugLog.renderThreadOnChange(
                "havoc.rhi.backend",
                backendKind,
                "[HavocRHI] backend switched: %s -> %s",
                previousKind,
                backendKind
        );
        logCapabilities();
    }

    private static void logCapabilities() {
        try {
            RhiCapabilities caps = RhiCapabilities.current();
            if (backendKind == BackendKind.GL) {
                DebugLog.info("[HavocRHI] GL supplemental capabilities: vendor='{}', renderer='{}', "
                                + "compute={}, ssbo={}, tessellation={}, geometry={}, multiBind={}, "
                                + "copyImage={} (auto={}), invalidate={}, nativeDSA={}",
                        caps.glVendor(), caps.glRenderer(),
                        caps.computeShaders(), caps.shaderStorageBuffers(),
                        caps.tessellationShaders(), caps.geometryShaders(), caps.multiBind(),
                        caps.copyImage(), caps.copyImageAutoSafe(),
                        caps.attachmentInvalidation(), caps.nativeDirectStateAccess());
            } else if (backendKind == BackendKind.VULKAN) {
                DebugLog.info("[HavocRHI] Vulkan supplemental capabilities: compute={}, ssbo={}, "
                                + "tessellation={}, geometry={}",
                        caps.computeShaders(), caps.shaderStorageBuffers(),
                        caps.tessellationShaders(), caps.geometryShaders());
            }
        } catch (Throwable t) {
            DebugLog.warnOnChange(
                    "havoc.rhi.capabilities.log.failed",
                    t.getClass().getSimpleName() + "|" + t.getMessage(),
                    "[HavocRHI] capability logging failed: %s: %s",
                    t.getClass().getSimpleName(),
                    t.getMessage()
            );
        }
    }

    // rhi() runs this on every call; the answer only changes when Blaze3D swaps its device, so
    // cache it per device instance instead of re-reading device info and lowercasing each time.
    private static Object detectedForDevice;
    private static BackendKind detectedKind = BackendKind.UNKNOWN;

    private static BackendKind detectBackendKind() {
        Object device;
        try {
            device = RenderSystem.tryGetDevice();
        } catch (Throwable ignored) {
            device = null;
        }
        if (device != null && device == detectedForDevice) return detectedKind;

        BackendKind kind = detectBackendKindUncached(device);
        // Only cache real devices: before the device exists the description can still change.
        if (device != null && kind != BackendKind.UNKNOWN) {
            detectedForDevice = device;
            detectedKind = kind;
        }
        return kind;
    }

    private static BackendKind detectBackendKindUncached(Object deviceObject) {
        String backendName = null;
        try {
            if (deviceObject instanceof com.mojang.blaze3d.systems.GpuDevice device && device.getDeviceInfo() != null) {
                backendName = device.getDeviceInfo().backendName();
            }
        } catch (Throwable ignored) {
            // Fall through to RenderSystem backend description.
        }

        if (backendName == null || backendName.isBlank()) {
            try {
                backendName = RenderSystem.getBackendDescription();
            } catch (Throwable ignored) {
                backendName = null;
            }
        }

        if (backendName == null || backendName.isBlank()) return BackendKind.UNKNOWN;
        String normalized = backendName.toLowerCase(Locale.ROOT);
        if (normalized.contains("vulkan")) return BackendKind.VULKAN;
        if (normalized.contains("opengl") || normalized.contains("gl")) return BackendKind.GL;
        return BackendKind.UNKNOWN;
    }

    public static HavocRhi rhi() {
        init();
        ensureBackendMatchesDevice();
        return rhi;
    }

    public static SodiumRenderBridge sodium() {
        return SODIUM;
    }

    public static RenderResourceManager resources() {
        return rhi().resources();
    }

    public static HavocFrameGraph frameGraph() {
        return FRAME_GRAPH;
    }

    public static HavocUniformAllocator uniforms() {
        return UNIFORMS;
    }

    public static RenderFrameContext currentContext() {
        return currentContext;
    }

    public static FrameLifecycle lifecycle() {
        return lifecycle;
    }

    /**
     * Opens the Havoc frame if needed, otherwise refreshes camera/viewport data without advancing frameId.
     */
    public static RenderFrameContext beginFrame(float tickDelta, Matrix4f projection, Matrix4f modelView) {
        return beginFrame(tickDelta, tickDelta, tickDelta, projection, modelView);
    }

    public static RenderFrameContext beginFrame(float tickProgress,
                                                float frameDeltaTicks,
                                                float fixedDeltaTicks,
                                                Matrix4f projection,
                                                Matrix4f modelView) {
        HavocRhi activeRhi = rhi();
        SodiumFrameContext sodiumFrame;
        if (!frameOpen) {
            frameId++;
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            SCENE_INSTANCES.beginWorld(minecraft != null ? minecraft.level : null);
            SCENE_INSTANCES.beginFrame(frameId);
            SCENE_VISIBILITY.beginFrame(frameId);
            UiPipelineTelemetry.beginFrame(frameId);
            activeRhi.stats().setDetailedPipelineStats(TracyProfiler.isEnabled());
            activeRhi.beginFrame(frameId);
            UNIFORMS.beginFrame(frameId);
            TextRenderSystem.beginFrame();
            sodiumFrame = SODIUM.beginFrame();
            RenderFrameProfiler.beginFrame(frameId);
            frameOpen = true;
            submissionEnded = false;
            lifecycle = FrameLifecycle.RECORDING;
        } else {
            sodiumFrame = SODIUM.currentFrameContext();
        }

        RenderPhase phase = currentContext != null ? currentContext.phase() : RenderPhase.NONE;
        RenderFrameContext ctx = new RenderFrameContext(
                frameId,
                tickProgress,
                frameDeltaTicks,
                fixedDeltaTicks,
                phase,
                CameraContext.capture(projection, modelView),
                ViewportContext.capture(),
                FramebufferContext.capture(),
                WorldSceneDepth::mainDepthView,
                VanillaWorldFogProvider.INSTANCE,
                LightPolicy.VANILLA,
                SODIUM.visibilityProvider(),
                sodiumFrame
        );
        currentSceneView = SceneViewContext.primary(ctx, activeRhi.capabilities());
        currentContext = ctx;
        RenderState.applyContext(ctx);
        return ctx;
    }

    /**
     * Refreshes only timing data for phases that do not own the world camera/projection
     * themselves, for example Fabric HUD callbacks.
     */
    public static RenderFrameContext updateFrameTiming(float tickProgress,
                                                       float frameDeltaTicks,
                                                       float fixedDeltaTicks) {
        RenderFrameContext base = ensureFrameContext();
        currentContext = base.withTiming(tickProgress, frameDeltaTicks, fixedDeltaTicks);
        RenderState.applyContext(currentContext);
        return currentContext;
    }

    public static RenderFrameContext ensureFrameContext() {
        if (currentContext != null) return currentContext;
        return beginFrame(RenderState.tickProgress, RenderState.frameDeltaTicks, RenderState.fixedDeltaTicks, new Matrix4f(), new Matrix4f(RenderSystem.getModelViewStack()));
    }

    public static RenderPhaseScope phase(RenderPhase phase) {
        return phase(phase, null);
    }

    public static RenderPhaseScope phase(RenderPhase phase, String label) {
        RenderFrameContext before = currentContext;
        RenderFrameContext base = ensureFrameContext();
        RenderPhase next = phase == null ? RenderPhase.NONE : phase;
        currentContext = base.withPhase(next);
        RenderState.applyContext(currentContext);
        return new RenderPhaseScope(next, before, RenderFrameProfiler.phase(next, label));
    }

    static void restorePhase(RenderFrameContext previous) {
        currentContext = previous;
        if (currentContext != null) {
            RenderState.applyContext(currentContext);
        } else {
            RenderState.clearContextBackedState();
        }
    }

    public static void endRenderSubmission() {
        if (!initialized || !frameOpen || submissionEnded) return;
        try {
            ItemBatchRenderer.finishUiItemFrame();
            TextRenderSystem.flush();
            rhi.endRenderSubmission();
            lifecycle = FrameLifecycle.SUBMITTED;
        } finally {
            submissionEnded = true;
        }
    }

    /**
     * Called after the window surface has been presented. Rendering submission must already
     * be closed before GpuSurface.blitFromTexture(), otherwise late Havoc draws miss the
     * presented frame and can leave stale main-target contents for the next frame.
     */
    public static void onFramePresented() {
        if (!initialized) return;
        try {
            if (!submissionEnded) {
                // Fallback for unusual renderFrame paths. Normal visible frames close in
                // GpuSurfaceMixin#havoc$beforeSurfaceBlit, before the surface copy.
                endRenderSubmission();
            }
            rhi.framePresented();
            UNIFORMS.onFramePresented();
            if (FrameStutterProfiler.isEnabled()) {
                FrameStutterProfiler.onFramePresented(rhiStatsSnapshot(), uniformStatsSnapshot(), resourceStatsSnapshot());
            }
            // The stat snapshots below are large records that only Tracy consumes; building four of them
            // every frame while no profiler is attached was pure allocation.
            if (TracyProfiler.isEnabled()) {
                RhiStatsSnapshot rhiSnapshot = rhi().stats().snapshot(true);
                TracyProfiler.plotUiPipeline(UiPipelineTelemetry.snapshot());
                TracyProfiler.plotRhiPipeline(rhiSnapshot);
                TracyProfiler.plotRenderResources(resourceStatsSnapshot());
                RenderFrameProfiler.endFrame(rhiSnapshot, uniformStatsSnapshot());
            } else {
                RenderFrameProfiler.endFrame(null, null);
            }
            FrameRateLog.onFrame();
            lifecycle = FrameLifecycle.PRESENTED;
        } finally {
            SCENE_INSTANCES.endFrame(frameId);
            currentContext = null;
            currentSceneView = null;
            frameOpen = false;
            submissionEnded = false;
            lifecycle = FrameLifecycle.IDLE;
            RenderState.clearContextBackedState();
        }
    }

    public static RhiStatsSnapshot rhiStatsSnapshot() {
        return rhi().stats().snapshot();
    }

    public static SodiumVisibilityStatsSnapshot sodiumVisibilityStatsSnapshot() {
        return SODIUM.visibilityStatsSnapshot();
    }

    public static SodiumTerrainInteropStatsSnapshot sodiumTerrainStatsSnapshot() {
        return SODIUM.terrainInterop().statsSnapshot();
    }

    public static IrisRuntimeSnapshot irisSnapshot() {
        return IrisRuntime.snapshot();
    }

    public static ImmediatelyFastRuntimeSnapshot immediatelyFastSnapshot() {
        return ImmediatelyFastRuntime.snapshot();
    }

    public static UniformAllocatorStatsSnapshot uniformStatsSnapshot() {
        return UNIFORMS.statsSnapshot();
    }

    public static RenderResourceStatsSnapshot resourceStatsSnapshot() {
        return resources().statsSnapshot();
    }

    public static WorldRenderStatsSnapshot worldRenderStatsSnapshot() {
        return Renderer3D.worldStatsSnapshot();
    }

    public static TextCommandStatsSnapshot textStatsSnapshot() {
        return TextRenderSystem.statsSnapshot();
    }

    public static SceneSpatialRegistry sceneSpatialRegistry() {
        return SCENE_SPATIAL;
    }

    public static SceneInstanceRegistry sceneInstances() {
        return SCENE_INSTANCES;
    }

    public static SceneInstanceStatsSnapshot sceneInstanceStatsSnapshot() {
        return SCENE_INSTANCES.statsSnapshot();
    }

    public static GltfGpuResidencyStats gltfGpuResidencyStatsSnapshot() {
        return GltfGpuResidencyManager.global().statsSnapshot();
    }

    public static SceneVisibilityService sceneVisibility() {
        return SCENE_VISIBILITY;
    }

    public static SceneVisibilityStatsSnapshot sceneVisibilityStatsSnapshot() {
        return SCENE_VISIBILITY.statsSnapshot();
    }

    public static SceneSpatialStatsSnapshot sceneSpatialStatsSnapshot() {
        return SCENE_SPATIAL.statsSnapshot();
    }

    public static SceneViewContext currentSceneView() {
        return currentSceneView;
    }

    /**
     * Refreshes the stable PRIMARY scene view after HavocWorldMatrices has been published.
     * The first beginFrame call intentionally happens before TAA jitter selection so it can supply
     * the canonical frame id; world-matrix capture follows immediately afterwards.
     */
    public static void refreshPrimarySceneView() {
        RenderFrameContext context = currentContext;
        HavocRhi activeRhi = rhi;
        if (context == null || activeRhi == null) return;
        currentSceneView = SceneViewContext.primary(context, activeRhi.capabilities());
    }

    public static void shutdown() {
        if (!initialized) return;
        try {
            UiMsaaClipLayer.shutdown();
            UiBlurResources.onBackendChanged();
            PostProcessManager.releaseBackendResources(rhi);
            GltfGpuResidencyManager.global().releaseBackend(rhi);
            IrisRuntime.releaseImportedGeometryBackend(rhi);
            UNIFORMS.close();
            rhi.close();
        } finally {
            initialized = false;
            currentContext = null;
            currentSceneView = null;
            frameOpen = false;
            submissionEnded = false;
            lifecycle = FrameLifecycle.IDLE;
            rhi = null;
            SCENE_INSTANCES.clear();
            SCENE_SPATIAL.clear();
            RenderState.clearContextBackedState();
        }
    }
}
