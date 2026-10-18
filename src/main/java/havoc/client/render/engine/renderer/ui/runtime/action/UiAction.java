/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui.runtime.action;

@FunctionalInterface
public interface UiAction {
    boolean run(UiActionContext context);
}
