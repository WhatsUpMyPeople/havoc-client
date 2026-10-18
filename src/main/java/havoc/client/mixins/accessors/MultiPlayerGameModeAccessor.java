/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.accessors;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {

    @Accessor("destroyBlockPos")
    BlockPos havoc$getCurrentBreakingPos();

    @Accessor("destroyProgress")
    float havoc$getCurrentBreakingProgress();

    @Accessor("destroyProgress")
    void havoc$setCurrentBreakingProgress(float progress);

    @Accessor("destroyDelay")
    int havoc$getBlockBreakingCooldown();

    @Accessor("destroyDelay")
    void havoc$setBlockBreakingCooldown(int cooldown);

    @Accessor("carriedIndex")
    int havoc$getLastSelectedSlot();

    @Accessor("carriedIndex")
    void havoc$setLastSelectedSlot(int slot);

    @Invoker("ensureHasSentCarriedItem")
    void havoc$syncSelectedSlot();
}
