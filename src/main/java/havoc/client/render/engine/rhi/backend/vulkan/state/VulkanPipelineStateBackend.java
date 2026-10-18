/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rhi.backend.vulkan.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import havoc.client.mixininterface.IRenderPipeline;
import havoc.client.render.engine.rhi.clip.ShapeClipBackend;
import havoc.client.render.engine.rhi.clip.ShapeClipRenderPassContract;
import havoc.client.render.engine.rhi.msaa.MsaaControl;
import havoc.client.render.engine.rhi.state.PipelineStateBackend;

/**
 * Vulkan pipeline state bridge. Fixed Vulkan state is injected while pipelines are compiled.
 */
public final class VulkanPipelineStateBackend implements PipelineStateBackend {
    private final MsaaControl msaa;
    private final ShapeClipBackend clip;

    public VulkanPipelineStateBackend(MsaaControl msaa, ShapeClipBackend clip) {
        this.msaa = msaa;
        this.clip = clip;
    }

    @Override
    public void applyPipelineState(RenderPipeline pipeline) {
        msaa.applyPipelineState(pipeline);

        ShapeClipRenderPassContract contract = ShapeClipRenderPassContract.NONE;
        if (pipeline instanceof IRenderPipeline havocPipeline) {
            contract = havocPipeline.havoc$getShapeClipContract();
        }
        clip.bindPipeline(pipeline, contract);
        clip.applyNativeState();
    }

    @Override
    public void resetRenderPassState() {
        msaa.resetPipelineState();
        clip.endRenderPass();
    }
}
