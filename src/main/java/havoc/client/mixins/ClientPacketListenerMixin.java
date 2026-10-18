/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.config.values.ItemCooldownRulesValue;
import havoc.client.events.Events;
import havoc.client.events.impl.PvpChatEvent;
import havoc.client.events.impl.PvpTabEvent;
import havoc.client.features.module.Modules;
import havoc.client.features.module.modules.combat.PvpCooldowns;
import havoc.client.features.module.modules.misc.NoSound;
import havoc.client.features.module.modules.player.NoRotate;
import havoc.client.features.module.modules.movement.Flight;
import havoc.client.features.module.modules.visuals.SoundESP;
import havoc.client.features.module.modules.visuals.TotemFX;
import havoc.client.features.module.modules.visuals.WorldTweaks;
import havoc.client.features.relations.StaffTracker;
import havoc.client.util.logging.DebugLog;
import havoc.client.util.logging.ServerDumpUtil;
import havoc.client.util.player.NetworkStatsUtil;
import havoc.client.util.player.effect.StatusEffectHeuristics;
import havoc.client.util.pvp.client.CooldownsState;
import havoc.client.util.pvp.opponents.OpponentCooldownManager;
import havoc.client.util.pvp.opponents.TotemPopCounter;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Unique
    private boolean havoc$restoreServerRotation;
    @Unique
    private float havoc$restoreYRot;
    @Unique
    private float havoc$restoreXRot;
    @Unique
    private float havoc$restoreYRotO;
    @Unique
    private float havoc$restoreXRotO;

    @Inject(method = {"handleMovePlayer", "handleRotatePlayer"}, at = @At("HEAD"))
    private void havoc$captureRotationBeforeServerCorrection(CallbackInfo ci) {
        NoRotate noRotate = Modules.get(NoRotate.class);
        Player player = Minecraft.getInstance().player;
        if (noRotate == null || !noRotate.isEnabled() || player == null) {
            havoc$restoreServerRotation = false;
            return;
        }
        havoc$restoreServerRotation = true;
        havoc$restoreYRot = player.getYRot();
        havoc$restoreXRot = player.getXRot();
        havoc$restoreYRotO = player.yRotO;
        havoc$restoreXRotO = player.xRotO;
    }

    @Inject(method = {"handleMovePlayer", "handleRotatePlayer"}, at = @At("TAIL"))
    private void havoc$restoreRotationAfterServerCorrection(CallbackInfo ci) {
        if (!havoc$restoreServerRotation) return;
        havoc$restoreServerRotation = false;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        player.setYRot(havoc$restoreYRot);
        player.setXRot(havoc$restoreXRot);
        player.yRotO = havoc$restoreYRotO;
        player.xRotO = havoc$restoreXRotO;
    }

    // "TPS: 20.0" или "TPS: *20.0"
    @Unique
    private static final Pattern TPS_PATTERN =
            Pattern.compile("TPS:\\s*\\*?(\\d+(?:\\.\\d+)?)");
    // "Пинг: 57"
    @Unique
    private static final Pattern PING_PATTERN =
            Pattern.compile("Пинг:\\s*(\\d+)");

    @Inject(method = "handlePlayerAbilities", at = @At("HEAD"), cancellable = true)
    private void havoc$flightAbilities(ClientboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        Flight flight = Modules.get(Flight.class);
        if (flight == null || !flight.isEnabled()) return;
        if (flight.onReceiveAbilities(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleTabListCustomisation", at = @At("TAIL"))
    private void havoc$captureTab(ClientboundTabListPacket packet, CallbackInfo ci) {
        String header = packet.header() == null ? "" : packet.header().getString();
        String footer = packet.footer() == null ? "" : packet.footer().getString();
        ServerDumpUtil.dumpTabText("HEADER", packet.header());
        ServerDumpUtil.dumpTabText("FOOTER", packet.footer());
        if (Events.BUS.hasListeners(PvpTabEvent.class)) {
            Events.BUS.post(new PvpTabEvent(header, footer));
        }
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void onChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        String msg = packet.content().getString();

        // PvP state listeners via events; legacy direct setters disabled.
        if (msg != null && !msg.isBlank() && Events.BUS.hasListeners(PvpChatEvent.class)) {
            Events.BUS.post(new PvpChatEvent(msg, PvpChatEvent.Source.GAME_MESSAGE));
        }
        /*
        if (msg.contains("Вы вошли в режим PVP")) {
            CooldownsState.MANAGER.enterPvp();
        } else if (msg.contains("PVP окончено")) {
            CooldownsState.MANAGER.exitPvp();
        }
        */
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    private void havoc$onChorus(ClientboundSoundPacket packet, CallbackInfo ci) {
        // Opponent cooldown tracking disabled (legacy system).
        /*
        if (!CooldownsState.shouldTrackOpponents()) return;
        if (packet.getSound().value() != SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Vec3d pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());

        if (!SoundEntityResolver.isUnambiguousOpponent(pos, 1.6)) return;

        PlayerEntity p = SoundEntityResolver.findClosestOpponent(pos, 1.6);
        if (p == null) return;

        OpponentCooldownManager.start(p.getUuid(), Items.CHORUS_FRUIT);
        */
    }

    @Inject(method = "handlePlayerChat", at = @At("HEAD"))
    private void havoc$pvpChatMessage(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
        String msg = packet == null ? null : packet.body().content();
        if (msg == null || msg.isBlank()) return;
        if (Events.BUS.hasListeners(PvpChatEvent.class)) {
            Events.BUS.post(new PvpChatEvent(msg, PvpChatEvent.Source.CHAT_MESSAGE));
        }
    }

    @Inject(method = "handleRemoveEntities", at = @At("TAIL"))
    private void havoc$onEntitiesDestroy(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {

        Minecraft mc = Minecraft.getInstance();
        ClientLevel world = mc.level;
        if (world == null) return;

        for (int id : packet.getEntityIds()) {

            Entity e = world.getEntity(id);
            if (!(e instanceof Player player)) continue;

            StaffTracker.onEntityDisappear(player.getUUID());
        }
    }

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    private void havoc$onEntitySpawn(ClientboundAddEntityPacket packet, CallbackInfo ci) {

        Minecraft mc = Minecraft.getInstance();
        ClientLevel world = mc.level;
        if (world == null) return;

        if (packet.getType() != net.minecraft.world.entity.EntityTypes.PLAYER) return;

        Entity entity = world.getEntity(packet.getId());
        if (!(entity instanceof Player player)) return;

        StaffTracker.onEntityAppear(player.getUUID(), player.getGameProfile().name());
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("TAIL"))
    private void havoc$onPlayerList(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {

        var actions = packet.actions();
        boolean listedAffects = actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED)
                || actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER);

        boolean gmAffects = actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE)
                || actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER);

        for (var entry : packet.entries()) {

            var uuid = entry.profileId();
            String name = entry.profile() != null ? entry.profile().name() : null;

            Boolean listed = listedAffects ? entry.listed() : null;
            var gm = gmAffects ? entry.gameMode() : null;

            // ВАЖНО: берём ПОЛНЫЙ Text из TAB
            var displayName = entry.displayName();
            if (displayName == null && name != null) {
                displayName = net.minecraft.network.chat.Component.literal(name);
            }

            StaffTracker.onTabUpdate(uuid, name, displayName, listed, gm);
        }
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("TAIL"))
    private void havoc$onPlayerRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {

        for (UUID id : packet.profileIds()) {

            // убрали из TAB
            StaffTracker.onTabUpdate(id, null, null, false, null);
            TotemPopCounter.onPlayerLogout(id);
            // ВАЖНО — фикс VANISH после кика
            StaffTracker.onEntityDisappear(id);
        }
    }

    @Inject(method = "handleSetTime", at = @At("HEAD"))
    private void havoc$captureServerTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        WorldTweaks.setServerTimeOfDay(packet.gameTime());
    }

    @Inject(method = "handleTabListCustomisation", at = @At("TAIL"))
    private void havoc$onTab(ClientboundTabListPacket packet, CallbackInfo ci) {
        parse(packet.header());
    }

    @Unique
    private void parse(Component text) {
        if (text == null) return;

        String raw = text.getString();
        if (raw.isEmpty()) return;

        Matcher tps = TPS_PATTERN.matcher(raw);
        if (tps.find()) {
            try {
                NetworkStatsUtil.updateTabTps(Float.parseFloat(tps.group(1)));
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher ping = PING_PATTERN.matcher(raw);
        if (ping.find()) {
            try {
                NetworkStatsUtil.updateTabPing(Integer.parseInt(ping.group(1)));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Inject(method = "handleContainerSetSlot", at = @At("HEAD"))
    private void havoc$debugSlotUpdate(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        // PvP cooldown confirmation disabled (legacy system).
        /*
        int syncId = packet.getSyncId();
        int slot = packet.getSlot();
        ItemStack stack = packet.getStack();

        var pending = CooldownsState.PENDING.consumeIfConfirmed(slot, stack.getCount());
        if (pending != null) {
            CooldownsState.MANAGER.commitConfirmedUse(pending.item(), pending.startedAtMs());
        }
        */
    }

    @Inject(method = "handleContainerContent", at = @At("HEAD"))
    private void havoc$debugFullInventory(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        // PvP cooldown confirmation disabled (legacy system).
        /*
        int syncId = packet.syncId();
        List<ItemStack> contents = packet.contents();

        for (int i = 0; i < contents.size(); i++) {
            ItemStack stack = contents.get(i);
            if (stack.isEmpty()) continue;

            var pending = CooldownsState.PENDING.consumeIfConfirmed(i, stack.getCount());
            if (pending != null) {
                CooldownsState.MANAGER.commitConfirmedUse(pending.item(), pending.startedAtMs());
            }
        }
        */
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"), cancellable = true)
    private void havoc$filterSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        NoSound module = Modules.get(NoSound.class);
        if (module == null || !module.isEnabled()) return;
        if (packet == null || packet.getSound() == null) return;
        String id = packet.getSound().value().location().toString().toLowerCase();
        if (module.shouldMute(id)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSoundEntityEvent", at = @At("HEAD"), cancellable = true)
    private void havoc$filterSoundFromEntity(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        NoSound module = Modules.get(NoSound.class);
        if (module == null || !module.isEnabled()) return;
        if (packet == null || packet.getSound() == null) return;
        String id = packet.getSound().value().location().toString().toLowerCase();
        if (module.shouldMute(id)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleParticleEvent", at = @At("HEAD"))
    private void havoc$heuristicParticle(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        var p = packet.getParticle();
        // Log raw particles once per hook for diagnostics
        DebugLog.info("Particle packet type=%s pos=%.2f %.2f %.2f count=%d",
                p.getType().toString(),
                packet.getX(), packet.getY(), packet.getZ(),
                packet.getCount());
        StatusEffectHeuristics.handle(p, packet.getX(), packet.getY(), packet.getZ());
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    private void havoc$onPearl(ClientboundSoundPacket packet, CallbackInfo ci) {
        // Opponent cooldown tracking disabled (legacy system).
        /*
        if (!CooldownsState.shouldTrackOpponents()) return;
        if (packet.getSound().value() != SoundEvents.ENTITY_ENDER_PEARL_THROW) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Vec3d pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());

        if (!SoundEntityResolver.isUnambiguousOpponent(pos, 1.6)) return;

        PlayerEntity p = SoundEntityResolver.findClosestOpponent(pos, 1.6);
        if (p == null) return;

        OpponentCooldownManager.start(p.getUuid(), Items.ENDER_PEARL);
        */
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    private void havoc$onPlaySound(ClientboundSoundPacket packet, CallbackInfo ci) {
        SoundESP esp = Modules.get(SoundESP.class);
        if (esp == null || !esp.isEnabled()) return;
        if (packet == null || packet.getSound() == null) return;
        Vec3 pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
        esp.handleSound(packet.getSound().value().location(), pos);
    }

    @Inject(method = "handleSoundEntityEvent", at = @At("HEAD"))
    private void havoc$onPlaySoundFromEntity(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        SoundESP esp = Modules.get(SoundESP.class);
        if (esp == null || !esp.isEnabled()) return;
        if (packet == null || packet.getSound() == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(packet.getId());
        if (entity == null) return;

        esp.handleSound(packet.getSound().value().location(), entity.position());
    }

    @Inject(method = "handleUpdateMobEffect", at = @At("HEAD"))
    private void havoc$logStatusEffect(ClientboundUpdateMobEffectPacket packet, CallbackInfo ci) {
        DebugLog.server("Packet: add/refresh effect eid=%d effect=%s amp=%d dur=%d particles=%s icon=%s ambient=%s keepFading=%s",
                packet.getEntityId(),
                packet.getEffect().unwrapKey().map(k -> k.identifier().toString()).orElse("unknown"),
                packet.getEffectAmplifier(),
                packet.getEffectDurationTicks(),
                packet.isEffectVisible(),
                packet.effectShowsIcon(),
                packet.isEffectAmbient(),
                packet.shouldBlend()
        );
    }

    @Inject(method = "handleRemoveMobEffect", at = @At("HEAD"))
    private void havoc$logRemove(ClientboundRemoveMobEffectPacket packet, CallbackInfo ci) {
        DebugLog.server("Packet: remove effect eid=%d (%s) effect=%s",
                packet.entityId(),
                resolveName(packet.entityId()),
                packet.effect().unwrapKey().map(k -> k.identifier().toString()).orElse("unknown"));
    }

    @Unique
    private String resolveName(int entityId) {
        var mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return "unknown";
        Entity e = mc.level.getEntity(entityId);
        if (e == null) return "unknown";
        if (e instanceof Player p) return p.getName().getString();
        return e.getName().getString();
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    private void havoc$onTotemSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        // Opponent cooldown tracking disabled (legacy system).
        /*
        if (!CooldownsState.shouldTrackOpponents()) return;
        if (packet.getSound().value() != SoundEvents.ITEM_TOTEM_USE) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Vec3d pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());

        if (!SoundEntityResolver.isUnambiguousOpponent(pos, 1.6)) return;

        PlayerEntity p = SoundEntityResolver.findClosestOpponent(pos, 1.6);
        if (p == null) return;

        OpponentCooldownManager.start(p.getUuid(), Items.TOTEM_OF_UNDYING);
        */
    }

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void havoc$onTotemStatus(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        if (packet.getEventId() == 35) { // 35 = сработал тотем бессмертия
            ClientPacketListener handler = (ClientPacketListener) (Object) this;
            Minecraft client = Minecraft.getInstance();
            // The handler runs once on the network thread and again after it is rescheduled to the main thread,
            // so an unguarded HEAD hook would count every pop twice.
            if (!client.isSameThread()) return;

            Entity entity = packet.getEntity(handler.getLevel());
            if (entity == null) return;
            if (entity instanceof Player player) {
                TotemPopCounter.recordPop(player);
            }
            if (entity.equals(client.player)) {
                PvpCooldowns cooldowns = Modules.get(PvpCooldowns.class);
                if (cooldowns != null) {
                    cooldowns.tryStartLocalCooldown(Items.TOTEM_OF_UNDYING, ItemCooldownRulesValue.Trigger.TOTEM_POP);
                }
                TotemFX fx = Modules.get(TotemFX.class);
                if (fx != null) {
                    fx.onTotemPop();
                }
                return;
            }
            if (entity instanceof Player player) {
                TotemFX fx = Modules.get(TotemFX.class);
                if (fx != null) {
                    fx.onOpponentTotemPop(player);
                }
                if (CooldownsState.shouldTrackOpponents()) {
                    OpponentCooldownManager.recordUse(player.getUUID(), Items.TOTEM_OF_UNDYING);
                }
            }
        }
    }
}
