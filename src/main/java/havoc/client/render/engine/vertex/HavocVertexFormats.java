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

package havoc.client.render.engine.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;

public enum HavocVertexFormats {
    ;
    public static final VertexFormat POS2 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .build();

    public static final VertexFormat POS3_TEXTURE_COLOR_PARAMS2 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS3)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .build();

    public static final VertexFormat POS2_COLOR = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .build();

    /** Continuous UI path vertex: screen position, color, and edge/path parameters. */
    public static final VertexFormat POS2_COLOR_PARAMS = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .build();

    /**
     * World-space wide line vertex.
     * Position is the current endpoint, Color is endpoint color, Line packs the opposite endpoint in xyz
     * and signed screen-space line width in w. The vertex shader expands the endpoint in clip space.
     */
    public static final VertexFormat POS3_COLOR_LINE = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS3)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Line", HavocVertexFormatElements.PARAMS)
            .build();

    public static final VertexFormat POS2_TEXTURE_COLOR = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .build();

    public static final VertexFormat POS2_COLOR_RECT_PARAMS = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .build();

    public static final VertexFormat POS2_COLOR_RECT_PARAMS2 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .build();

    public static final VertexFormat POS2_LOCAL_COLOR_RECT_PARAMS = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .build();

    public static final VertexFormat POS2_LOCAL_COLOR_RECT_PARAMS2 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .build();

    /** Shared data-driven UI geometry family: shape header plus two shape-specific payloads. */
    public static final VertexFormat POS2_LOCAL_COLOR_RECT_PARAMS3 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .addAttribute("Params3", HavocVertexFormatElements.PARAMS3)
            .build();

    public static final VertexFormat POS2_TEXTURE_COLOR_RECT_PARAMS = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .build();

    public static final VertexFormat POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .build();

    public static final VertexFormat POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS2 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .build();

    /** Liquid-glass optical payload plus an optional eight-point primitive mask. */
    public static final VertexFormat POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS6 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .addAttribute("Params3", HavocVertexFormatElements.PARAMS3)
            .addAttribute("Params4", HavocVertexFormatElements.PARAMS4)
            .addAttribute("Params5", HavocVertexFormatElements.PARAMS5)
            .addAttribute("Params6", HavocVertexFormatElements.PARAMS6)
            .build();

    /** Liquid-glass payload with one additional material-modifier vector. */
    public static final VertexFormat POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS7 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .addAttribute("Params3", HavocVertexFormatElements.PARAMS3)
            .addAttribute("Params4", HavocVertexFormatElements.PARAMS4)
            .addAttribute("Params5", HavocVertexFormatElements.PARAMS5)
            .addAttribute("Params6", HavocVertexFormatElements.PARAMS6)
            .addAttribute("Params7", HavocVertexFormatElements.PARAMS7)
            .build();

    /** Shared glass geometry, modifiers and two data-driven optical parameter vectors. */
    public static final VertexFormat POS2_TEXTURE_LOCAL_COLOR_RECT_PARAMS9 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .addAttribute("Params3", HavocVertexFormatElements.PARAMS3)
            .addAttribute("Params4", HavocVertexFormatElements.PARAMS4)
            .addAttribute("Params5", HavocVertexFormatElements.PARAMS5)
            .addAttribute("Params6", HavocVertexFormatElements.PARAMS6)
            .addAttribute("Params7", HavocVertexFormatElements.PARAMS7)
            .addAttribute("Params8", HavocVertexFormatElements.PARAMS8)
            .addAttribute("Params9", HavocVertexFormatElements.PARAMS9)
            .addAttribute("Params10", HavocVertexFormatElements.PARAMS10)
            .addAttribute("Params11", HavocVertexFormatElements.PARAMS11)
            .build();

    public static final VertexFormat POS2_TEXTURE_COLOR_RECT_PARAMS2 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .build();

    public static final VertexFormat POS2_LOCAL_COLOR_RECT_PARAMS5 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .addAttribute("Params3", HavocVertexFormatElements.PARAMS3)
            .addAttribute("Params4", HavocVertexFormatElements.PARAMS4)
            .addAttribute("Params5", HavocVertexFormatElements.PARAMS5)
            .build();

    public static final VertexFormat POS2_LOCAL_COLOR_RECT_PARAMS6 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .addAttribute("Params3", HavocVertexFormatElements.PARAMS3)
            .addAttribute("Params4", HavocVertexFormatElements.PARAMS4)
            .addAttribute("Params5", HavocVertexFormatElements.PARAMS5)
            .addAttribute("Params6", HavocVertexFormatElements.PARAMS6)
            .build();

    public static final VertexFormat POS2_LOCAL_COLOR_RECT_PARAMS7 = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS2)
            .addAttribute("Local", HavocVertexFormatElements.LOCAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Rect", HavocVertexFormatElements.RECT)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .addAttribute("Params3", HavocVertexFormatElements.PARAMS3)
            .addAttribute("Params4", HavocVertexFormatElements.PARAMS4)
            .addAttribute("Params5", HavocVertexFormatElements.PARAMS5)
            .addAttribute("Params6", HavocVertexFormatElements.PARAMS6)
            .addAttribute("Params7", HavocVertexFormatElements.PARAMS7)
            .build();

    /**
     * Canonical imported PBR surface vertex. The layout is shaderpack-neutral and intentionally
     * carries both glTF UV sets so adapter code can honor per-texture texCoord selection.
     * 12 + 8 + 8 + 12 + 16 + 4 = 60 bytes.
     */
    public static final VertexFormat ASSET_PBR_STATIC = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS3)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("UV1", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Normal", HavocVertexFormatElements.NORMAL)
            .addAttribute("Tangent", HavocVertexFormatElements.TANGENT)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .build();

    /**
     * Imported PBR vertex with glTF JOINTS_0/WEIGHTS_0. 32-bit joints are retained here instead
     * of silently truncating them to the older rig renderer's 8-bit bone ABI. GPU skinning/adapters
     * can lower this canonical representation later according to actual backend limits.
     * 60 + 16 + 16 = 92 bytes.
     */
    public static final VertexFormat ASSET_PBR_SKINNED = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS3)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("UV1", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Normal", HavocVertexFormatElements.NORMAL)
            .addAttribute("Tangent", HavocVertexFormatElements.TANGENT)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("JointIndices", HavocVertexFormatElements.BONE_INDICES32)
            .addAttribute("JointWeights", HavocVertexFormatElements.BONE_WEIGHTS32)
            .build();

    /**
     * Rigged textured 3D vertex. Attribute order is mirrored by rig_textured.vert.
     * 12 + 8 + 12 + 4 + 4 + 4 + 16 + 4 = 64 bytes.
     */
    public static final VertexFormat RIG_POSITION_TEXTURE_NORMAL_COLOR_BONES_DEFORM = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS3)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Normal", HavocVertexFormatElements.NORMAL)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("BoneIndices", HavocVertexFormatElements.BONE_INDICES)
            .addAttribute("BoneWeights", HavocVertexFormatElements.BONE_WEIGHTS)
            .addAttribute("DeformCoord", HavocVertexFormatElements.DEFORM_COORD)
            .addAttribute("DeformMeta", HavocVertexFormatElements.DEFORM_META)
            .build();

    /** Four-control-point extracted fluid patch consumed only by the native tessellation path. */
    public static final VertexFormat WATER_PATCH = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS3)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .addAttribute("Params3", HavocVertexFormatElements.PARAMS3)
            .addAttribute("MaterialId", HavocVertexFormatElements.MATERIAL_ID)
            .addAttribute("MaterialMapMask", HavocVertexFormatElements.MATERIAL_MAP_MASK)
            .addAttribute("MaterialSurface", HavocVertexFormatElements.MATERIAL_SURFACE)
            .build();

    /**
     * Water-only extracted surface contract. Unlike {@link #WATER_PATCH}, this layout carries the
     * producer-known fluid/material flags required by both tessellated and triangle fallback paths.
     */
    public static final VertexFormat WATER_FORWARD_PATCH = VertexFormat.builder(0)
            .addAttribute("Position", HavocVertexFormatElements.POS3)
            .addAttribute("UV0", HavocVertexFormatElements.TEXTURE)
            .addAttribute("LocalSurface", HavocVertexFormatElements.TEXTURE)
            .addAttribute("Color", HavocVertexFormatElements.COLOR)
            .addAttribute("Params", HavocVertexFormatElements.PARAMS)
            .addAttribute("Params2", HavocVertexFormatElements.PARAMS2)
            .addAttribute("Params3", HavocVertexFormatElements.PARAMS3)
            .addAttribute("Params4", HavocVertexFormatElements.PARAMS4)
            .addAttribute("MaterialId", HavocVertexFormatElements.MATERIAL_ID)
            .addAttribute("FluidTypeId", HavocVertexFormatElements.MATERIAL_ID)
            .addAttribute("MaterialMapMask", HavocVertexFormatElements.MATERIAL_MAP_MASK)
            .addAttribute("MaterialFeatureMask", HavocVertexFormatElements.MATERIAL_SURFACE)
            .addAttribute("SurfaceFlags", HavocVertexFormatElements.MATERIAL_SURFACE)
            .addAttribute("MaterialSurface", HavocVertexFormatElements.MATERIAL_SURFACE)
            .build();

}
