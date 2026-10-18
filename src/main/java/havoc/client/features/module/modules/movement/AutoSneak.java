/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.movement;

import havoc.client.config.values.EnumValue;
import havoc.client.events.EventHandler;
import havoc.client.events.impl.MovementInputEvent;
import havoc.client.events.impl.PacketEvent;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;

@ModuleInfo(
        id = "autosneak",
        displayName = "AutoSneak",
        aliases = {"Sneak", "SilentSneak"},
        category = ModuleCategory.MOVEMENT,
        subcategory = ModuleSubcategory.BASIC,
        description = "module.autosneak.description")
public final class AutoSneak extends Module {
    private final Minecraft mc = Minecraft.getInstance();
    private final EnumValue<Mode> mode = enumSetting("autoSneakMode", "mode", Mode.INPUT, Mode.values());

    @EventHandler
    private void onMovementInput(MovementInputEvent event) {
        if (isEnabled() && mode.get() == Mode.INPUT) event.setSneak(true);
    }

    @EventHandler(priority = 100)
    private void onPacketSend(PacketEvent.Send event) {
        if (!isEnabled() || mode.get() != Mode.PACKET) return;
        if (!(event.getPacket() instanceof ServerboundPlayerInputPacket packet)) return;

        Input input = packet.input();
        if (input.shift()) return;

        event.cancel();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundPlayerInputPacket(new Input(
                    input.forward(),
                    input.backward(),
                    input.left(),
                    input.right(),
                    input.jump(),
                    true,
                    input.sprint()
            )));
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum Mode implements EnumValue.IdProvider {
        INPUT("input"),
        PACKET("packet");

        private final String id;
    }
}
