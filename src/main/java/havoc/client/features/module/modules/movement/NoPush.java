/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.projectile.FishingHook;
import havoc.client.config.values.BooleanMapValue;
import havoc.client.events.EventHandler;
import havoc.client.events.impl.EventPushOutOfBlocks;
import havoc.client.events.impl.PacketEvent;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;

import java.util.LinkedHashMap;

@ModuleInfo(
        id = "nopush",
        displayName = "NoPush",
        category = ModuleCategory.MOVEMENT, subcategory = ModuleSubcategory.BASIC,
        description = "module.nopush.description")
public class NoPush extends Module {

    private static final String SETTING_TOGGLES = "toggles";
    private final BooleanMapValue toggles = group(
            "nopush_toggles",
            SETTING_TOGGLES,
            new LinkedHashMap<>() {{
                put("players", true); // толкание другими энтити
                put("blocks", true);
                put("liquids", true);
                put("fishing_hook", true);  // выталкивание из блоков
                put("pearl_phase", true); // спуф позы после жемчуга
            }}
    );

    public boolean off(String key) {
        return isEnabled() && toggles.get(key);
    }

    public boolean blocksEnabled() {
        return off("blocks");
    }

    public boolean playersEnabled() {
        return off("players");
    }

    public boolean liquidsEnabled() {
        return off("liquids");
    }

    public boolean fishingHookEnabled() {
        return off("fishing_hook");
    }

    @EventHandler
    public void onPushOutOfBlocks(EventPushOutOfBlocks e) {
        if (!blocksEnabled()) return;
        e.cancel();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (!fishingHookEnabled()) return;
        if (!(e.getPacket() instanceof ClientboundEntityEventPacket pac)) return;
        if (pac.getEventId() != 31) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) return;
        if (pac.getEntity(mc.level) instanceof FishingHook hook && hook.getHookedIn() == mc.player) {
            e.cancel();
        }
    }
}
