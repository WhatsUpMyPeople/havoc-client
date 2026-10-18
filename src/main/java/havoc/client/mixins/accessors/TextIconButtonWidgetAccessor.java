/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.accessors;

import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteIconButton.class)
public interface TextIconButtonWidgetAccessor {
    @Accessor("spriteWidth")
    int havoc$getTextureWidth();

    @Accessor("spriteHeight")
    int havoc$getTextureHeight();

    @Accessor("sprite")
    WidgetSprites havoc$getTexture();
}


