/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.rhi.shader;

/**
 * Raised when a Blaze3D resource from one backend generation is submitted to another backend.
 * This is usually transient around device/backend activation and must not permanently disable
 * an otherwise valid path.
 */
public final class RhiResourceOwnershipException extends IllegalArgumentException {
    public RhiResourceOwnershipException(String message) {
        super(message);
    }
}
