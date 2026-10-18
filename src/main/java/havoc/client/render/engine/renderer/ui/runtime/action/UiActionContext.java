/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui.runtime.action;

import havoc.client.render.engine.renderer.ui.runtime.core.UiNode;

public record UiActionContext(UiNode node, UiActionRef ref, Object event) {
}
