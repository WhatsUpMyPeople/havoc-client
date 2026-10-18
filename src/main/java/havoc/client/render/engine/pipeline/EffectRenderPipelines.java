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
import havoc.client.render.engine.vertex.HavocVertexFormats;
import net.minecraft.resources.Identifier;

public final class EffectRenderPipelines {
    private static final RenderPipeline.Snippet MESH_UNIFORMS = HavocRenderPipelines.meshUniforms();
    private static final RenderPipeline.Snippet FOG_UNIFORMS = HavocRenderPipelines.fogUniforms();


    private EffectRenderPipelines() {
    }

    @PipelineRegister
    public static final RenderPipeline WORLD_REIMAGINED_SKYBOX_SHADER = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS, FOG_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/world_reimagined_skybox_shader"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("reimagined_skybox.frag"))
            .withUniform("SkyboxShader", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT).withCull(false).build();
    /**
     * Fullscreen heat distortion + vignette (pos2).
     */
    @PipelineRegister
    public static final RenderPipeline HEAT_FX = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/heat_fx"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("heat_fx.frag"))
            .withSampler("u_Texture")
            .withUniform("Heat", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();
    /**
     * Fullscreen shader ESP gradient composite (pos2).
     */
    @PipelineRegister
    public static final RenderPipeline SHADER_ESP_GRADIENT = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/shader_esp_gradient"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("shader_esp_gradient.frag"))
            .withSampler("u_Texture")
            .withUniform("ShaderEspGradient", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();
    /**
     * Fullscreen shader ESP separable shadow blur (pos2).
     */
    @PipelineRegister
    public static final RenderPipeline SHADER_ESP_SHADOW = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/shader_esp_shadow"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("shader_esp_shadow.frag"))
            .withSampler("u_Texture")
            .withSampler("u_Mask")
            .withUniform("ShaderEspBlur", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();
    /**
     * Fullscreen shader ESP procedural smoke fill (pos2).
     */
    @PipelineRegister
    public static final RenderPipeline SHADER_ESP_SMOKE = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/shader_esp_smoke"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("shader_esp_smoke.frag"))
            .withSampler("u_Texture")
            .withUniform("ShaderEspSmoke", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();
    /**
     * No depth test; additive; textured-color triangles with procedural sun shading.
     */
    /**
     * Fullscreen nether portal rift (pos2).
     */
    @PipelineRegister
    public static final RenderPipeline PORTAL_RIFT = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/portal_rift"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("portal_rift.frag"))
            .withSampler("u_Texture")
            .withUniform("Heat", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();
    /**
     * Fullscreen sleep overlay (pos2).
     */
    @PipelineRegister
    public static final RenderPipeline SLEEP_OVERLAY = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/sleep_overlay"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("sleep_overlay.frag"))
            .withSampler("u_Texture")
            .withUniform("PostProcess", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();
    /**
     * Fullscreen hand glass (pos2).
     */
    @PipelineRegister
    public static final RenderPipeline HAND_GLASS = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/hand_glass"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("hand_glass.frag"))
            .withSampler("u_Src")
            .withSampler("u_Mask")
            .withUniform("HandGlass", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline HAND_SMOKE = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/hand_smoke"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("hand_smoke.frag"))
            .withSampler("u_Src")
            .withSampler("u_Mask")
            .withUniform("HandSmoke", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline HAND_RIFT = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/hand_rift"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("hand_rift.frag"))
            .withSampler("u_Src")
            .withSampler("u_Mask")
            .withUniform("HandRift", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline HAND_METALLIC = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/hand_metallic"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("hand_metallic.frag"))
            .withSampler("u_Src")
            .withSampler("u_Mask")
            .withSampler("u_Occupancy")
            .withUniform("HandMetallic", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline HAND_MASK_OCCUPANCY = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/hand_mask_occupancy"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("hand_mask_occupancy.frag"))
            .withSampler("u_Mask")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline HAND_MASK_OCCUPANCY_DILATE = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/hand_mask_occupancy_dilate"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("hand_mask_occupancy_dilate.frag"))
            .withSampler("u_Mask")
            .withUniform("HandMetallic", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline HAND_GHOSTING_HISTORY = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/hand_ghosting_history"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("hand_ghosting_history.frag"))
            .withSampler("u_History")
            .withSampler("u_Mask")
            .withUniform("HandGhosting", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build();

    @PipelineRegister
    public static final RenderPipeline HAND_GHOSTING = new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
            .withLocation(Identifier.fromNamespaceAndPath("havoc", "pipeline/hand_ghosting"))
            .withVertexFormat(HavocVertexFormats.POS2, com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withVertexShader(shader("damage_tint.vert"))
            .withFragmentShader(shader("hand_ghosting.frag"))
            .withSampler("u_Src")
            .withSampler("u_History")
            .withUniform("HandGhosting", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build();

    private static Identifier shader(String path) {
        return Identifier.fromNamespaceAndPath("havoc", "shaders/" + path);
    }
}
