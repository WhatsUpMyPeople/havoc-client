/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.sodium.terrain;

/** Centralized Havoc terrain attribute names used by Vulkan shader reflection. */
public enum HavocChunkMeshAttributes {
    ;
    public static final String SURFACE_FLAGS = "a_HavocSurfaceFlags";
    public static final String MATERIAL_DATA = "a_HavocMaterialData";
    public static final String MATERIAL_META = "a_HavocMaterialMeta";
    public static final String TANGENT = "a_HavocTangent";
    public static final String MATERIAL_SURFACE = "a_HavocMaterialSurface";
    public static final String BASE_COLOR = "a_HavocBaseColor";
}
