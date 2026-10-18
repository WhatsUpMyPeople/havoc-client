/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui.runtime.reconcile;

import havoc.client.render.engine.renderer.ui.runtime.core.UiNode;
import havoc.client.render.engine.renderer.ui.runtime.core.UiNodeSpec;

public final class UiNodeMatcher {
    public boolean canReuse(UiNode node, UiNodeSpec spec) {
        return node != null && node.sameIdentity(spec);
    }
}
