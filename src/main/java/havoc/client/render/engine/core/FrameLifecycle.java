/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.core;

/**
 * Backend-visible lifecycle state for one Minecraft-presented frame.
 */
public enum FrameLifecycle {
    IDLE,
    RECORDING,
    SUBMITTED,
    PRESENTED
}
