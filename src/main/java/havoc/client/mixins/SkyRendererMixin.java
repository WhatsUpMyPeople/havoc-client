/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.render.engine.msaa.MsaaWorldTarget;
import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.visuals.NoRender;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Shadow
    @Final
    private RenderTarget renderTarget;

    @Redirect(
            method = {
                    "renderSkyDisc",
                    "renderDarkDisc",
                    "renderSun",
                    "renderMoon",
                    "renderStars",
                    "renderSunriseAndSunset",
                    "renderEndSky",
                    "renderEndFlash"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/SkyRenderer;renderTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private RenderTarget havoc$currentWorldTarget(SkyRenderer instance) {
        RenderTarget msaa = MsaaWorldTarget.getFramebufferOverride();
        return msaa != null ? msaa : renderTarget;
    }

@Unique
    private static boolean havoc$noRenderOff(String key) {
        NoRender noRender = Modules.get(NoRender.class);
        return noRender != null && noRender.offWorld(key);
    }

    @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
    private void havoc$noRenderSkyDisc(int color, CallbackInfo ci) {
        if (havoc$noRenderOff("sky")) ci.cancel();
    }

    @Inject(method = "renderEndSky", at = @At("HEAD"), cancellable = true)
    private void havoc$noRenderEndSky(CallbackInfo ci) {
        if (havoc$noRenderOff("sky")) ci.cancel();
    }

    @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
    private void havoc$noRenderSun(float alpha, PoseStack poseStack, CallbackInfo ci) {
        if (havoc$noRenderOff("sun")) ci.cancel();
    }

    @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
    private void havoc$noRenderMoon(MoonPhase moonPhase, float alpha, PoseStack poseStack, CallbackInfo ci) {
        if (havoc$noRenderOff("moon")) ci.cancel();
    }

    @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
    private void havoc$noRenderStars(float alpha, PoseStack poseStack, CallbackInfo ci) {
        if (havoc$noRenderOff("stars")) ci.cancel();
    }

    @Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
    private void havoc$noRenderSunrise(PoseStack poseStack, float angle, int color, CallbackInfo ci) {
        if (havoc$noRenderOff("sun")) ci.cancel();
    }
}
