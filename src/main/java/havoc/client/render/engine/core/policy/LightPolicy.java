/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.core.policy;

/**
 * Placeholder for Havoc lighting policy. Kept explicit so fullbright/ambient/shaderpack hooks do not leak into renderers.
 */
public interface LightPolicy {
    LightPolicy VANILLA = new LightPolicy() {
    };
}
