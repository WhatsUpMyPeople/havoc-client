/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import havoc.client.render.engine.core.ViewportContext;
import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.engine.renderer.Renderer2D.Deferred2DLayer;
import havoc.client.render.engine.renderer.ui.clip.UiClipSnapshot;
import havoc.client.render.engine.renderer.ui.clip.UiScissorSnapshot;
import havoc.client.render.engine.uniform.MeshBuilder;

public record DeferredTexturedSubmit(Deferred2DLayer layer, ViewportContext viewport,
                                     UiScissorSnapshot scissorSnapshot, UiClipSnapshot clipSnapshot, String samplerName,
                                     GpuTextureView samplerView, GpuSampler sampler, RenderPipeline pipeline,
                                     MeshBuilder mesh) implements Deferred2DSubmit {
    @Override
    public void submit() {
        if (mesh == null || samplerView == null || sampler == null) return;
        UiClipSnapshot clip = clipSnapshot != null ? clipSnapshot : UiClipSnapshot.NONE;
        String resolvedSamplerName = samplerName != null ? samplerName : "u_Texture";
        UiRenderDispatcher.submitImmediate(
                "Renderer2D.TEXTURE.deferred",
                1,
                (context, rhi) -> UiDirectTexturedRenderer.draw(
                        mesh, pipeline, resolvedSamplerName, samplerView, sampler, clip, layer, rhi)
        );
        Renderer2D.flushUiLayer();
    }

    @Override
    public void release() {
        if (mesh != null) {
            mesh.close();
        }
    }
}
