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
import havoc.client.render.engine.shader.HavocShaderSources;
import havoc.client.render.engine.vertex.HavocVertexFormats;
import net.minecraft.resources.Identifier;

public final class UiBatchRenderPipelines {
    private static final RenderPipeline.Snippet MESH_UNIFORMS = HavocRenderPipelines.meshUniforms();
    private static final RenderPipeline.Snippet UI_BATCH_UNIFORMS = HavocRenderPipelines.uiBatchUniforms();
    private static final RenderPipeline.Snippet UI_ANALYTIC_CLIP_UNIFORMS = HavocRenderPipelines.uiAnalyticClipUniforms();
    private static final RenderPipeline.Snippet UI_BACKDROP_UNIFORMS = HavocRenderPipelines.uiBackdropUniforms();
    private static final RenderPipeline.Snippet UI_BLEND_UNIFORMS = HavocRenderPipelines.uiBlendUniforms();
    private static final RenderPipeline.Snippet UI_GLASS_FRAME_UNIFORMS = HavocRenderPipelines.uiGlassFrameUniforms();
    private static final RenderPipeline.Snippet UI_GLASS_STACK_UNIFORMS = HavocRenderPipelines.uiGlassStackUniforms();


    private UiBatchRenderPipelines() {
    }

    @PipelineRegister
    public static final RenderPipeline UI_ORBIZ_RING_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_orbiz_ring_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS5, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params5_generic.vert"))
            .withFragmentShader(shader("orbiz_ring_batch.frag"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_ROUNDED_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_rounded_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params.vert"))
            .withFragmentShader(shader("rounded_rect_batch.frag"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_ROUNDED_FILL_SMOKE_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_rounded_fill_smoke_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS5, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params5.vert"))
            .withFragmentShader(shader("ui_rounded_fill_smoke_batch.frag"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_MODULE_CATEGORY_SURFACE_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_module_category_surface_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS5, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params5.vert"))
            .withFragmentShader(shader("ui_module_category_surface_batch.frag"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_WIDGET_SURFACE_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_widget_surface_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS6, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params6.vert"))
            .withFragmentShader(shader("ui_widget_surface_batch.frag"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_MAIN_MENU_HONEYCOMB_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_main_menu_honeycomb_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS6, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params6_generic.vert"))
            .withFragmentShader(shader("ui_main_menu_honeycomb_batch.frag"))
            .withSampler("u_Texture")
            .withSampler("u_BlurTexture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_ROUNDED_STROKE_ANGULAR_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_rounded_stroke_angular_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params2.vert"))
            .withFragmentShader(shader("rounded_rect_stroke_angular_batch.frag"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_PATH_BATCH = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_path_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_COLOR_PARAMS, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_path_fast.vert"))
            .withFragmentShader(shader("ui_path.frag"))
            .withContract(RenderPipelineContract.UI_WARPED)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_PATH_BATCH_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_path_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_COLOR_PARAMS, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_path_fast.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("ui_path.frag")))
            .withContract(RenderPipelineContract.UI_WARPED)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_SHAPE_BATCH = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_geometry_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS3, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_geometry_fast.vert"))
            .withFragmentShader(shader("ui_shape_batch.frag"))
            .withContract(RenderPipelineContract.UI_WARPED)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_SHAPE_BATCH_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_geometry_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS3, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_geometry_fast.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("ui_shape_batch.frag")))
            .withContract(RenderPipelineContract.UI_WARPED)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_PRIMITIVE_BATCH = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_primitive_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS5, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_primitive_fast.vert"))
            .withFragmentShader(shader("ui_primitive_batch.frag"))
            .withContract(RenderPipelineContract.UI_WARPED)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_MATERIAL_PRIMITIVE_BATCH = new ExtendedRenderPipelineBuilder(UI_BATCH_UNIFORMS, UI_GLASS_FRAME_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_material_primitive_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS7, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("ui_material_primitive_fast.vert"))
            .withFragmentShader(shader("ui_material_primitive_batch.frag"))
            .withContract(RenderPipelineContract.UI_WARPED)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_ROUNDED_GLOW_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_rounded_glow_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params.vert"))
            .withFragmentShader(shader("rounded_rect_glow_batch.frag"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunctions.ALPHA_ADDITIVE)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_GLOW_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_glow_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params2.vert"))
            .withFragmentShader(shader("ui_glow_batch.frag"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunctions.ALPHA_ADDITIVE)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXTURED_SHAPE_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_textured_shape_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_tex_local_color_rect_params.vert"))
            .withFragmentShader(shader("ui_textured_shape_batch.frag"))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_TEXTURED_SHAPE_BATCH_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_textured_shape_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_tex_local_color_rect_params.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("ui_textured_shape_batch.frag")))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(true)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_BLUR_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_blur_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params.vert"))
            .withFragmentShader(shader("ui_blur_batch.frag"))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_BLUR_BATCH_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_blur_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("ui_blur_batch.frag")))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_BLUR_BATCH_CORNERS = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_blur_batch_corners"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params2.vert"))
            .withFragmentShader(shader("ui_blur_batch_corners.frag"))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_BLUR_BATCH_CORNERS_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_blur_batch_corners"))
            .withVertexFormat(HavocVertexFormats.POS2_LOCAL_COLOR_RECT_PARAMS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_local_color_rect_params2.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("ui_blur_batch_corners.frag")))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_LIQUID_GLASS_BATCH = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS, UI_BLEND_UNIFORMS, UI_GLASS_FRAME_UNIFORMS, UI_GLASS_STACK_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_liquid_glass_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS9, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_tex_local_color_rect_params9.vert"))
            .withFragmentShader(shader("ui_liquid_glass_batch.frag"))
            .withSampler("u_Texture")
            .withSampler("u_BlurTexture")
            .withSampler("u_StackTexture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_LIQUID_GLASS_BATCH_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS, UI_BLEND_UNIFORMS, UI_GLASS_FRAME_UNIFORMS, UI_GLASS_STACK_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_liquid_glass_batch"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS9, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_tex_local_color_rect_params9.vert"))
            .withFragmentShader(HavocShaderSources.analyticClipVariantId(shader("ui_liquid_glass_batch.frag")))
            .withSampler("u_Texture")
            .withSampler("u_BlurTexture")
            .withSampler("u_StackTexture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_LIQUID_GLASS_BATCH_UI_UNDERLAY = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS, UI_BACKDROP_UNIFORMS, UI_BLEND_UNIFORMS, UI_GLASS_FRAME_UNIFORMS, UI_GLASS_STACK_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/ui_liquid_glass_batch_ui_underlay"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS9, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_tex_local_color_rect_params9.vert"))
            .withFragmentShader(HavocShaderSources.uiUnderlayVariantId(shader("ui_liquid_glass_batch.frag")))
            .withSampler("u_Texture")
            .withSampler("u_BlurTexture")
            .withSampler("u_StackTexture")
            .withSampler("u_UiUnderlayTexture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline UI_LIQUID_GLASS_BATCH_UI_UNDERLAY_ANALYTIC_CLIP = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, UI_BATCH_UNIFORMS, UI_ANALYTIC_CLIP_UNIFORMS, UI_BACKDROP_UNIFORMS, UI_BLEND_UNIFORMS, UI_GLASS_FRAME_UNIFORMS, UI_GLASS_STACK_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/analytic_clip/ui_liquid_glass_batch_ui_underlay"))
            .withVertexFormat(HavocVertexFormats.POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS9, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("pos_tex_local_color_rect_params9.vert"))
            .withFragmentShader(HavocShaderSources.uiUnderlayVariantId(HavocShaderSources.analyticClipVariantId(shader("ui_liquid_glass_batch.frag"))))
            .withSampler("u_Texture")
            .withSampler("u_BlurTexture")
            .withSampler("u_StackTexture")
            .withSampler("u_UiUnderlayTexture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    private static Identifier shader(String path) {
        return Identifier.fromNamespaceAndPath("havoc", "shaders/" + path);
    }
}