/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.api.v0.clickgui;

import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.engine.text.TextRenderer;

public record HavocClickGuiRenderContext(
        Renderer2D renderer,
        TextRenderer regularFont,
        TextRenderer mediumFont
) {
}
