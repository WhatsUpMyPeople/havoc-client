/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.accessors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LocalPlayer.class)
public interface LocalPlayerAccessor {

    @Accessor("minecraft")
    Minecraft getClient();

    @Accessor("portalEffectIntensity")
    float havoc$getNauseaIntensity();

    @Accessor("portalEffectIntensity")
    void havoc$setNauseaIntensity(float value);

    @Accessor("oPortalEffectIntensity")
    float havoc$getLastNauseaIntensity();

    @Accessor("oPortalEffectIntensity")
    void havoc$setLastNauseaIntensity(float value);

    @Accessor("positionReminder")
    void havoc$setTicksSinceLastPositionPacketSent(int ticks);

    @Accessor("wasSprinting")
    void havoc$setLastSprinting(boolean sprinting);

    @Invoker("canStartSprinting")
    boolean havoc$canStartSprinting();

    @Invoker("shouldStopRunSprinting")
    boolean havoc$shouldStopSprinting();

    @Invoker("shouldStopSwimSprinting")
    boolean havoc$shouldStopSwimSprinting();
}


