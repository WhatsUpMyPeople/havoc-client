/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.misc;

import havoc.client.config.values.BooleanValue;
import havoc.client.config.values.ModeValue;
import havoc.client.config.values.NumberValue;
import havoc.client.config.values.StringValue;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import havoc.client.features.module.Notifier;
import havoc.client.util.screen.ClientScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Runs a server-side sell command on a timer or when the inventory fills up.
 *
 * <p>Default command is {@code /sell all} which works on most prison/skyblock servers.
 * On servers with a different command, set {@code command} accordingly
 * (e.g. {@code /sellall}, {@code /shop sellall}, {@code /is sellall}).</p>
 *
 * <p>Two triggers:
 * <ul>
 *   <li><b>TIMER</b> — send the command every N seconds.</li>
 *   <li><b>INVENTORY_FILL</b> — send the command when the main inventory
 *       (36 slots, hotbar included) is at least X% full of non-empty stacks.</li>
 * </ul>
 * A minimum-cooldown setting throttles both modes so a lagging server can't be spammed.</p>
 *
 * <p>Pauses while a container GUI is open (so the command doesn't collide with a chest/shop
 * UI) and optionally while the player has recently taken damage, so a mistimed sell never
 * eats a potion a KillAura target would have pushed us to use.</p>
 */
@ModuleInfo(
        id = "autosell",
        displayName = "AutoSell",
        aliases = {"SellAll", "AutoShop"},
        category = ModuleCategory.MISC,
        subcategory = ModuleSubcategory.UTILITY,
        description = "module.autosell.description")
public final class AutoSell extends Module {

    private static final String MODE_TIMER = "timer";
    private static final String MODE_FILL  = "inventory_fill";

    private final Minecraft mc = Minecraft.getInstance();

    private final StringValue command =
            text("autoSellCommand", "command", "/sell all");

    private final ModeValue mode = modeSetting(
            "autoSellMode", "mode",
            MODE_TIMER,
            MODE_TIMER, MODE_FILL);

    private final NumberValue<Integer> intervalSeconds = visibleWhen(
            num("autoSellIntervalSeconds", "interval_seconds", 60, 5, 3600),
            () -> MODE_TIMER.equals(mode.get()));

    private final NumberValue<Integer> fillPercent = visibleWhen(
            num("autoSellFillPercent", "fill_percent", 85, 10, 100),
            () -> MODE_FILL.equals(mode.get()));

    private final NumberValue<Integer> minCooldownSeconds =
            num("autoSellMinCooldownSeconds", "min_cooldown_seconds", 10, 1, 600);

    private final BooleanValue pauseInGui =
            bool("autoSellPauseInGui", "pause_in_gui", true);

    private final BooleanValue pauseInCombat =
            bool("autoSellPauseInCombat", "pause_in_combat", true);

    private final NumberValue<Integer> combatRecoveryTicks = visibleWhen(
            num("autoSellCombatRecoveryTicks", "combat_recovery_ticks", 40, 10, 200),
            pauseInCombat::get);

    private final BooleanValue notify =
            bool("autoSellNotify", "notify", true);

    // ---------- state ----------
    private int tickCounter;
    private int lastSendTick = Integer.MIN_VALUE;
    private int lastHurtTickObserved;

    @Override
    public void onEnable() {
        tickCounter = 0;
        lastSendTick = Integer.MIN_VALUE;
        lastHurtTickObserved = 0;
    }

    @Override
    public void onDisable() {
        tickCounter = 0;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        tickCounter++;

        // Observe damage for the combat-pause check.
        if (player.hurtTime > 0) {
            lastHurtTickObserved = tickCounter;
        }

        if (pauseInGui.get() && ClientScreen.current() != null) return;

        if (pauseInCombat.get()
                && tickCounter - lastHurtTickObserved < combatRecoveryTicks.get()) {
            return;
        }

        // Cooldown check applies to BOTH trigger modes.
        int minGapTicks = minCooldownSeconds.get() * 20;
        if (lastSendTick != Integer.MIN_VALUE
                && tickCounter - lastSendTick < minGapTicks) {
            return;
        }

        boolean shouldSend;
        if (MODE_FILL.equals(mode.get())) {
            shouldSend = inventoryFillPercent(player) >= fillPercent.get();
        } else {
            shouldSend = tickCounter - Math.max(lastSendTick, 0) >= intervalSeconds.get() * 20
                    || lastSendTick == Integer.MIN_VALUE
                       && tickCounter >= intervalSeconds.get() * 20;
        }

        if (!shouldSend) return;

        dispatch(player);
    }

    private void dispatch(LocalPlayer player) {
        String raw = command.get();
        if (raw == null) return;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return;
        // Strip a leading '/' because sendCommand expects the command name only.
        if (trimmed.startsWith("/")) trimmed = trimmed.substring(1).trim();
        if (trimmed.isEmpty()) return;

        try {
            player.connection.sendCommand(trimmed);
        } catch (Throwable t) {
            // Any protocol/encoding issue — surface quietly, don't crash the module.
            if (notify.get()) Notifier.error("AutoSell: couldn't send command (" + t.getClass().getSimpleName() + ")");
            return;
        }

        lastSendTick = tickCounter;

        if (notify.get()) {
            String pct = MODE_FILL.equals(mode.get())
                    ? "  (" + inventoryFillPercent(player) + "% full)"
                    : "";
            Notifier.success("AutoSell: /" + trimmed + pct);
        }
    }

    /** Percent of the main inventory (36 slots incl. hotbar) that holds a non-empty stack. */
    private static int inventoryFillPercent(LocalPlayer player) {
        Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        if (size <= 0) return 0;
        // Only count the 36 "player inventory" slots (hotbar + main), skipping armor/offhand.
        int main = Math.min(size, 36);
        int occupied = 0;
        for (int i = 0; i < main; i++) {
            ItemStack s = inv.getItem(i);
            if (s != null && !s.isEmpty()) occupied++;
        }
        return (occupied * 100) / main;
    }
}
