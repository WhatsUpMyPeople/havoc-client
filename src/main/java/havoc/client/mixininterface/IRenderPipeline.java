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

package havoc.client.mixininterface;

import havoc.client.render.engine.rhi.clip.ShapeClipRenderPassContract;
import havoc.client.render.engine.pipeline.RenderPipelineContract;
import havoc.client.render.engine.rhi.pipeline.PipelineMetadata;

public interface IRenderPipeline {
    void havoc$setLineSmooth(boolean lineSmooth);

    boolean havoc$getLineSmooth();

    void havoc$setShapeClipContract(ShapeClipRenderPassContract contract);

    ShapeClipRenderPassContract havoc$getShapeClipContract();

    void havoc$setContract(RenderPipelineContract contract);

    RenderPipelineContract havoc$getContract();

    void havoc$setMetadata(PipelineMetadata metadata);

    PipelineMetadata havoc$getMetadata();
}
