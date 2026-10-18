#version 330 core

/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

#moj_import <sodium:globals.glsl>
#moj_import <sodium:fog.glsl>
#moj_import <sodium:chunk_vertex.glsl>

out vec4 v_Color;
out vec2 v_TexCoord;
out float v_ViewDistance;
flat out uint v_HavocSurfaceFlags;

#ifdef HAVOC_DEFERRED_GBUFFER
out vec4 v_HavocBaseColor;
out vec2 v_HavocLightCoord;
out vec3 v_HavocViewPosition;
out float v_HavocVertexAo;
flat out uint v_HavocMaterialParams;
flat out uint v_HavocMaterialId;
flat out uint v_HavocMaterialMeta;
flat out uint v_HavocMaterialSurface;
flat out vec4 v_HavocTangent;
#endif

#ifdef USE_FOG
out vec2 v_FragDistance;
out float fadeFactor;
#endif

uniform isamplerBuffer u_SectionTimeInfo;

#ifdef VULKAN
layout(push_constant) uniform PC {
    vec3 u_RegionOffset;
    int u_CurrentTime;
    uint u_RegionID;
};
#else
uniform vec3 u_RegionOffset;
uniform int u_CurrentTime;
uniform uint u_RegionID;
#endif

uniform sampler2D u_LightTex;

layout(location = 4) in uint a_HavocSurfaceFlags;
#ifdef HAVOC_DEFERRED_GBUFFER
layout(location = 5) in uint a_HavocMaterialData;
layout(location = 6) in uint a_HavocMaterialMeta;
layout(location = 7) in vec4 a_HavocTangent;
layout(location = 8) in uint a_HavocMaterialSurface;
layout(location = 9) in vec4 a_HavocBaseColor;
#endif


#ifdef HAVOC_DIRECTIONAL_SHADOW_DISTORTION
// Non-linear single-map warping concentrates texel density around the camera and decreases it
// continuously toward the finite shadow boundary without over-expanding diagonal axes.
const float HAVOC_SHADOW_DISTORTION = 0.85;

float havoc_shadow_quartic_length(vec2 value) {
    vec2 squared = value * value;
    vec2 fourth = squared * squared;
    return sqrt(sqrt(fourth.x + fourth.y));
}

float havoc_shadow_distortion_factor(vec2 shadowNdc) {
    return havoc_shadow_quartic_length(shadowNdc) * HAVOC_SHADOW_DISTORTION
            + (1.0 - HAVOC_SHADOW_DISTORTION);
}
#endif


uvec3 _get_relative_chunk_coord(uint pos) {
    return uvec3(pos) >> uvec3(5u, 0u, 2u) & uvec3(7u, 3u, 7u);
}

vec3 _get_draw_translation(uint pos) {
    return _get_relative_chunk_coord(pos) * vec3(16.0);
}

void main() {
    _vert_init();

    vec3 translation = u_RegionOffset + _get_draw_translation(_draw_id);
    vec3 position = _vert_position + translation;
    vec4 viewPosition = u_ModelViewMatrix * vec4(position, 1.0);

#ifdef USE_FOG
    v_FragDistance = getFragDistance(position);

    int chunkId = int(_draw_id);
    int chunkFade = texelFetch(u_SectionTimeInfo, int((u_RegionID * 256u) + uint(chunkId))).r;
    float fade = clamp(float(u_CurrentTime - chunkFade) * u_FadePeriodInv, 0.0, 1.0);
    fadeFactor = (chunkFade < 0) ? 1.0 : fade;
#endif

    gl_Position = u_ProjectionMatrix * viewPosition;
#ifdef HAVOC_DIRECTIONAL_SHADOW_DISTORTION
    if (abs(gl_Position.w) > 1.0e-7) {
        vec2 shadowNdc = gl_Position.xy / gl_Position.w;
        float distortionFactor = max(havoc_shadow_distortion_factor(shadowNdc), 1.0e-4);
        gl_Position.xy /= distortionFactor;
    }
#endif

#if defined(HAVOC_SHADOW_PASS) || defined(HAVOC_DEFERRED_GBUFFER)
    // Deferred G-buffer stores material/base tint only. Vanilla lightmap is a lighting result and
    // must not be baked into albedo before Havoc's renderer-owned lighting stage.
    v_Color = _vert_color;
#else
    v_Color = _vert_color * texture(u_LightTex, _vert_tex_light_coord);
#endif
    v_TexCoord = (_vert_tex_diffuse_coord_bias * u_TexCoordShrink) + _vert_tex_diffuse_coord;
    v_ViewDistance = length(viewPosition.xyz);
    v_HavocSurfaceFlags = a_HavocSurfaceFlags;

#ifdef HAVOC_DEFERRED_GBUFFER
    v_HavocBaseColor = a_HavocBaseColor;
    v_HavocLightCoord = _vert_tex_light_coord;
    v_HavocViewPosition = viewPosition.xyz;
    // AO is per-vertex data. Keep it smooth; packing it inside the flat material meta makes each
    // triangle inherit only its provoking vertex and creates the visible diagonal faceting.
    v_HavocVertexAo = float(a_HavocMaterialMeta & 255u) / 255.0;
    v_HavocMaterialParams = _material_params;
    v_HavocMaterialId = a_HavocMaterialData;
    v_HavocMaterialMeta = a_HavocMaterialMeta;
    v_HavocMaterialSurface = a_HavocMaterialSurface;
    v_HavocTangent = vec4(normalize(mat3(u_ModelViewMatrix) * a_HavocTangent.xyz), a_HavocTangent.w);
#endif
}
