/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.accessors;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntityRenderState.class)
public interface LivingEntityRenderStateAccessor {

    @Accessor("bodyRot")
    void havoc$setBodyYaw(float bodyYaw);

    @Accessor("yRot")
    void havoc$setRelativeHeadYaw(float relativeHeadYaw);

    @Accessor("xRot")
    void havoc$setPitch(float pitch);
}
