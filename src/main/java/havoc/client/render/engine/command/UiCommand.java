/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.command;

/**
 * Normalized UI command root.
 * <p>
 * Commands emitted by Renderer2D into the ordered UI compiler.
 */
public interface UiCommand {
    UiCommandKind kind();
}
