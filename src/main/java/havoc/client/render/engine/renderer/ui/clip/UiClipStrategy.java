/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui.clip;

/** Shape-clip implementation selected for the lifetime of a clip scope. */
public enum UiClipStrategy {
    ANALYTIC,
    MSAA_STENCIL
}
