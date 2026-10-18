/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui.draw;

/**
 * Whole-box geometry. Corner specs remain local RECT details; a squircle is a
 * single superellipse defined by the complete box bounds.
 */
public enum UiBoxForm {
    RECT,
    SQUIRCLE
}
