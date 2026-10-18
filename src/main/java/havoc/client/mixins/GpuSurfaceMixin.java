/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.CommandEncoderBackend;
import com.mojang.blaze3d.systems.GpuSurface;
import com.mojang.blaze3d.systems.GpuSurfaceBackend;
import com.mojang.blaze3d.systems.SurfaceException;
import com.mojang.blaze3d.textures.GpuTextureView;
import havoc.client.render.engine.animation.AnimationUtility;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.profiler.ProfilerPhase;
import havoc.client.render.engine.profiler.TracyGpuProfiler;
import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.engine.renderer.ui.UiBlurResources;
import havoc.client.util.FastFps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GpuSurface.class)
public abstract class GpuSurfaceMixin {
    /**
     * Last safe point before Minecraft copies the main render target into the
     * window surface. Havoc submission must be closed before the blit.
     */
    @Inject(method = "blitFromTexture", at = @At("HEAD"))
    private void havoc$beforeSurfaceBlit(CommandEncoder encoder, GpuTextureView textureView, CallbackInfo info) {
        HavocRenderSystem.endRenderSubmission();
    }

    @Inject(method = "present", at = @At("HEAD"))
    private void havoc$onPresentHead(CallbackInfo info) {
        FastFps.onFrame();
        AnimationUtility.onFrame();
    }

    @Redirect(
            method = "configure",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuSurfaceBackend;configure(Lcom/mojang/blaze3d/systems/GpuSurface$Configuration;)V")
    )
    private void havoc$profileSurfaceConfigure(GpuSurfaceBackend backend, GpuSurface.Configuration configuration) throws SurfaceException {
        try (ProfilerPhase.Scope ignored = ProfilerPhase.scope("surface:configure")) {
            backend.configure(configuration);
        }
    }

    @Redirect(
            method = "acquireNextTexture",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuSurfaceBackend;acquireNextTexture()V")
    )
    private void havoc$profileSurfaceAcquire(GpuSurfaceBackend backend) throws SurfaceException {
        try (ProfilerPhase.Scope ignored = ProfilerPhase.scope("surface:acquire")) {
            backend.acquireNextTexture();
        }
    }

    @Redirect(
            method = "blitFromTexture",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuSurfaceBackend;blitFromTexture(Lcom/mojang/blaze3d/systems/CommandEncoderBackend;Lcom/mojang/blaze3d/textures/GpuTextureView;)V")
    )
    private void havoc$profileSurfaceBlit(GpuSurfaceBackend backend, CommandEncoderBackend encoder, GpuTextureView textureView) {
        try (ProfilerPhase.Scope ignored = ProfilerPhase.scope("surface:blit")) {
            backend.blitFromTexture(encoder, textureView);
        } catch (RuntimeException | Error failure) {
            // A lost/minimized surface can fail before present() is reached. Retire Havoc's
            // frame here as well so transient resources and logical frame identity cannot leak.
            havoc$finishFrameBoundary();
            throw failure;
        }
    }

    @Redirect(
            method = "present",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuSurfaceBackend;present()V")
    )
    private void havoc$profileSurfacePresent(GpuSurfaceBackend backend) {
        try {
            try (ProfilerPhase.Scope ignored = ProfilerPhase.scope("surface:present")) {
                backend.present();
            }
        } finally {
            // Surface loss/minimize may throw out of backend.present(). Havoc must still retire
            // the logical frame or the next world render reuses stale frame id/uniform/transient state.
            havoc$finishFrameBoundary();
        }
    }

    private void havoc$finishFrameBoundary() {
        if (TracyGpuProfiler.isEnabled()) {
            TracyGpuProfiler.onFrameEnd();
        }
        HavocRenderSystem.onFramePresented();
        Renderer2D.getBatchStats().onFrameStart();
        UiBlurResources.invalidateWorldSource();
    }
}
