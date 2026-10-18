/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.playeranimator;

/** Render-state extension carrying the solved rig from extraction into body and feature submits. */
public interface PlayerRigRenderState {
    PlayerRigInstance havoc$getPlayerRig();

    void havoc$setPlayerRig(PlayerRigInstance rig);
}
