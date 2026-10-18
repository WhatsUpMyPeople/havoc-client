/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.text;

import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.core.RenderFrameContext;
import havoc.client.render.engine.rhi.HavocRhi;
import havoc.client.render.engine.text.backend.TextBackendRouter;
import havoc.client.render.engine.text.backend.TextDrawCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Frame-local text command buffer. It is intentionally independent from Renderer2D.
 */
public final class TextCommandBuffer {
    private final List<TextDrawCommand> commands = new ArrayList<>();
    private final TextCommandStats stats;

    public TextCommandBuffer(TextCommandStats stats) {
        this.stats = stats;
    }

    public void record(TextDrawCommand command) {
        if (command == null || command.text() == null || command.text().isEmpty()) return;
        commands.add(command);
        stats.recorded();
        if (command.effect() != null && command.effect().enabled()) stats.effect();
        if (command.clip() != null && command.clip().enabled()) stats.clipped();
    }

    public int size() {
        return commands.size();
    }

    public void flush(TextBackendRouter router) {
        if (commands.isEmpty()) return;
        RenderFrameContext context = HavocRenderSystem.ensureFrameContext();
        HavocRhi rhi = HavocRenderSystem.rhi();
        for (int i = 0; i < commands.size(); ) {
            int consumed = Math.max(1, router.drawAdjacent(commands, i, context, rhi));
            for (int j = 0; j < consumed && i + j < commands.size(); j++) {
                stats.submitted();
            }
            i += consumed;
        }
        commands.clear();
    }

    public void clear() {
        commands.clear();
    }
}
