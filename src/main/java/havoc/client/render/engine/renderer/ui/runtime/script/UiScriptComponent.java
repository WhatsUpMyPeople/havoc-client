/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui.runtime.script;

import havoc.client.render.engine.renderer.ui.runtime.core.UiNodeSpec;

public interface UiScriptComponent {
    UiNodeSpec render(UiScriptRenderContext context);

    default void dispose() {
    }
}
