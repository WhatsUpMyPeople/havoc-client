/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.sodium;

import com.mojang.blaze3d.textures.GpuSampler;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.core.HavocWorldMatrices;
import havoc.client.render.engine.postprocess.TemporalAntiAliasingPass;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Observes Sodium's terrain boundary and keeps its raster projection in the same TAA space
 * as vanilla/entity feature rendering without cancelling or replacing the production draw.
 */
@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class SodiumWorldRendererTerrainMixin {
    /**
     * Sodium snapshots the projection in its own GameRenderer mixin and later passes that
     * snapshot through ChunkRenderMatrices. Havoc jitters the RenderSystem projection at
     * the same call site, so relying on wrapper order can leave terrain and vanilla/entity
     * rendering in different projection spaces. Replace only the raster matrix here with the
     * exact effective projection already published to the vanilla path. Culling stays stable.
     */
    @ModifyVariable(
            method = "renderLayer(Lnet/caffeinemc/mods/sodium/client/render/chunk/ChunkRenderMatrices;Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;DDDLnet/caffeinemc/mods/sodium/client/util/FogParameters;Lcom/mojang/blaze3d/textures/GpuSampler;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            remap = false
    )
    private ChunkRenderMatrices havoc$syncTaaTerrainProjection(ChunkRenderMatrices matrices) {
        if (matrices == null || !TemporalAntiAliasingPass.shouldJitter() || !HavocWorldMatrices.isValid()) {
            return matrices;
        }

        var projection = HavocWorldMatrices.renderProjectionMatrix();
        if (projection == null) {
            return matrices;
        }
        return new ChunkRenderMatrices(projection, matrices.modelView());
    }

    @Inject(method = "renderLayer(Lnet/caffeinemc/mods/sodium/client/render/chunk/ChunkRenderMatrices;Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;DDDLnet/caffeinemc/mods/sodium/client/util/FogParameters;Lcom/mojang/blaze3d/textures/GpuSampler;)V", at = @At("HEAD"), remap = false)
    private void havoc$beforeTerrainLayer(ChunkRenderMatrices matrices,
                                              TerrainRenderPass pass,
                                              double cameraX,
                                              double cameraY,
                                              double cameraZ,
                                              FogParameters fog,
                                              GpuSampler sampler,
                                              CallbackInfo ci) {
        HavocRenderSystem.sodium().terrainInterop().beforeTerrainDraw(
                matrices, pass, cameraX, cameraY, cameraZ, fog, sampler);
    }

    @Inject(method = "renderLayer(Lnet/caffeinemc/mods/sodium/client/render/chunk/ChunkRenderMatrices;Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;DDDLnet/caffeinemc/mods/sodium/client/util/FogParameters;Lcom/mojang/blaze3d/textures/GpuSampler;)V", at = @At("RETURN"), remap = false)
    private void havoc$afterTerrainLayer(ChunkRenderMatrices matrices,
                                             TerrainRenderPass pass,
                                             double cameraX,
                                             double cameraY,
                                             double cameraZ,
                                             FogParameters fog,
                                             GpuSampler sampler,
                                             CallbackInfo ci) {
        HavocRenderSystem.sodium().terrainInterop().afterTerrainDraw(
                matrices, pass, cameraX, cameraY, cameraZ, fog, sampler);
    }
}
