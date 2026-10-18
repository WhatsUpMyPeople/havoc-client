/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins.sodium;

import havoc.client.render.engine.material.MaterialClassification;
import havoc.client.render.engine.material.MaterialClassifier;
import havoc.client.render.engine.material.MaterialDomain;
import havoc.client.render.engine.material.MaterialRegistry;
import havoc.client.render.engine.material.MaterialSurfaceDescriptor;
import havoc.client.render.sodium.fluid.FluidSurfaceMetadataCapture;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;

/**
 * Non-invasive block/material observer. It preserves explicit height-displacement metadata for
 * future Iris material integration but never cancels or replaces Sodium geometry.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer")
public abstract class SodiumBlockMetadataCaptureMixin {
    private static final ThreadLocal<ArrayDeque<Entry>> HAVOC_CONTEXT =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "renderModel", at = @At("HEAD"), remap = false)
    private void havoc$pushBlockMetadata(BlockStateModel model,
                                              BlockState state,
                                              BlockPos pos,
                                              BlockPos origin,
                                              CallbackInfo ci) {
        HAVOC_CONTEXT.get().addLast(new Entry(
                state,
                pos == null ? null : pos.immutable(),
                origin == null ? null : origin.immutable()
        ));
    }

    @Inject(method = "renderModel", at = @At("RETURN"), remap = false)
    private void havoc$popBlockMetadata(BlockStateModel model,
                                             BlockState state,
                                             BlockPos pos,
                                             BlockPos origin,
                                             CallbackInfo ci) {
        ArrayDeque<Entry> stack = HAVOC_CONTEXT.get();
        if (!stack.isEmpty()) stack.removeLast();
        if (stack.isEmpty()) HAVOC_CONTEXT.remove();
    }

    @Inject(
            method = "bufferQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;isTranslucent()Z",
                    shift = At.Shift.BEFORE
            ),
            remap = false
    )
    private void havoc$captureHeightMetadata(MutableQuadViewImpl quad,
                                                  float[] brightness,
                                                  Material material,
                                                  CallbackInfo ci) {
        ArrayDeque<Entry> stack = HAVOC_CONTEXT.get();
        Entry entry = stack.isEmpty() ? null : stack.peekLast();
        if (entry == null || entry.worldPos == null || quad == null) return;

        TextureAtlasSprite sprite = quad.sprite(SpriteFinderCache.forBlockAtlas());
        MaterialClassification classification = MaterialClassifier.classifyBlock(entry.state, havoc$domain(material));
        MaterialSurfaceDescriptor descriptor = MaterialRegistry.global().resolve(sprite, classification);
        FluidSurfaceMetadataCapture.captureHeight(entry.worldPos, descriptor);
    }

    private static MaterialDomain havoc$domain(Material material) {
        if (material == null || material.pass == null) return MaterialDomain.UNKNOWN;
        if (material.pass == DefaultTerrainRenderPasses.SOLID) return MaterialDomain.OPAQUE;
        if (material.pass == DefaultTerrainRenderPasses.CUTOUT) return MaterialDomain.CUTOUT;
        if (material.pass == DefaultTerrainRenderPasses.TRANSLUCENT) return MaterialDomain.TRANSLUCENT;
        return MaterialDomain.UNKNOWN;
    }

    private record Entry(BlockState state, BlockPos worldPos, BlockPos renderOrigin) { }
}
