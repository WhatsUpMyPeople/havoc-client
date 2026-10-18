/*
 * This file is part of the Havoc Client distribution.
 * Havoc modifications copyright (c) 2026 pivosos2007.
 *
 * Portions of this file are based on, adapted from, or implemented
 * with reference to Meteor Client
 * (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Licensed under the GNU General Public License v3.0.
 * See THIRD_PARTY_NOTICES.md for details.
 */

package havoc.client.mixins;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import havoc.client.mixininterface.IRenderPipeline;
import havoc.client.render.engine.rhi.clip.ShapeClipRenderPassContract;
import havoc.client.render.engine.pipeline.RenderPipelineContract;
import havoc.client.render.engine.rhi.pipeline.PipelineDomain;
import havoc.client.render.engine.rhi.pipeline.PipelineMetadata;

@Mixin(RenderPipeline.class)
public abstract class RenderPipelineMixin implements IRenderPipeline {
    @Unique
    private boolean havoc$lineSmooth;

    @Unique
    private ShapeClipRenderPassContract havoc$shapeClipContract = ShapeClipRenderPassContract.NONE;

    @Unique
    private RenderPipelineContract havoc$contract = RenderPipelineContract.EXTENDED;

    @Unique
    private PipelineMetadata havoc$metadata = PipelineMetadata.builder(PipelineDomain.UNKNOWN).build();

    @Override
    public void havoc$setLineSmooth(boolean lineSmooth) {
        this.havoc$lineSmooth = lineSmooth;
    }

    @Override
    public boolean havoc$getLineSmooth() {
        return havoc$lineSmooth;
    }

    @Override
    public void havoc$setShapeClipContract(ShapeClipRenderPassContract contract) {
        this.havoc$shapeClipContract = contract == null ? ShapeClipRenderPassContract.NONE : contract;
    }

    @Override
    public ShapeClipRenderPassContract havoc$getShapeClipContract() {
        return havoc$shapeClipContract;
    }

    @Override
    public void havoc$setContract(RenderPipelineContract contract) {
        this.havoc$contract = contract == null ? RenderPipelineContract.EXTENDED : contract;
    }

    @Override
    public RenderPipelineContract havoc$getContract() {
        return havoc$contract;
    }

    @Override
    public void havoc$setMetadata(PipelineMetadata metadata) {
        this.havoc$metadata = metadata != null ? metadata : PipelineMetadata.builder(PipelineDomain.UNKNOWN).build();
    }

    @Override
    public PipelineMetadata havoc$getMetadata() {
        return havoc$metadata;
    }
}
