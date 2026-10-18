/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.framegraph;

/** Logical execution domain used when lowering resource hazards to backend barriers. */
public enum FrameGraphExecutionDomain {
    GRAPHICS,
    COMPUTE,
    TRANSFER
}
