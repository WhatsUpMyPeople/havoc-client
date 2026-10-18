/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui.runtime.script;

public interface UiScriptEngine extends AutoCloseable {
    UiScriptRenderResult render(UiScriptModule module, UiScriptRenderContext context);

    @Override
    void close();
}
