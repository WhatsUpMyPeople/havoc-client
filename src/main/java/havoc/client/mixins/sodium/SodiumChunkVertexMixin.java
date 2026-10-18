/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.render.sodium.terrain.HavocChunkVertexExtension;

@Mixin(ChunkVertexEncoder.Vertex.class)
public abstract class SodiumChunkVertexMixin implements HavocChunkVertexExtension {
    @Unique
    private int havoc$surfaceFlags;
    @Unique
    private int havoc$materialId;
    @Unique
    private int havoc$materialMapPresenceMask;
    @Unique
    private int havoc$materialFeatureMask;
    @Unique
    private int havoc$packedScalarSurface;

    @Inject(method = "copyVertexTo", at = @At("HEAD"))
    private static void havoc$copyVertexData(ChunkVertexEncoder.Vertex from, ChunkVertexEncoder.Vertex to, CallbackInfo ci) {
        ((HavocChunkVertexExtension) from).havoc$copyData((HavocChunkVertexExtension) to);
    }

    @Override
    public void havoc$setSurfaceFlags(int surfaceFlags) {
        this.havoc$surfaceFlags = surfaceFlags;
    }

    @Override
    public int havoc$getSurfaceFlags() {
        return this.havoc$surfaceFlags;
    }

    @Override
    public void havoc$setMaterialData(int materialId, int mapPresenceMask, int featureMask, int packedScalarSurface) {
        this.havoc$materialId = materialId;
        this.havoc$materialMapPresenceMask = mapPresenceMask & 0xFF;
        this.havoc$materialFeatureMask = featureMask & 0xFFFF;
        this.havoc$packedScalarSurface = packedScalarSurface;
    }

    @Override
    public int havoc$getMaterialId() {
        return this.havoc$materialId;
    }

    @Override
    public int havoc$getMaterialMapPresenceMask() {
        return this.havoc$materialMapPresenceMask;
    }

    @Override
    public int havoc$getMaterialFeatureMask() {
        return this.havoc$materialFeatureMask;
    }

    @Override
    public int havoc$getPackedScalarSurface() {
        return this.havoc$packedScalarSurface;
    }

    @Override
    public void havoc$copyData(HavocChunkVertexExtension dest) {
        dest.havoc$setSurfaceFlags(this.havoc$surfaceFlags);
        dest.havoc$setMaterialData(
                this.havoc$materialId,
                this.havoc$materialMapPresenceMask,
                this.havoc$materialFeatureMask,
                this.havoc$packedScalarSurface
        );
    }
}
