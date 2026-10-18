/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.mojang.blaze3d.opengl.GlTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.mixininterface.IMsaaTexture;
import havoc.client.render.engine.core.HavocRenderSystem;

@Mixin(GlTexture.class)
public abstract class GlTextureMixin implements IMsaaTexture {
    @Final
    @Shadow
    protected int id;

    @Unique
    private int havoc$samples = 1;

    @Override
    public void havoc$setSamples(int samples) {
        havoc$samples = samples;
    }

    @Override
    public int havoc$getSamples() {
        return havoc$samples;
    }

    @Override
    public boolean havoc$isMsaa() {
        return havoc$samples > 1;
    }

    @Override
    public int havoc$getGlId() {
        return id;
    }

    @Inject(method = "destroyImmediately", at = @At("HEAD"))
    private void havoc$onFree(CallbackInfo ci) {
        HavocRenderSystem.rhi().msaa().unregisterTexture(id);
    }
}




