/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui.runtime.input;

import havoc.client.render.engine.renderer.ui.runtime.core.UiNode;

public record UiHitResult(UiNode node, float localX, float localY) {
    public static final UiHitResult MISS = new UiHitResult(null, 0.0f, 0.0f);

    public boolean hit() {
        return node != null;
    }
}
