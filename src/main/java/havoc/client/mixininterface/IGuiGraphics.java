/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixininterface;

public interface IGuiGraphics {
    /**
     * Runs a task without vanilla UI scaling applied to {@code net.minecraft.client.gui.GuiGraphicsExtractor} matrix stack.
     */
    void havoc$runUnscaled(Runnable task);

    /**
     * Runs a task with an additional 2D transform applied to {@code net.minecraft.client.gui.GuiGraphicsExtractor} matrix stack.
     * Order: translate(tx, ty) then rotate(angleRad). Angle in radians.
     */
    void havoc$withTransform(float tx, float ty, float angleRad, Runnable task);

    void havoc$drawItemBar(net.minecraft.world.item.ItemStack stack, int x, int y);

    void havoc$drawCooldownProgress(net.minecraft.world.item.ItemStack stack, int x, int y);

    void havoc$addDeferredHudMarker(havoc.client.render.engine.renderer.Renderer2D.Deferred2DLayer layer);
}

