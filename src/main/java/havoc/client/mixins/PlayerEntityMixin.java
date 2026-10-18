/*
 * This file is part of the Havoc Client distribution.
 * Havoc modifications copyright (c) 2026 pivosos2007.
 *
 * Portions of this file are based on LiquidBounce
 * (https://github.com/CCBlueX/LiquidBounce).
 * Copyright (c) 2015-2026 CCBlueX.
 *
 * LiquidBounce portions are licensed under GPLv3-or-later.
 * Havoc modifications are licensed under GPLv3.
 * See THIRD_PARTY_NOTICES.md for details.
 */

package havoc.client.mixins;

/*
 * Parts of attack rotation and keep-sprint related hooks are adapted from
 * LiquidBounce (https://github.com/CCBlueX/LiquidBounce).
 * Original copyright (c) CCBlueX.
 */

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import havoc.client.events.Events;
import havoc.client.events.impl.PlayerSafeWalkEvent;
import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.player.NoDelay;
import havoc.client.features.module.modules.combat.TPSSync;
import havoc.client.features.module.modules.movement.NoStun;
import havoc.client.features.module.modules.movement.Timer;
import havoc.client.mixininterface.IPlayerAttackCooldown;
import havoc.client.util.aiming.RotationManager;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements IPlayerAttackCooldown {

    @Shadow
    @Final
    private ItemCooldowns cooldowns;

    @Inject(method = "isStayingOnGroundSurface", at = @At("RETURN"), cancellable = true)
    private void havoc$safeWalk(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if ((Object) this != Minecraft.getInstance().player) return;
        if (!Events.BUS.hasListeners(PlayerSafeWalkEvent.class)) return;

        PlayerSafeWalkEvent event = new PlayerSafeWalkEvent();
        Events.BUS.post(event);
        if (event.isSafeWalk()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isMobilityRestricted", at = @At("HEAD"), cancellable = true)
    private void havoc$noStunBlindnessSprint(CallbackInfoReturnable<Boolean> cir) {
        NoStun noStun = Modules.get(NoStun.class);
        if (noStun != null && noStun.shouldIgnoreBlindness((Player) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
    private float havoc$noStunIgnoreSlowness(float original) {
        NoStun noStun = Modules.get(NoStun.class);
        if (noStun == null) return original;
        return (float) noStun.getMovementSpeedAttributeWithoutSlowness((Player) (Object) this, original);
    }

    @ModifyExpressionValue(
            method = "causeExtraKnockback",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"
            )
    )
    private float havoc$fixKnockbackYaw(float original) {
        if ((Object) this != Minecraft.getInstance().player) {
            return original;
        }
        return RotationManager.INSTANCE.getMovementRotation().yaw();
    }

    @ModifyExpressionValue(
            method = "doSweepAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"
            )
    )
    private float havoc$fixSweepYaw(float original) {
        if ((Object) this != Minecraft.getInstance().player) {
            return original;
        }
        return RotationManager.INSTANCE.getMovementRotation().yaw();
    }

    @ModifyExpressionValue(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getLookAngle()Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 havoc$fixTravelRotationVector(Vec3 original) {
        if ((Object) this != Minecraft.getInstance().player) {
            return original;
        }
        return RotationManager.INSTANCE.getMovementRotation().directionVector();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void handleShieldCooldown(CallbackInfo ci) {
        NoDelay noDelay = Modules.get(NoDelay.class);
        if (noDelay == null || !noDelay.shouldDisableShieldCooldown()) return;

        cooldowns.removeCooldown(BuiltInRegistries.ITEM.getKey(Items.SHIELD));
    }

    /**
     * Синхронизация attack cooldown с серверным TPS.
     * НЕ трогает ItemCooldownManager.
     * НЕ влияет на shield.
     */
    @ModifyReturnValue(
            method = "getCurrentItemAttackStrengthDelay",
            at = @At("RETURN")
    )
    private float havoc$tpsSyncAttackCooldown(float original) {
        float result = original;

        TPSSync tpsSync = Modules.get(TPSSync.class);
        if (tpsSync != null && tpsSync.isEnabled()) {
            float serverDelta = tpsSync.getServerTickDelta();
            if (serverDelta > 0.0f && serverDelta < 1.0f) {
                // замедляем заполнение cooldown’а под TPS
                result = result / serverDelta;
            }
        }

        float timerMult = Timer.getTickTimer();
        if (timerMult > 0.0001f && Math.abs(timerMult - 1.0f) > 0.0001f) {
            result = result * timerMult;
        }

        return result;
    }

    @Override
    public float havoc$getAttackCooldownProgress(float tickDelta) {
        Player self = (Player) (Object) this;
        return self.getAttackStrengthScale(tickDelta);
    }

    @Override
    public boolean havoc$isAttackCharged(float tickDelta) {
        return havoc$getAttackCooldownProgress(tickDelta) >= 1.0f;
    }
}
