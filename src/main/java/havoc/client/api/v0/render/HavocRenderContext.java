/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.api.v0.render;

import com.mojang.blaze3d.vertex.PoseStack;
import havoc.client.features.module.HudPhase;
import havoc.client.features.module.WorldPhase;
import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.engine.renderer.Renderer3D;
import havoc.client.render.engine.text.TextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public record HavocRenderContext(
        String addonId,
        String callbackId,
        HavocRenderStage stage,
        HudPhase hudPhase,
        WorldPhase worldPhase,
        Renderer2D renderer2D,
        Renderer3D renderer3D,
        Renderer3D depthRenderer3D,
        TextRenderer textRenderer,
        GuiGraphicsExtractor guiContext,
        PoseStack poseStack,
        float tickDelta
) {
    public Minecraft client() {
        return Minecraft.getInstance();
    }
}
