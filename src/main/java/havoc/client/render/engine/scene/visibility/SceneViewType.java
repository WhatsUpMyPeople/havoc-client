/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.scene.visibility;

/** Logical renderer view. Visibility from one view must never be reused as truth for another. */
public enum SceneViewType {
    PRIMARY,
    SHADOW,
    REFLECTION,
    PROBE,
    DEBUG
}
