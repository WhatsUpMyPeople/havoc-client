/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.accessors;

import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {

    @Accessor("levelRenderState")
    LevelRenderState havoc$getWorldRenderState();

    @Accessor("skyRenderer")
    SkyRenderer havoc$getSkyRendering();

    @Accessor("submitNodeStorage")
    SubmitNodeStorage havoc$getEntityRenderCommandQueue();

    @Accessor("featureRenderDispatcher")
    FeatureRenderDispatcher havoc$getEntityRenderDispatcher();

    @Accessor("renderBuffers")
    RenderBuffers havoc$getRenderBuffers();

    @Accessor("renderBuffers")
    void havoc$setRenderBuffers(RenderBuffers renderBuffers);

    @Accessor("chunkLayerSampler")
    GpuSampler havoc$getTerrainSampler();

    @Accessor("chunkLayerSampler")
    void havoc$setTerrainSampler(GpuSampler terrainSampler);

    @Invoker("submitEntities")
    void havoc$invokeSubmitEntities(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                        LevelRenderState levelRenderState,
                                        SubmitNodeCollector collector);

}
