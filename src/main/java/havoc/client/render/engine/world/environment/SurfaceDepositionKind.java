/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.world.environment;

/**
 * Explicit airborne material deposited onto world surfaces. This semantic is supplied by the
 * weather/environment producer and is never inferred from color, biome ID or rendered pixels.
 */
public enum SurfaceDepositionKind {
    NONE(0),
    SAND(1),
    DUST(2),
    ASH(3);

    private final int gpuCode;

    SurfaceDepositionKind(int gpuCode) {
        this.gpuCode = gpuCode;
    }

    public int gpuCode() {
        return gpuCode;
    }
}
