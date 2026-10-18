/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import havoc.client.render.engine.material.MaterialClassification;
import havoc.client.render.engine.material.MaterialDomain;
import havoc.client.render.engine.material.MaterialRegistry;
import havoc.client.render.engine.material.MaterialSurfaceDescriptor;
import havoc.client.render.helpers.SodiumSurfaceFlagContext;
import havoc.client.render.sodium.terrain.HavocChunkVertexExtension;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer")
public abstract class SodiumBlockRendererMixin {


    @Inject(method = "renderModel", at = @At("HEAD"), remap = false)
    private void havoc$pushSurfaceState(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
        SodiumSurfaceFlagContext.pushForState(state, pos, origin);
    }

    @Inject(method = "renderModel", at = @At("RETURN"), remap = false)
    private void havoc$popSurfaceState(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
        SodiumSurfaceFlagContext.pop();
    }

    @Inject(
            method = "bufferQuad",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;light:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER),
            remap = false
    )
    private void havoc$writeSurfaceContract(MutableQuadViewImpl quad,
                                                 float[] brightness,
                                                 Material material,
                                                 CallbackInfo ci,
                                                 @Local(ordinal = 0) ChunkVertexEncoder.Vertex vertex) {
        HavocChunkVertexExtension extension = (HavocChunkVertexExtension) vertex;
        extension.havoc$setSurfaceFlags(SodiumSurfaceFlagContext.getSurfaceFlags(vertex.y));

        TextureAtlasSprite sprite = quad.sprite(SpriteFinderCache.forBlockAtlas());
        MaterialClassification classification = SodiumSurfaceFlagContext.materialClassification(havoc$domain(material));
        MaterialSurfaceDescriptor descriptor = MaterialRegistry.global().resolve(sprite, classification);
        extension.havoc$setMaterialData(
                descriptor.stableId(),
                MaterialRegistry.global().gpuPresenceMask(descriptor),
                descriptor.gpuFeatureMask16(),
                descriptor.packScalarSurface()
        );
    }


    private static MaterialDomain havoc$domain(Material material) {
        if (material == null || material.pass == null) return MaterialDomain.UNKNOWN;
        if (material.pass == DefaultTerrainRenderPasses.SOLID) return MaterialDomain.OPAQUE;
        if (material.pass == DefaultTerrainRenderPasses.CUTOUT) return MaterialDomain.CUTOUT;
        if (material.pass == DefaultTerrainRenderPasses.TRANSLUCENT) return MaterialDomain.TRANSLUCENT;
        return MaterialDomain.UNKNOWN;
    }
}
