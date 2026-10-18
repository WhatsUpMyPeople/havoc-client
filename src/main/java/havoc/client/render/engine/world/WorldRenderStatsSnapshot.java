/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.world;

public record WorldRenderStatsSnapshot(long frameId,
                                       long recordedCommands,
                                       long submittedCommands,
                                       long skippedEmptyCommands,
                                       long submittedVertices,
                                       long submittedIndices,
                                       long fogBindings,
                                       long depthPrePassBindings,
                                       long depthMainBindings,
                                       long depthDisabledBindings) {
}
