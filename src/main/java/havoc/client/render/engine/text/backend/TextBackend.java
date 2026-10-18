/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.text.backend;

import havoc.client.render.engine.core.RenderFrameContext;
import havoc.client.render.engine.rhi.HavocRhi;

import java.util.List;

public interface TextBackend {
    String id();

    boolean supports(TextDrawCommand command);

    default boolean supports(TextDrawCommand command, TextPlacementTransform placement) {
        return supports(command);
    }

    default void draw(TextDrawCommand command, RenderFrameContext context, HavocRhi rhi) {
        draw(command, TextPlacementResolver.resolve(command, context), context, rhi);
    }

    default void drawBatch(List<TextDrawCommand> commands, int start, int end, RenderFrameContext context, HavocRhi rhi) {
        for (int i = start; i < end; i++) {
            draw(commands.get(i), context, rhi);
        }
    }

    void draw(TextDrawCommand command, TextPlacementTransform placement, RenderFrameContext context, HavocRhi rhi);
}
