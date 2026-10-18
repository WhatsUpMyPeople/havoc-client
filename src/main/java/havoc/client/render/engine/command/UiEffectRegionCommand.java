/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.command;

import havoc.client.render.engine.renderer.ui.draw.UiEffectSpec;

public record UiEffectRegionCommand(UiEffectSpec effect) implements UiCommand {
    @Override
    public UiCommandKind kind() {
        return UiCommandKind.EFFECT_REGION;
    }
}
