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

package havoc.client.render.engine.pipeline;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import havoc.client.render.engine.shader.HavocShaderSources;
import havoc.client.render.engine.vertex.HavocVertexFormats;
import net.minecraft.resources.Identifier;

public final class UiRenderPipelines {
    private static final RenderPipeline.Snippet MESH_UNIFORMS = HavocRenderPipelines.meshUniforms();
    private static final RenderPipeline.Snippet UI_BATCH_UNIFORMS = HavocRenderPipelines.uiBatchUniforms();
    private static final RenderPipeline.Snippet UI_ANALYTIC_CLIP_UNIFORMS = HavocRenderPipelines.uiAnalyticClipUniforms();
    private static final RenderPipeline.Snippet UI_BLEND_UNIFORMS = HavocRenderPipelines.uiBlendUniforms();
    private static final RenderPipeline.Snippet UI_GLASS_FRAME_UNIFORMS = HavocRenderPipelines.uiGlassFrameUniforms();


    private UiRenderPipelines() {
    }

    @PipelineRegister
    public static final RenderPipeline GUI_TEXTURE_LOOKUP = new ExtendedRenderPipelineBuilder(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/gui_texture_lookup"))
            .withFragmentShader(shader("gui_texture_lookup.frag"))
            .build();
    /**
     * UI triangles (pos2 + color).
     */
    @PipelineRegister
    public static final RenderPipeline UI_COLORED = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_colored"))
            .withVertexFormat(HavocVertexFormats.POS2_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_color.vert"))
            .withFragmentShader(shader("pos_color.frag"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();
    /**
     * UI lines (pos2 + color).
     */
    @PipelineRegister
    public static final RenderPipeline UI_COLORED_LINES_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_colored_lines_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_COLOR, com.mojang.blaze3d.PrimitiveTopology.DEBUG_LINES)
            .withVertexShader(shader("ui_pos_color_fast.vert"))
            .withFragmentShader(shader("pos_color.frag"))
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();
    /**
     * UI textured triangles (pos2 + tex + color).
     */
    @PipelineRegister
    public static final RenderPipeline UI_TEXTURED = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_textured"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_tex_color.vert"))
            .withFragmentShader(shader("pos_tex_color.frag"))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_MAP_TILE_LIGHT = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_map_tile_light"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_tex_color.vert"))
            .withFragmentShader(shader("map_tile_light.frag"))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_MAP_TILE_OPAQUE = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_map_tile_opaque"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_tex_color.vert"))
            .withFragmentShader(shader("map_tile_opaque.frag"))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXTURED_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_textured"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_tex_color.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("pos_tex_color.frag")))
            .withSampler("u_Texture")
            .withContract(RenderPipelineContract.UI_EXTENDED)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();
    /**
     * UI textured triangles for premultiplied-alpha sources such as GuiItemAtlas.
     */
    @PipelineRegister
    public static final RenderPipeline UI_TEXTURED_PREMULTIPLIED_ALPHA = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_textured_premultiplied_alpha"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("pos_tex_color.frag"))
            .withSampler("u_Texture")
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXTURED_PREMULTIPLIED_ALPHA_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_textured_premultiplied_alpha"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("pos_tex_color_premultiplied.frag")))
            .withSampler("u_Texture")
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXTURED_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_textured_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("pos_tex_color.frag"))
            .withSampler("u_Texture")
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXTURED_FAST_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_textured_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("pos_tex_color.frag")))
            .withSampler("u_Texture")
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();
    /**
     * UI MSDF SVG quads (pos2 + tex + color).
     */
    @PipelineRegister
    public static final RenderPipeline UI_SVG_MSDF_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_svg_msdf_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("svg_msdf.frag"))
            .withSampler("u_Texture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_SVG_MSDF_FAST_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_svg_msdf_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("svg_msdf.frag")))
            .withSampler("u_Texture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();
    /**
     * UI textured triangles (pos2 + tex + color), additive blend.
     */
    @PipelineRegister
    public static final RenderPipeline UI_TEXTURED_ADDITIVE_TRANSFORMED = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_textured_additive_transformed"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_transformed.vert"))
            .withFragmentShader(shader("pos_tex_color.frag"))
            .withSampler("u_Texture")
            .withContract(RenderPipelineContract.UI_EXTENDED)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunctions.ALPHA_ADDITIVE)
            .withCull(true)
            .build();
    // ======================
    // UI (2D)
    // ======================
    /**
     * UI text (pos2 + tex + color).
     */
    @PipelineRegister
    public static final RenderPipeline UI_TEXT = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("text.vert"))
            .withFragmentShader(shader("text.frag"))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXT_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_text"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("text.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("text.frag")))
            .withSampler("u_Texture")
            .withContract(RenderPipelineContract.UI_EXTENDED)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();
    /** Stable screen-space UI text. UNSCALED/CUSTOM and transformed callers keep UI_TEXT. */
    @PipelineRegister
    public static final RenderPipeline UI_TEXT_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("text.frag"))
            .withSampler("u_Texture")
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXT_FAST_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_text_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("text.frag")))
            .withSampler("u_Texture")
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();
    /**
     * UI MSDF text (pos2 + tex + color).
     */
    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text_msdf"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("text.vert"))
            .withFragmentShader(shader("text_msdf.frag"))
            .withSampler("u_Texture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_text_msdf"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("text.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("text_msdf.frag")))
            .withSampler("u_Texture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_EXTENDED)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text_msdf_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("text_msdf.frag"))
            .withSampler("u_Texture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    /** MSDF glyph mask rendered as backdrop-refracting liquid glass. */
    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF_GLASS_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS, UI_GLASS_FRAME_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text_msdf_glass_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("text_msdf_glass.frag"))
            .withSampler("u_Texture")
            .withSampler("u_BlurTexture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    /** Reusable MSDF glass compositor. Blend mode is data in UIBlend, not a shader permutation. */
    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF_GLASS_BLEND_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS, UI_BLEND_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text_msdf_glass_blend_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("text_msdf_glass_blend.frag"))
            .withSampler("u_Texture")
            .withSampler("u_SceneTexture")
            .withSampler("u_BlurTexture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF_GLASS_NEGATIVE_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text_msdf_glass_negative_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("text_msdf_glass_negative.frag"))
            .withSampler("u_Texture")
            .withSampler("u_SceneTexture")
            .withSampler("u_BlurTexture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF_GLASS_MONOCHROME_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text_msdf_glass_monochrome_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("text_msdf_glass_monochrome.frag"))
            .withSampler("u_Texture")
            .withSampler("u_SceneTexture")
            .withSampler("u_BlurTexture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF_GLASS_MONO_NEGATIVE_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text_msdf_glass_mono_negative_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("text_msdf_glass_mono_negative.frag"))
            .withSampler("u_Texture")
            .withSampler("u_SceneTexture")
            .withSampler("u_BlurTexture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF_GLASS_DUOTONE_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text_msdf_glass_duotone_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("text_msdf_glass_duotone.frag"))
            .withSampler("u_Texture")
            .withSampler("u_SceneTexture")
            .withSampler("u_BlurTexture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF_GLASS_SOLARIZE_FAST = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_text_msdf_glass_solarize_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(shader("text_msdf_glass_solarize.frag"))
            .withSampler("u_Texture")
            .withSampler("u_SceneTexture")
            .withSampler("u_BlurTexture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXT_MSDF_FAST_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_text_msdf_fast"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_COLOR, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_pos_tex_color_fast.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("text_msdf.frag")))
            .withSampler("u_Texture")
            .withUniform("MsdfText", UniformType.UNIFORM_BUFFER)
            .withContract(RenderPipelineContract.UI_FAST)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    private static Identifier shader(String path) {
        return Identifier.fromNamespaceAndPath("havoc", "shaders/" + path);
    }
}