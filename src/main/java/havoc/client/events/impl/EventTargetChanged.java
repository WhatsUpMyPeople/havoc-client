/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.events.impl;

import net.minecraft.world.entity.LivingEntity;
import havoc.client.events.Event;
import havoc.client.util.target.TargetManager;

/**
 * Fired when the centralized target changes.
 */
public class EventTargetChanged extends Event {
    public final LivingEntity previous;
    public final TargetManager.Source previousSource;
    public final LivingEntity current;
    public final TargetManager.Source currentSource;

    public EventTargetChanged(LivingEntity previous,
                              TargetManager.Source previousSource,
                              LivingEntity current,
                              TargetManager.Source currentSource) {
        this.previous = previous;
        this.previousSource = previousSource;
        this.current = current;
        this.currentSource = currentSource;
    }
}
