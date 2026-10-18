/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.command;

import havoc.client.render.engine.renderer.ui.clip.UiClipSnapshot;
import havoc.client.render.engine.renderer.ui.clip.UiScissorSnapshot;

/** A normalized command plus the complete immutable clip state at its recording boundary. */
public record UiRecordedCommand(UiCommand command,
                                UiScissorSnapshot scissorSnapshot,
                                UiClipSnapshot clipSnapshot) {
    public UiRecordedCommand {
        if (command == null) throw new IllegalArgumentException("command");
        scissorSnapshot = scissorSnapshot != null ? scissorSnapshot : UiScissorSnapshot.NONE;
        clipSnapshot = clipSnapshot != null ? clipSnapshot : UiClipSnapshot.NONE;
    }
}
