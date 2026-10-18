/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.sodium;

import net.minecraft.resources.Identifier;
import havoc.client.util.logging.DebugLog;

/**
 * Sodium shader override/workaround registry.
 * <p>
 * Sodium 26.2 compiles terrain through Mojang RenderPipeline, so Havoc must
 * redirect shader identifiers and let vanilla ShaderManager load/preprocess the
 * .vsh/.fsh resources. Do not read shader source manually here; that bypasses
 * vanilla defines/import handling and breaks Vulkan parity.
 */
public final class SodiumShaderWorkarounds {
    private static final Identifier SODIUM_BLOCK_LAYER_OPAQUE = Identifier.fromNamespaceAndPath("sodium", "blocks/block_layer_opaque");
    private static final Identifier HAVOC_BLOCK_LAYER_OPAQUE = Identifier.fromNamespaceAndPath("havoc", "sodium/block_layer_opaque");
    private long overrideHits;

    public Identifier overrideShaderIdentifier(Identifier id) {
        if (id == null) return null;
        if (SODIUM_BLOCK_LAYER_OPAQUE.equals(id)) {
            overrideHits++;
            DebugLog.infoOnChange("sodium.shader.override.block_layer_opaque", "havoc",
                    "[SODIUM] Havoc Sodium RenderPipeline shader override applied for %s", id);
            return HAVOC_BLOCK_LAYER_OPAQUE;
        }
        return id;
    }

}
