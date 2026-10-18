/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.visuals.NoRender;
import havoc.client.util.block.BlockObservationHub;
import havoc.client.render.engine.light.BlockLightChangeTracker;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "setServerVerifiedBlockState", at = @At("TAIL"))
    private void havoc$recordBlockUpdate(BlockPos pos, BlockState state, @Block.UpdateFlags int flags, CallbackInfo ci) {
        BlockObservationHub.observeWorldUpdate(pos, state);
    }

    @Inject(method = "onChunkLoaded", at = @At("TAIL"))
    private void havoc$invalidateBlockLightOnChunkLoad(ChunkPos pos, CallbackInfo ci) {
        BlockLightChangeTracker.markChanged();
    }

    @Inject(method = "unload", at = @At("TAIL"))
    private void havoc$invalidateBlockLightOnChunkUnload(LevelChunk chunk, CallbackInfo ci) {
        BlockLightChangeTracker.markChanged();
    }

    @Inject(
            method = "doAddParticle(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void havoc$noRenderEatParticles(
            ParticleOptions effect,
            boolean force,
            boolean canSpawnOnMinimal,
            double x, double y, double z,
            double vx, double vy, double vz,
            CallbackInfo ci
    ) {
        NoRender nr = Modules.get(NoRender.class);
        if (nr == null || !nr.isEnabled()) return;

        // частицы еды / питья
        if (nr.off("eat_particles")
                && effect instanceof ItemParticleOption) {
            ci.cancel();
        }

        if (nr.off("hit_particles")) {
            var type = effect.getType();
            if (type == ParticleTypes.DAMAGE_INDICATOR
                    || type == ParticleTypes.CRIT
                    || type == ParticleTypes.ENCHANTED_HIT) {
                ci.cancel();
            }
        }

        if (nr.off("sweep_particles")) {
            var type = effect.getType();
            if (type == ParticleTypes.SWEEP_ATTACK) {
                ci.cancel();
            }
        }
    }

@Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    private void havoc$noRenderBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        NoRender noRender = Modules.get(NoRender.class);
        if (noRender != null
                && (noRender.offParticle("all_particles") || noRender.offParticle("block_break_particles"))) {
            ci.cancel();
        }
    }

    @Inject(method = "addBreakingBlockEffect", at = @At("HEAD"), cancellable = true)
    private void havoc$noRenderBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        NoRender noRender = Modules.get(NoRender.class);
        if (noRender != null
                && (noRender.offParticle("all_particles") || noRender.offParticle("block_breaking_particles"))) {
            ci.cancel();
        }
    }

    @org.spongepowered.asm.mixin.injection.Redirect(
            method = "tickWeatherEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void havoc$noRenderRainSplash(
            ClientLevel level,
            ParticleOptions effect,
            double x, double y, double z,
            double vx, double vy, double vz
    ) {
        NoRender noRender = Modules.get(NoRender.class);
        if (noRender != null
                && (noRender.offParticle("all_particles") || noRender.offParticle("rain_splash_particles"))) {
            return;
        }
        level.addParticle(effect, x, y, z, vx, vy, vz);
    }
}
