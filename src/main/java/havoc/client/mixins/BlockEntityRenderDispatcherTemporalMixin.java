/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins;

import havoc.client.mixininterface.ITemporalMotionRenderState;
import havoc.client.render.engine.temporal.TemporalMotionRuntime;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherTemporalMixin {
    @Inject(method = "tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Z)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;", at = @At("RETURN"))
    private void havoc$captureTemporalMotion(BlockEntity blockEntity, float tickProgress,
                                                  ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
                                                  boolean renderBoundingBox,
                                                  CallbackInfoReturnable<BlockEntityRenderState> cir) {
        BlockEntityRenderState state = cir.getReturnValue();
        if (state instanceof ITemporalMotionRenderState temporalState) {
            temporalState.havoc$setTemporalMotionState(TemporalMotionRuntime.captureBlockEntity(blockEntity, state));
        }
    }
}
