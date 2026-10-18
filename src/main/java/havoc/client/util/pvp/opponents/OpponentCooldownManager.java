/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.pvp.opponents;

import net.minecraft.world.item.Item;
import havoc.client.config.values.ItemCooldownRulesValue;
import havoc.client.util.pvp.ItemCooldownSnapshot;
import havoc.client.util.pvp.ItemUseCooldowns;

import java.util.Map;
import java.util.UUID;

/**
 * Opponent-facing facade over the owner-based item-use cooldown engine.
 */
public enum OpponentCooldownManager {
    ;

    public static void start(UUID playerId, Item item) {
        recordUse(playerId, item);
    }

    public static void recordUse(UUID playerId, Item item) {
        ItemUseCooldowns.recordUse(playerId, item);
    }

    public static void recordUse(UUID playerId, Item item, ItemCooldownRulesValue.Rule rule) {
        ItemUseCooldowns.recordUse(playerId, item, rule);
    }

    public static boolean isCooling(UUID playerId, Item item) {
        return ItemUseCooldowns.isCooling(playerId, item);
    }

    public static float getProgress(UUID playerId, Item item) {
        return snapshot(playerId, item).cooldownProgress();
    }

    public static ItemCooldownSnapshot snapshot(UUID playerId, Item item) {
        return ItemUseCooldowns.snapshot(playerId, item);
    }

    public static Map<Item, ItemCooldownSnapshot> snapshots(UUID playerId) {
        return ItemUseCooldowns.snapshots(playerId);
    }

    public static void clear(UUID playerId) {
        ItemUseCooldowns.clear(playerId);
    }
}
