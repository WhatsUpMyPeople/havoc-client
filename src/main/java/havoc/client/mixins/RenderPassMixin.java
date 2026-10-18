/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.util.logging.DebugLog;

@Mixin(RenderPass.class)
public abstract class RenderPassMixin {
    @Inject(method = "setPipeline", at = @At("RETURN"))
    private void havoc$applyRhiPipelineState(RenderPipeline pipeline, CallbackInfo ci) {
        try {
            HavocRenderSystem.rhi().pipelineState().applyPipelineState(pipeline);
        } catch (Throwable t) {
            DebugLog.error("[ShapeClip] pipeline state hook failed for " + pipeline, t);
        }
    }
}
