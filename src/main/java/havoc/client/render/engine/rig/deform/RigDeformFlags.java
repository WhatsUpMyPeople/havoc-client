/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rig.deform;

/** Bit flags shared by compile-time vertex bindings and runtime deform state. */
public final class RigDeformFlags {
    public static final int NONE = 0;
    public static final int BEND = 1;
    public static final int TWIST = 1 << 1;
    public static final int RIBBON = 1 << 2;
    public static final int ALL = BEND | TWIST | RIBBON;

    private RigDeformFlags() {
    }
}
