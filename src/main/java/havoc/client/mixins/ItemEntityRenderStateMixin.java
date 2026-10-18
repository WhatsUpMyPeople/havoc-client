/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import havoc.client.mixininterface.IItemEntityRenderState;

@Mixin(ItemEntityRenderState.class)
public class ItemEntityRenderStateMixin implements IItemEntityRenderState {
    @Unique
    private boolean havoc$onGround;

    @Override
    public boolean havoc$isOnGround() {
        return havoc$onGround;
    }

    @Override
    public void havoc$setOnGround(boolean onGround) {
        havoc$onGround = onGround;
    }
}
