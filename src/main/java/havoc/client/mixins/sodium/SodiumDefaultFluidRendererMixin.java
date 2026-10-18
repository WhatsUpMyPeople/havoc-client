/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.sodium;

import havoc.client.render.engine.material.MaterialClassification;
import havoc.client.render.engine.material.MaterialRegistry;
import havoc.client.render.engine.material.MaterialSurfaceDescriptor;
import havoc.client.render.sodium.terrain.HavocChunkVertexExtension;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds Havoc's exact fluid/material contract to Sodium's default quad producer. */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer")
public abstract class SodiumDefaultFluidRendererMixin {
    @Shadow(remap = false)
    @Final
    private ChunkVertexEncoder.Vertex[] vertices;

    @Inject(
            method = "writeQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;isTranslucent()Z",
                    shift = At.Shift.BEFORE
            ),
            remap = false
    )
    private void havoc$stampFluidMaterial(ChunkModelBuilder builder,
                                              TranslucentGeometryCollector collector,
                                              Material sodiumMaterial,
                                              BlockPos renderOrigin,
                                              ModelQuadView quad,
                                              ModelQuadFacing facing,
                                              boolean reversed,
                                              CallbackInfo ci) {
        havoc.client.render.sodium.fluid.FluidSurfaceContext.Entry context = havoc.client.render.sodium.fluid.FluidSurfaceContext.current();
        if (context == null || quad == null) return;

        // The material producer and Havoc's vertex encoder form one contract. Iris owns the
        // Sodium terrain format while its pipeline is active, so ordinary Sodium vertices do not
        // carry this extension. Keep this boundary fail-safe in case another renderer changes the
        // mixin composition independently of HavocMixinPlugin.
        for (ChunkVertexEncoder.Vertex vertex : vertices) {
            if (!(vertex instanceof HavocChunkVertexExtension)) return;
        }

        TextureAtlasSprite sprite = quad.getSprite();
        MaterialClassification classification = context.classification();
        MaterialSurfaceDescriptor descriptor = MaterialRegistry.global().resolve(sprite, classification);
        int mapMask = MaterialRegistry.global().gpuPresenceMask(descriptor);
        int featureMask = descriptor.gpuFeatureMask16();
        int packedSurface = descriptor.packScalarSurface();
        for (ChunkVertexEncoder.Vertex vertex : vertices) {
            HavocChunkVertexExtension extension = (HavocChunkVertexExtension) vertex;
            extension.havoc$setSurfaceFlags(0);
            extension.havoc$setMaterialData(descriptor.stableId(), mapMask, featureMask, packedSurface);
        }

    }
}
