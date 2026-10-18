/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.postprocess;

import havoc.client.config.MainConfig;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.core.HavocWorldMatrices;
import havoc.client.render.engine.rhi.HavocRhi;
import havoc.client.render.engine.rhi.shader.RhiStorageImage;
import havoc.client.render.iris.IrisRuntime;
import havoc.client.util.logging.DebugLog;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/** Canonical production TAA pass. MSAA/TAA exclusivity is owned by VisualConfig.
 *  Native-resolution reprojection intentionally reconstructs the current jittered screen sample
 *  through the stable projection: the raster jitter is not cancelled before
 *  history lookup, so a static output pixel accumulates different sub-pixel samples over time.
 */
public final class TemporalAntiAliasingPass implements PostProcessPass, PostProcessBackendResourceOwner {
    public static final TemporalAntiAliasingPass INSTANCE = new TemporalAntiAliasingPass();

    private final TemporalAntiAliasingBackend backend = new TemporalAntiAliasingBackend();
    private Matrix4f previousView;
    private Matrix4f previousProjection;
    private Vec3 previousCameraPosition;
    private Object previousWorld;
    private boolean historyValid;
    private boolean computeSupported = true;
    private boolean wasSelected;

    private TemporalAntiAliasingPass() { }

    public static boolean shouldJitter() {
        // Shaderpack raster jitter is injected by the selected data-driven adapter.
        if (!MainConfig.get().isTaaRuntimeActive() || IrisRuntime.isShaderpackRendererActive()) return false;
        try {
            return INSTANCE.computeSupported && PostProcessExecutionPolicy.useCompute(HavocRenderSystem.rhi());
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public boolean isActive() {
        // This is the main-framebuffer graph path only. A shaderpack adapter invokes the same
        // backend from its declared temporal stage through renderShaderpack().
        boolean selected = MainConfig.get().isTaaRuntimeActive() && !IrisRuntime.isShaderpackRendererActive();
        if (!selected && wasSelected) invalidateHistory();
        wasSelected = selected;
        return selected && computeSupported;
    }

    @Override public int getPriority() { return -100; }
    @Override public Phase getPhase() { return Phase.PRE_HAND; }

    @Override
    public boolean prefersStorageOutput(HavocRhi rhi) {
        return isActive() && PostProcessExecutionPolicy.useCompute(rhi);
    }

    @Override
    public boolean render(PostProcessExecutionContext execution) {
        if (!isActive() || execution == null || execution.context() == null || execution.destinationStorage() == null) {
            invalidateHistory();
            return false;
        }

        HavocRhi rhi = execution.rhi();
        boolean zeroToOne = rhi.capabilities().zeroToOneDepth();
        return renderResources(rhi, execution.destinationStorage(), execution.source(),
                execution.context().mainDepth(),
                HavocWorldMatrices.positionMatrix(),
                HavocWorldMatrices.unjitteredRenderProjectionMatrix(),
                HavocWorldMatrices.cameraPosition(),
                zeroToOne ? 1.0f : 2.0f, zeroToOne ? 0.0f : -1.0f,
                0.0f, true);
    }

    /** Runs the canonical TAA backend at a shaderpack-owned HDR stage. */
    public boolean renderShaderpack(HavocRhi rhi,
                                    RhiStorageImage destination,
                                    GpuTextureView currentColor,
                                    GpuTextureView currentDepth,
                                    Matrix4fc currentView,
                                    Matrix4fc currentProjection,
                                    Vec3 currentCamera) {
        if (!MainConfig.get().isTaaRuntimeActive() || !IrisRuntime.isShaderpackRendererActive()
                || !computeSupported || rhi == null || destination == null
                || currentColor == null || currentDepth == null) {
            invalidateHistory();
            return false;
        }
        // Iris presents depthtex* to shaderpacks as forward depth by rewriting depth reads to
        // (1 - rawDepth), while the physical GPU depth texture remains Minecraft reverse-Z.
        // CapturedRenderingState.gbufferProjection has reverse-Z already undone and therefore
        // expects forward [-1, 1] NDC. Convert raw reverse-Z depth with: z = 1 - 2 * rawDepth.
        return renderResources(rhi, destination, currentColor, currentDepth,
                currentView, currentProjection, currentCamera,
                -2.0f, 1.0f, 0.0f, true);
    }

    private boolean renderResources(HavocRhi rhi,
                                    RhiStorageImage destination,
                                    GpuTextureView currentColor,
                                    GpuTextureView currentDepth,
                                    Matrix4fc currentViewSource,
                                    Matrix4fc currentProjectionSource,
                                    Vec3 currentCamera,
                                    float depthNdcScale,
                                    float depthNdcBias,
                                    float backgroundDepth,
                                    boolean greaterDepthIsCloser) {
        Matrix4f currentView = currentViewSource == null ? null : new Matrix4f(currentViewSource);
        Matrix4f currentProjection = currentProjectionSource == null ? null : new Matrix4f(currentProjectionSource);
        if (currentView == null || currentProjection == null
                || currentCamera == null || currentDepth == null) {
            capture(currentView, currentProjection, currentCamera);
            backend.invalidateHistory();
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        boolean sameWorld = mc != null && previousWorld == mc.level;
        boolean usableHistory = historyValid && sameWorld && previousView != null && previousProjection != null
                && previousCameraPosition != null && currentCamera.distanceToSqr(previousCameraPosition) < 4096.0;
        Matrix4f reprojectionView = usableHistory ? previousView : currentView;
        Matrix4f reprojectionProjection = usableHistory ? previousProjection : currentProjection;
        Vec3 cameraDelta = usableHistory ? currentCamera.subtract(previousCameraPosition) : Vec3.ZERO;
        try {
            MainConfig config = MainConfig.get();
            backend.render(
                    rhi, destination, currentColor, currentDepth,
                    currentView, currentProjection, reprojectionView, reprojectionProjection,
                    cameraDelta, usableHistory,
                    depthNdcScale, depthNdcBias, backgroundDepth, greaterDepthIsCloser,
                    config.isTaaFxaaEnabled(), config.isTaaSharpenEnabled(),
                    config.getTaaSharpeningIntensity());
            capture(currentView, currentProjection, currentCamera);
            PostProcessExecutionPolicy.logComputeActive("taa", "Temporal AA");
            return true;
        } catch (Throwable t) {
            computeSupported = false;
            backend.close();
            invalidateHistory();
            DebugLog.error("[Havoc] TAA compute path failed", t);
            return false;
        }
    }

    private void capture(Matrix4f view, Matrix4f projection, Vec3 cameraPosition) {
        if (view == null || projection == null || cameraPosition == null) {
            invalidateHistory();
            return;
        }
        previousView = new Matrix4f(view);
        previousProjection = new Matrix4f(projection);
        previousCameraPosition = cameraPosition;
        Minecraft mc = Minecraft.getInstance();
        previousWorld = mc != null ? mc.level : null;
        historyValid = true;
    }

    /** Latest canonical TAA history, before optional presentation filtering. */
    public GpuTextureView currentHistoryColorView() {
        return backend.currentHistoryColorView();
    }

    public void invalidateHistory() {
        previousView = null;
        previousProjection = null;
        previousCameraPosition = null;
        previousWorld = null;
        historyValid = false;
        backend.invalidateHistory();
    }

    @Override
    public void releaseBackendResources(HavocRhi owner) {
        backend.release(owner);
        computeSupported = true;
        invalidateHistory();
    }
}
