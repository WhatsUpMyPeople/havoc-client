/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.events.impl;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import havoc.client.events.Event;

@Getter
public final class AttackEntityEvent extends Event {
    private final Player player;
    private final Entity target;

    public AttackEntityEvent(Player player, Entity target) {
        this.player = player;
        this.target = target;
    }

}
