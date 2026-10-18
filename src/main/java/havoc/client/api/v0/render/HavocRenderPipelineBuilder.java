/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.api.v0.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import havoc.client.render.engine.pipeline.HavocRenderPipelines;
import havoc.client.render.engine.pipeline.DepthTestFunction;
import havoc.client.render.engine.pipeline.ExtendedRenderPipelineBuilder;
import havoc.client.render.engine.pipeline.RenderPipelineContract;
import havoc.client.render.engine.rhi.clip.ShapeClipRenderPassContract;
import havoc.client.render.engine.rhi.pipeline.PipelineDomain;
import havoc.client.render.engine.rhi.pipeline.TransformPolicy;
import net.minecraft.resources.Identifier;

@SuppressWarnings("unused")
public final class HavocRenderPipelineBuilder {
    private final ExtendedRenderPipelineBuilder delegate;

    private HavocRenderPipelineBuilder(RenderPipeline.Snippet... snippets) {
        this.delegate = new ExtendedRenderPipelineBuilder(snippets);
    }

    public static HavocRenderPipelineBuilder create(RenderPipeline.Snippet... snippets) {
        return new HavocRenderPipelineBuilder(snippets);
    }

    public static RenderPipeline.Snippet meshUniforms() {
        return HavocRenderPipelines.meshUniforms();
    }

    public HavocRenderPipelineBuilder location(Identifier id) {
        delegate.withLocation(id);
        return this;
    }

    public HavocRenderPipelineBuilder vertexFormat(VertexFormat format, PrimitiveTopology mode) {
        delegate.withVertexFormat(format, mode);
        return this;
    }

    public HavocRenderPipelineBuilder vertexShader(Identifier id) {
        delegate.withVertexShader(id);
        return this;
    }

    public HavocRenderPipelineBuilder fragmentShader(Identifier id) {
        delegate.withFragmentShader(id);
        return this;
    }

    public HavocRenderPipelineBuilder depthTest(DepthTestFunction function) {
        delegate.withDepthTestFunction(function);
        return this;
    }

    public HavocRenderPipelineBuilder depthWrite(boolean write) {
        delegate.withDepthWrite(write);
        return this;
    }

    public HavocRenderPipelineBuilder colorTarget(int index,
                                                       GpuFormat format,
                                                       BlendFunction blend,
                                                       int writeMask) {
        delegate.withColorTarget(index, format, blend, writeMask);
        return this;
    }

    public HavocRenderPipelineBuilder colorTarget(int index, GpuFormat format) {
        delegate.withColorTarget(index, format);
        return this;
    }

    public HavocRenderPipelineBuilder unusedColorTarget(int index) {
        delegate.withUnusedColorTarget(index);
        return this;
    }

    public HavocRenderPipelineBuilder blend(BlendFunction blend) {
        delegate.withBlend(blend);
        return this;
    }

    public HavocRenderPipelineBuilder blend(BlendFactor source, BlendFactor destination) {
        delegate.withBlend(source, destination);
        return this;
    }

    public HavocRenderPipelineBuilder blendSeparate(BlendFactor sourceColor,
                                                         BlendFactor destinationColor,
                                                         BlendFactor sourceAlpha,
                                                         BlendFactor destinationAlpha) {
        delegate.withBlendSeparate(sourceColor, destinationColor, sourceAlpha, destinationAlpha);
        return this;
    }

    public HavocRenderPipelineBuilder noBlend() {
        delegate.withoutBlend();
        return this;
    }

    public HavocRenderPipelineBuilder cull(boolean cull) {
        delegate.withCull(cull);
        return this;
    }

    public HavocRenderPipelineBuilder sampler(String name) {
        delegate.withSampler(name);
        return this;
    }

    public HavocRenderPipelineBuilder uniform(String name, UniformType type) {
        delegate.withUniform(name, type);
        return this;
    }

    public HavocRenderPipelineBuilder contract(RenderPipelineContract contract) {
        delegate.withContract(contract);
        return this;
    }

    public HavocRenderPipelineBuilder domain(PipelineDomain domain) {
        delegate.withDomain(domain);
        return this;
    }

    public HavocRenderPipelineBuilder transformPolicy(TransformPolicy policy) {
        delegate.withTransformPolicy(policy);
        return this;
    }

    public HavocRenderPipelineBuilder shapeFamily(String family) {
        delegate.withShapeFamily(family);
        return this;
    }

    public HavocRenderPipelineBuilder lineSmooth() {
        delegate.withLineSmooth();
        return this;
    }

    public HavocRenderPipelineBuilder shapeClipSupport() {
        delegate.withShapeClipSupport();
        return this;
    }

    public HavocRenderPipelineBuilder shapeClipContract(ShapeClipRenderPassContract contract) {
        delegate.withShapeClipContract(contract);
        return this;
    }

    public RenderPipeline build() {
        return delegate.build();
    }

    public RenderPipeline buildAndRegister() {
        return HavocRenderPipelines.registerAddonPipeline(build());
    }
}
