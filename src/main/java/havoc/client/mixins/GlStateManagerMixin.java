/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.mojang.blaze3d.opengl.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import havoc.client.render.engine.profiler.GlSyncTracker;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
    @Inject(method = "_glFenceSync", at = @At("RETURN"))
    private static void havoc$onFenceSync(int condition, int flags, CallbackInfoReturnable<Long> cir) {
        GlSyncTracker.onFence(cir.getReturnValue());
    }

    @Inject(method = "_glClientWaitSync", at = @At("HEAD"))
    private static void havoc$onClientWaitSync(long sync, int flags, long timeout, CallbackInfoReturnable<Integer> cir) {
        GlSyncTracker.onWaitStart(sync);
    }

    @Inject(method = "_glClientWaitSync", at = @At("RETURN"))
    private static void havoc$onClientWaitSyncReturn(long sync, int flags, long timeout, CallbackInfoReturnable<Integer> cir) {
        GlSyncTracker.onWaitEnd();
    }
}
