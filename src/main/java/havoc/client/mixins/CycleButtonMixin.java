/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import havoc.client.util.screen.ClientScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.features.gui.hud.nondraggable.impl.BetterButtons;
import havoc.client.mixins.accessors.CyclingButtonWidgetAccessor;

@Mixin(CycleButton.class)
public abstract class CycleButtonMixin<T> {

    @Inject(method = "extractContents(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"), cancellable = true)
    private void havoc$drawIcon(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BetterButtons buttons = BetterButtons.get();
        if (buttons == null || !buttons.useUiButtons()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && ClientScreen.current() instanceof MerchantScreen) return;
        CycleButton<T> self = (CycleButton<T>) (Object) this;
        AbstractWidget widget = self;

        boolean hovered = widget.isMouseOver(mouseX, mouseY);
        float hover = BetterButtons.updateHover(widget, hovered, mouseX, mouseY);

        CyclingButtonWidgetAccessor<T> accessor = (CyclingButtonWidgetAccessor<T>) self;
        Identifier icon = accessor.havoc$getIcon() != null
                ? accessor.havoc$getIcon().apply(self, self.getValue())
                : null;

        boolean showLabel = accessor.havoc$getLabelType() != CycleButton.DisplayState.HIDE;
        BetterButtons.enqueueCyclingButton(self, icon, showLabel, hover, widget.isActive(), widget.isFocused());

        ci.cancel();
    }
}



