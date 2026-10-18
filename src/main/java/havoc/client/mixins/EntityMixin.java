/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.events.Events;
import havoc.client.events.impl.PlayerMoveEvent;
import havoc.client.events.impl.PlayerStepSuccessEvent;
import havoc.client.events.impl.PlayerVelocityStrafe;
import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.movement.NoPush;
import havoc.client.features.module.modules.movement.NoStun;
import havoc.client.mixininterface.IEntity;
import havoc.client.mixins.accessors.EntityAccessor;
import havoc.client.mixins.accessors.EntityInvoker;
import havoc.client.render.helpers.TrailPoint;
import havoc.client.util.aiming.RotationManager;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {

    @Unique
    private final List<TrailPoint> trails = new ArrayList<>();
    @Shadow
    public double xOld;
    @Shadow
    public double yOld;
    @Shadow
    public double zOld;
    @Unique
    private MoverType havoc$lastMoveType = MoverType.SELF;
    @Unique
    private Vec3 havoc$stepBeforePos = Vec3.ZERO;
    @Unique
    private Vec3 havoc$stepRequestedMovement = Vec3.ZERO;
    @Unique
    private boolean havoc$stepWasOnGround = false;

    @Unique
    @Override
    public Vec3 get$InstantRenderPos() {
        return new Vec3(xOld, yOld, zOld);
    }

    @Unique
    @Override
    public List<TrailPoint> havoc$getTrails() {
        return trails;
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void havoc$cacheMoveType(MoverType type, Vec3 movement, CallbackInfo ci) {
        havoc$lastMoveType = type;
        Entity self = (Entity) (Object) this;
        if (self instanceof LocalPlayer) {
            havoc$stepBeforePos = self.position();
            havoc$stepRequestedMovement = movement;
            havoc$stepWasOnGround = self.onGround();
        }
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true, index = 2)
    private Vec3 havoc$onMove(Vec3 movement) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LocalPlayer)) return movement;
        if (!Events.BUS.hasListeners(PlayerMoveEvent.class)) return movement;

        PlayerMoveEvent event = new PlayerMoveEvent(havoc$lastMoveType, movement);
        Events.BUS.post(event);
        Vec3 out = event.getMovement();
        return out != null ? out : movement;
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void havoc$onMoveReturn(MoverType type, Vec3 movement, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LocalPlayer)) return;
        if (!Events.BUS.hasListeners(PlayerStepSuccessEvent.class)) return;

        Vec3 before = havoc$stepBeforePos;
        Vec3 after = self.position();
        Vec3 requested = havoc$stepRequestedMovement;
        double yDelta = after.y - before.y;
        double horizontalRequested = Math.hypot(requested.x, requested.z);
        if (yDelta <= 0.5 || horizontalRequested <= 1.0E-5) return;
        if (!havoc$stepWasOnGround) return;
        if (requested.y > 0.05) return;

        Events.BUS.post(new PlayerStepSuccessEvent(after.subtract(before), before, after));
    }

    @Redirect(
            method = "moveRelative",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 havoc$velocityStrafe(Vec3 movementInput, float speed, float yaw) {
        Entity self = (Entity) (Object) this;
        Vec3 vanilla = EntityInvoker.havoc$movementInputToVelocity(movementInput, speed, yaw);
        if (!(self instanceof LocalPlayer)) return vanilla;
        if (!Events.BUS.hasListeners(PlayerVelocityStrafe.class)) return vanilla;

        PlayerVelocityStrafe event = new PlayerVelocityStrafe(movementInput, speed, yaw, vanilla);
        Events.BUS.post(event);
        return event.getVelocity();
    }

    @ModifyVariable(
            method = "calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float havoc$rotationVectorPitch(float pitch) {
        if (!((Object) this instanceof LocalPlayer)) {
            return pitch;
        }

        return RotationManager.INSTANCE.getCurrentRotation() != null
                ? RotationManager.INSTANCE.getCurrentRotation().pitch()
                : pitch;
    }

    @Redirect(
            method = "push(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"
            )
    )
    private void havoc$cancelEntityPush(Entity instance, double x, double y, double z) {
        NoPush noPush = Modules.get(NoPush.class);
        if (noPush == null || !noPush.off("players")) {
            instance.push(x, y, z);
        }
    }

    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
    private void nostun$cancelSlowMovement(BlockState state, Vec3 multiplier, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LocalPlayer)) return;

        NoStun ns = Modules.get(NoStun.class);
        if (ns == null || !ns.isEnabled()) return;

        Vec3 override = ns.getSlowMovementOverride(state, multiplier);
        if (override != null && self instanceof EntityAccessor accessor) {
            self.resetFallDistance();
            accessor.havoc$setMovementMultiplier(override);
            ci.cancel();
            return;
        }

        if (ns.shouldCancelSlowMovement(state)) {
            ci.cancel();
        }
    }

}
