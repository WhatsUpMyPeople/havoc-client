#version 330 core

/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

#moj_import <sodium:globals.glsl>
#moj_import <sodium:fog.glsl>
#moj_import <sodium:chunk_material.glsl>

in vec4 v_Color;
in vec2 v_TexCoord;
in vec2 v_FragDistance;
in float fadeFactor;
in float v_ViewDistance;
flat in uint v_HavocSurfaceFlags;

#ifdef HAVOC_DEFERRED_GBUFFER
in vec4 v_HavocBaseColor;
in vec2 v_HavocLightCoord;
in vec3 v_HavocViewPosition;
in float v_HavocVertexAo;
flat in uint v_HavocMaterialParams;
flat in uint v_HavocMaterialId;
flat in uint v_HavocMaterialMeta;
flat in uint v_HavocMaterialSurface;
flat in vec4 v_HavocTangent;
#endif

uniform sampler2D u_BlockTex;

#ifdef HAVOC_DEFERRED_GBUFFER
uniform sampler2D u_HavocAlbedoAtlas;
uniform sampler2D u_HavocNormalHeightAtlas;
uniform sampler2D u_HavocSurfaceAtlas;
uniform sampler2D u_HavocSpecularAtlas;
#endif

layout(location = 0) out vec4 fragColor;

#ifdef HAVOC_DEFERRED_GBUFFER
layout(location = 1) out vec4 havocGbufferSurface;
layout(location = 2) out vec4 havocGbufferGeometry;
layout(location = 3) out vec4 havocGbufferAuxiliary;
layout(location = 4) out vec4 havocGbufferMaterial;
layout(location = 5) out uint havocGbufferMaterialId;
#endif

const uint HAVOC_SURFACE_SOFT_FADE = 1u << 0u;

vec4 sampleNearest(sampler2D source, vec2 uv, vec2 pixelSize, vec2 du, vec2 dv, vec2 texelScreenSize) {
    vec2 uvTexelCoords = uv / pixelSize;
    vec2 texelCenter = round(uvTexelCoords) - 0.5f;
    vec2 texelOffset = uvTexelCoords - texelCenter;

    texelOffset = (texelOffset - 0.5f) * pixelSize / texelScreenSize + 0.5f;
    texelOffset = clamp(texelOffset, 0.0f, 1.0f);

    uv = (texelCenter + texelOffset) * pixelSize;
    return textureGrad(source, uv, du, dv);
}

vec4 sampleNearest(sampler2D source, vec2 uv, vec2 pixelSize) {
    vec2 du = dFdx(uv);
    vec2 dv = dFdy(uv);
    vec2 texelScreenSize = sqrt(du * du + dv * dv);
    return sampleNearest(source, uv, pixelSize, du, dv, texelScreenSize);
}

vec4 sampleRGSS(sampler2D source, vec2 uv, vec2 pixelSize) {
    vec2 du = dFdx(uv);
    vec2 dv = dFdy(uv);

    vec2 texelScreenSize = sqrt(du * du + dv * dv);
    float maxTexelSize = max(texelScreenSize.x, texelScreenSize.y);
    float minPixelSize = min(pixelSize.x, pixelSize.y);
    float transitionStart = minPixelSize * 1.0;
    float transitionEnd = minPixelSize * 2.0;
    float blendFactor = smoothstep(transitionStart, transitionEnd, maxTexelSize);

    float duLength = length(du);
    float dvLength = length(dv);
    float minDerivative = min(duLength, dvLength);
    float maxDerivative = max(duLength, dvLength);
    float effectiveDerivative = sqrt(minDerivative * maxDerivative);
    float mipLevelExact = max(0.0, log2(effectiveDerivative / minPixelSize));

    const vec2 offsets[4] = vec2[](
        vec2(0.125, 0.375),
        vec2(-0.125, -0.375),
        vec2(0.375, -0.125),
        vec2(-0.375, 0.125)
    );

    vec4 rgssColor = vec4(0.0);
    for (int i = 0; i < 4; ++i) {
        vec2 sampleUV = uv + offsets[i] * pixelSize;
        rgssColor += textureLod(source, sampleUV, mipLevelExact);
    }
    rgssColor *= 0.25;

    vec4 nearestColor = sampleNearest(source, uv, pixelSize, du, dv, texelScreenSize);
    return mix(nearestColor, rgssColor, blendFactor);
}

#ifdef HAVOC_DEFERRED_GBUFFER
vec2 havoc_encode_octahedral(vec3 normal) {
    normal /= abs(normal.x) + abs(normal.y) + abs(normal.z);
    vec2 encoded = normal.xy;
    if (normal.z < 0.0) {
        encoded = (1.0 - abs(encoded.yx)) * vec2(encoded.x >= 0.0 ? 1.0 : -1.0, encoded.y >= 0.0 ? 1.0 : -1.0);
    }
    return encoded * 0.5 + 0.5;
}

float havoc_encode_distance(float distanceValue) {
    return clamp((log2(1.0 + max(distanceValue, 0.0)) + 1.0) / 17.0, 1.0 / 255.0, 1.0);
}

float havoc_unpack_unorm8(uint packedValue, uint shift) {
    return float((packedValue >> shift) & 255u) / 255.0;
}

bool havoc_has_map(uint mask, uint bit) {
    return (mask & (1u << bit)) != 0u;
}
#endif

void main() {
#ifdef HAVOC_SHADOW_PASS
#ifdef ALPHA_CUTOUT
    vec4 shadowTexel = texture(u_BlockTex, v_TexCoord) * v_Color;
    if (shadowTexel.a < ALPHA_CUTOUT) {
        discard;
    }
#endif
    fragColor = vec4(0.0);
    return;
#endif

    vec4 texel = u_UseRGSS ? sampleRGSS(u_BlockTex, v_TexCoord, u_TexelSize) : sampleNearest(u_BlockTex, v_TexCoord, u_TexelSize);

#ifdef HAVOC_DEFERRED_GBUFFER
    uint mapMask = (v_HavocMaterialMeta >> 8u) & 255u;
    if (havoc_has_map(mapMask, 7u)) {
        vec4 overrideAlbedo = texture(u_HavocAlbedoAtlas, v_TexCoord);
        texel.rgb = overrideAlbedo.rgb;
        texel.a *= overrideAlbedo.a;
    }
#endif

    vec4 color = texel * v_Color;

    if ((v_HavocSurfaceFlags & HAVOC_SURFACE_SOFT_FADE) != 0u) {
        float obstructionAlpha = smoothstep(0.18, 0.85, v_ViewDistance);
        color.a *= obstructionAlpha;
    }

#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif

    fragColor = _linearFog(color, v_FragDistance, u_FogColor, u_EnvironmentFog, u_RenderFog, fadeFactor);

#ifdef HAVOC_DEFERRED_GBUFFER
    vec3 geometricNormal = normalize(cross(dFdx(v_HavocViewPosition), dFdy(v_HavocViewPosition)));
    if (!gl_FrontFacing) {
        geometricNormal = -geometricNormal;
    }

    vec3 viewNormal = geometricNormal;
    if (havoc_has_map(mapMask, 0u)) {
        vec3 tangentNormal = texture(u_HavocNormalHeightAtlas, v_TexCoord).rgb * 2.0 - 1.0;
        tangentNormal = normalize(tangentNormal);
        vec3 tangent = normalize(v_HavocTangent.xyz - geometricNormal * dot(geometricNormal, v_HavocTangent.xyz));
        vec3 bitangent = normalize(cross(geometricNormal, tangent)) * v_HavocTangent.w;
        viewNormal = normalize(mat3(tangent, bitangent, geometricNormal) * tangentNormal);
    }

    float vertexAo = clamp(v_HavocVertexAo, 0.0, 1.0);
    float materialAo = 1.0;
    float roughness = havoc_unpack_unorm8(v_HavocMaterialSurface, 0u);
    float metallic = havoc_unpack_unorm8(v_HavocMaterialSurface, 8u);
    float dielectricF0 = havoc_unpack_unorm8(v_HavocMaterialSurface, 16u);
    float emission = havoc_unpack_unorm8(v_HavocMaterialSurface, 24u);

    if ((mapMask & 0x3Fu) != 0u) {
        vec4 surfaceSample = texture(u_HavocSurfaceAtlas, v_TexCoord);
        vec4 specularSample = texture(u_HavocSpecularAtlas, v_TexCoord);
        if (havoc_has_map(mapMask, 1u)) materialAo = surfaceSample.r;
        if (havoc_has_map(mapMask, 2u)) roughness = surfaceSample.g;
        if (havoc_has_map(mapMask, 3u)) metallic = surfaceSample.b;
        if (havoc_has_map(mapMask, 4u)) dielectricF0 = specularSample.r;
        if (havoc_has_map(mapMask, 5u)) emission = surfaceSample.a;
    }

    vec3 albedo = texel.rgb * v_HavocBaseColor.rgb;
    float ao = max(1.0 / 255.0, clamp(vertexAo * materialAo, 0.0, 1.0));
    float material = float(v_HavocMaterialParams & 255u) / 255.0;
    havocGbufferSurface = vec4(albedo, ao);
    havocGbufferGeometry = vec4(
        havoc_encode_octahedral(viewNormal),
        clamp(v_HavocLightCoord, 0.0, 1.0)
    );
    havocGbufferAuxiliary = vec4(
        havoc_encode_distance(v_FragDistance.y),
        havoc_encode_distance(v_FragDistance.x),
        clamp(fadeFactor, 0.0, 1.0),
        material
    );
    havocGbufferMaterial = vec4(
        clamp(roughness, 0.0, 1.0),
        clamp(metallic, 0.0, 1.0),
        clamp(dielectricF0, 0.0, 1.0),
        max(emission, 0.0)
    );
    havocGbufferMaterialId = v_HavocMaterialId;
#endif
}
