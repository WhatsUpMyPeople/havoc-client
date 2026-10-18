/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import havoc.client.render.ViewObstructionFadeContext;
import havoc.client.render.engine.msaa.MsaaWorldTarget;

import java.util.Arrays;

@Mixin(SubmitNodeCollection.class)
public abstract class BatchingRenderCommandQueueMixin {
    @Unique
    private static final int HAVOC_VIEW_FADE_DITHER_MARK = 0x00010001;
    @Unique
    private static final int HAVOC_VIEW_FADE_A2C_MARK = 0x00010100;

    @Unique
    private static int havoc$markViewObstructionFade(int argb, float alpha, boolean msaaActive) {
        int rgb = (argb & 0x00FEFEFE) | (msaaActive ? HAVOC_VIEW_FADE_A2C_MARK : HAVOC_VIEW_FADE_DITHER_MARK);
        int alphaByte = Math.round(255.0f * Mth.clamp(alpha, 0.0f, 1.0f));
        return rgb | ((alphaByte & 0xFF) << 24);
    }

    @Unique
    private static boolean havoc$shouldApplyViewObstructionFade() {
        return ViewObstructionFadeContext.isActive() && ViewObstructionFadeContext.alpha() < 0.99f;
    }

    @Unique
    private static int havoc$applyFadeToArgb(int color) {
        int baseColor = color == -1 ? 0xFFFFFFFF : ((color & 0x00FFFFFF) | 0xFF000000);
        return havoc$markViewObstructionFade(baseColor, ViewObstructionFadeContext.alpha(), MsaaWorldTarget.isActive());
    }

    @ModifyVariable(method = "submitModel", at = @At("HEAD"), argsOnly = true, index = 7)
    private int havoc$applyViewObstructionFadeAlpha(int tintedColor) {
        if (!havoc$shouldApplyViewObstructionFade()) {
            return tintedColor;
        }
        return havoc$applyFadeToArgb(tintedColor);
    }

    @ModifyVariable(method = "submitItem", at = @At("HEAD"), argsOnly = true, index = 6)
    private int[] havoc$applyViewObstructionFadeAlphaToItemTints(int[] tints) {
        if (!havoc$shouldApplyViewObstructionFade()) {
            return tints;
        }

        int sentinel = havoc$applyFadeToArgb(-1);
        if (tints == null || tints.length == 0) {
            return new int[]{sentinel};
        }

        int[] faded = Arrays.copyOf(tints, tints.length);
        faded[0] = havoc$applyFadeToArgb(faded[0]);
        for (int i = 1; i < faded.length; i++) {
            faded[i] = havoc$applyFadeToArgb(faded[i]);
        }
        return faded;
    }

}
