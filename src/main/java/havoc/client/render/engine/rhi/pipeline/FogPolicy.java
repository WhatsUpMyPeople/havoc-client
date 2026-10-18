/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rhi.pipeline;

public enum FogPolicy {
    NONE,
    VANILLA,
    HAVOC,
    SHADERPACK;

    public boolean requiresWorldFogUniform() {
        return this == VANILLA || this == HAVOC || this == SHADERPACK;
    }
}
