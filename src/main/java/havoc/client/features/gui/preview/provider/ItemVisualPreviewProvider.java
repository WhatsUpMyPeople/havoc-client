/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.gui.preview.provider;

import havoc.client.features.gui.chat.ChatHoverUtil;
import havoc.client.features.gui.hud.script.ScriptedTooltipPanel;
import havoc.client.features.gui.preview.VisualPreviewControlMode;
import havoc.client.features.gui.preview.VisualPreviewInteractionProfile;
import havoc.client.features.gui.preview.VisualPreviewProvider;
import havoc.client.features.gui.preview.VisualPreviewSceneContext;
import havoc.client.features.gui.preview.render.VisualPreviewItemRenderer;
import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.engine.renderer.ui.runtime.render.UiProjectionMode;
import havoc.client.render.engine.text.TextRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ItemVisualPreviewProvider implements VisualPreviewProvider {
    private final ItemStack stack;
    private final ScriptedTooltipPanel tooltipPanel = new ScriptedTooltipPanel("item_preview");

    public ItemVisualPreviewProvider(ItemStack stack) {
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    @Override
    public String id() {
        return "item:" + BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    @Override
    public String title() {
        return stack.isEmpty() ? "Item Preview" : stack.getHoverName().getString();
    }

    @Override
    public VisualPreviewControlMode controlMode() {
        return VisualPreviewControlMode.OBJECT_ROTATE;
    }

    @Override
    public VisualPreviewInteractionProfile interactionProfile() {
        return VisualPreviewInteractionProfile.OBJECT_INSPECTION;
    }

    @Override
    public float initialZoom() {
        return 0.72f;
    }

    @Override
    public void renderSubject(VisualPreviewSceneContext context) {
        VisualPreviewItemRenderer.render(stack, context);
    }

    @Override
    public void renderOverlay(VisualPreviewSceneContext context, Renderer2D renderer) {
        if (stack.isEmpty()) return;
        ChatHoverUtil.HoverTip tip = ChatHoverUtil.buildItemTip(stack, context.minecraft(), false);
        if (tip == null || tip.lines().isEmpty()) return;

        float scale = Math.max(0.85f, Math.min(1.35f, context.height() / 720.0f));
        float maxContentWidth = Math.max(160.0f * scale,
                Math.min(360.0f * scale, context.width() * 0.31f));
        List<ScriptedTooltipPanel.Line> lines = new ArrayList<>(Math.min(18, tip.lines().size()));
        int sourceLimit = Math.min(18, tip.lines().size());
        for (int i = 0; i < sourceLimit; i++) {
            ChatHoverUtil.ColoredLine source = tip.lines().get(i);
            lines.add(new ScriptedTooltipPanel.Line(
                    source.text() != null ? source.text() : "",
                    source.color()
            ));
        }

        TextRenderer fallback = TextRenderer.get();
        ScriptedTooltipPanel.Prepared prepared = tooltipPanel.prepare(
                context.minecraft(),
                fallback,
                lines,
                scale,
                1.0f,
                maxContentWidth,
                0.0f,
                0.0f,
                1.0f,
                ScriptedTooltipPanel.Style.DEFAULT,
                ScriptedTooltipPanel.Context.ITEM
        );
        if (prepared == null) return;

        tooltipPanel.render(
                context.minecraft(),
                prepared,
                renderer,
                fallback,
                null,
                context.tickDelta(),
                18.0f * scale,
                47.0f * scale,
                UiProjectionMode.CURRENT
        );
    }

    public ItemStack stack() {
        return stack.copy();
    }
}
