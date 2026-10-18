/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rhi;

public enum MeshOwnership {
    /**
     * Backed by backend-owned ring buffers. Do not close from draw code.
     */
    BACKEND_FRAME,
    /**
     * Backed by persistent resource manager object. Do not close from draw code.
     */
    PERSISTENT
}
