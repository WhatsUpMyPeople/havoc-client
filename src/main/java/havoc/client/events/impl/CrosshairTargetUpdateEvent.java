/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.events.impl;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import havoc.client.events.Event;

@Getter
public final class CrosshairTargetUpdateEvent extends Event {
    private final float tickDelta;
    @Setter
    private HitResult hitResult;
    @Setter
    private Entity targetedEntity;

    public CrosshairTargetUpdateEvent(float tickDelta, HitResult hitResult, Entity targetedEntity) {
        this.tickDelta = tickDelta;
        this.hitResult = hitResult;
        this.targetedEntity = targetedEntity;
    }

}
