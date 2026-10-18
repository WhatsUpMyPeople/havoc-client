/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.player;

import havoc.client.config.values.BooleanValue;
import havoc.client.events.EventHandler;
import havoc.client.events.impl.PacketEvent;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import havoc.client.mixins.accessors.ServerboundMovePlayerPacketAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

@ModuleInfo(
        id = "antihunger",
        displayName = "AntiHunger",
        aliases = {"NoHunger", "HungerSpoof"},
        category = ModuleCategory.PLAYER,
        subcategory = ModuleSubcategory.EXPLOIT,
        description = "module.antihunger.description")
public final class AntiHunger extends Module {
    private final Minecraft mc = Minecraft.getInstance();

    private final BooleanValue sprintSpoof = bool("antiHungerSprintSpoof", "sprint_spoof", true);
    private final BooleanValue groundSpoof = bool("antiHungerGroundSpoof", "ground_spoof", true);

    @EventHandler(priority = 100)
    public void onPacketSend(PacketEvent.Send event) {
        if (!isEnabled()) return;

        Packet<?> packet = event.getPacket();
        if (sprintSpoof.get() && packet instanceof ServerboundPlayerCommandPacket commandPacket) {
            ServerboundPlayerCommandPacket.Action action = commandPacket.getAction();
            if (action == ServerboundPlayerCommandPacket.Action.START_SPRINTING
                    || action == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
                event.cancel();
                return;
            }
        }

        if (!groundSpoof.get() || !(packet instanceof ServerboundMovePlayerPacket movePacket) || mc.player == null) {
            return;
        }
        if (mc.player.fallDistance >= 2.0f || mc.player.isFallFlying() || mc.player.isPassenger()) {
            return;
        }

        ((ServerboundMovePlayerPacketAccessor) movePacket).havoc$setOnGround(false);
    }
}
