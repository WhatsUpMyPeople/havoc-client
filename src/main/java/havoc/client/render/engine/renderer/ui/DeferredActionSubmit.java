/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui;

import havoc.client.render.engine.core.ViewportContext;
import havoc.client.render.engine.renderer.Renderer2D.Deferred2DLayer;
import havoc.client.render.engine.renderer.ui.clip.UiClipSnapshot;
import havoc.client.render.engine.renderer.ui.clip.UiScissorSnapshot;

public record DeferredActionSubmit(Deferred2DLayer layer, ViewportContext viewport,
                                   UiScissorSnapshot scissorSnapshot, UiClipSnapshot clipSnapshot,
                                   Runnable action) implements Deferred2DSubmit {
    @Override
    public void submit() {
        if (action != null) {
            action.run();
        }
    }
}
