/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 *
 * -----------------------------------------------------------------------------
 * Rewritten AutoCrystal (xrose-port):
 *
 * The previous implementation went through Havoc's heavy rotation manager,
 * target manager, protocol anticheat presets and crystal tracker. It would
 * never actually fire for most users because any one of a dozen gate checks
 * (anticheat preset, rotation ownership, target manager hand-off, hotbar
 * reset ticks, etc.) could silently abort the tick. This rewrite ports
 * xrose's clean CrystalAura: find target, attack best crystal in hit range,
 * else place a new crystal; nothing clever.
 *
 * The mixin AutoCrystalPacketMixin still calls onCrystalSpawned() /
 * onCrystalsRemoved() on this class — kept as no-ops so the mixin stays
 * valid without rewiring the mixin plugin.
 *
 * The macro key is preserved: with it NONE the module acts whenever enabled,
 * with it bound the module only acts while the key is held.
 * -----------------------------------------------------------------------------
 */

package havoc.client.features.module.modules.combat;

import havoc.client.config.values.BindMode;
import havoc.client.config.values.BooleanValue;
import havoc.client.config.values.KeyBindValue;
import havoc.client.config.values.NumberValue;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import havoc.client.features.module.Notifier;
import havoc.client.mixins.accessors.PlayerInventoryAccessor;
import havoc.client.util.input.KeyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@ModuleInfo(
        id = "autocrystal",
        displayName = "AutoCrystal",
        aliases = "CrystalAura",
        category = ModuleCategory.COMBAT,
        subcategory = ModuleSubcategory.ATTACK,
        description = "module.autocrystal.description")
public class AutoCrystal extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    // ------------------------------------------------------------------
    //                            Settings
    // ------------------------------------------------------------------

    private final NumberValue<Double> targetRange =
            num("autocrystalTargetRange", "target_range", 10.0, 3.0, 16.0);
    private final NumberValue<Double> placeRange =
            num("autocrystalPlaceRange", "place_range", 4.5, 1.0, 6.0);
    private final NumberValue<Double> hitRange =
            num("autocrystalHitRange", "hit_range", 4.0, 2.0, 6.0);
    private final NumberValue<Double> minDamage =
            num("autocrystalMinDamage", "min_damage", 6.0, 0.5, 20.0);
    private final NumberValue<Double> maxSelfDamage =
            num("autocrystalMaxSelfDamage", "max_self_damage", 8.0, 0.5, 20.0);
    private final NumberValue<Integer> placeDelayMs =
            num("autocrystalPlaceDelay", "place_delay_ms", 200, 0, 1000);

    private final BooleanValue autoSwitch =
            bool("autocrystalAutoSwitch", "auto_switch", true);
    private final BooleanValue syncCooldown =
            bool("autocrystalSyncCooldown", "sync_cooldown", true);
    private final BooleanValue noSuicide =
            bool("autocrystalNoSuicide", "no_suicide", true);
    private final BooleanValue ignoreWalls =
            bool("autocrystalIgnoreWalls", "ignore_walls", false);
    private final BooleanValue swingHand =
            bool("autocrystalSwing", "swing_hand", true);
    private final BooleanValue rotate =
            bool("autocrystalRotate", "rotate", true);
    private final BooleanValue debug =
            bool("autocrystalDebug", "debug", true);

    // Hold-to-act macro. NONE = act whenever enabled; bound = only while held.
    private final KeyBindValue macroKey =
            bind("autocrystalMacroKey", "macro_key", "NONE", BindMode.HOLD);

    // ------------------------------------------------------------------
    //                            State
    // ------------------------------------------------------------------

    private LivingEntity currentTarget;
    private int restoreSlot = -1;
    private long lastPlaceMs = 0L;
    private int debugTicker = 0;
    private String lastDebugLine = "";

    // ------------------------------------------------------------------
    //                      Mixin-callback shims
    // ------------------------------------------------------------------
    // AutoCrystalPacketMixin pokes these; kept as no-ops. The ported logic
    // walks the EndCrystal entity list every tick instead of maintaining a
    // tracker, so nothing to update here.

    public void onCrystalSpawned(EndCrystal crystal) { /* no-op */ }

    public void onCrystalsRemoved() { /* no-op */ }

    /** TargetHud status string: shown on the HUD under the current target. */
    public String getTargetHudStatus(LivingEntity hudTarget) {
        if (!isEnabled() || hudTarget == null) return null;
        if (currentTarget == null || hudTarget != currentTarget) return null;
        if (mc.player == null || mc.level == null) return "Idle";
        if (!isMacroActive()) return "Waiting";

        long cooldown = Math.max(0L, placeDelayMs.get() - (System.currentTimeMillis() - lastPlaceMs));
        if (cooldown > 0L) return "Place in " + cooldown + "ms";
        return "Hunting";
    }

    // ------------------------------------------------------------------
    //                         Lifecycle
    // ------------------------------------------------------------------

    @Override
    public void onEnable() {
        resetState();
        if (debug.get()) Notifier.info("AutoCrystal armed");
    }

    @Override
    public void onDisable() {
        resetState();
        if (debug.get()) Notifier.clear("autocrystal_status");
    }

    private void debugStatus(String line) {
        if (!debug.get()) return;
        if (!line.equals(lastDebugLine)) {
            Notifier.update("autocrystal_status", line);
            lastDebugLine = line;
        }
    }

    private void resetState() {
        this.currentTarget = null;
        this.restoreSlot = -1;
        this.lastPlaceMs = 0L;
    }

    /** Hold-to-act gate. */
    private boolean isMacroActive() {
        String combo = macroKey.get();
        if (combo == null || combo.isEmpty() || combo.equalsIgnoreCase("NONE")) return true;
        return KeyManager.isComboHeldAllowScreen(combo);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        if (!isMacroActive()) {
            debugStatus("waiting on macro key");
            return;
        }

        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.gameMode == null) {
            debugStatus("no player/level");
            return;
        }

        // Restore hotbar slot from a prior silent swap if the user has not
        // meanwhile picked a different slot themselves.
        if (restoreSlot != -1 && selectedSlot(player) != restoreSlot) {
            switchSlot(player, restoreSlot);
            restoreSlot = -1;
        }

        currentTarget = findTarget(player, level);
        if (currentTarget == null) {
            debugStatus("no target in " + targetRange.get().intValue() + " blocks");
            return;
        }

        double tdist = Math.sqrt(player.distanceToSqr(currentTarget));

        // Attack first (free damage), place only if nothing to hit.
        if (attackBestCrystal(player)) {
            debugStatus("hit crystal on " + currentTarget.getName().getString()
                    + " (" + String.format("%.1f", tdist) + "b)");
            return;
        }

        int placed = tryPlaceCrystal(player, level);
        if (placed == 1) {
            debugStatus("placed on " + currentTarget.getName().getString()
                    + " (" + String.format("%.1f", tdist) + "b)");
        } else if (placed == -1) {
            long cd = Math.max(0L, placeDelayMs.get() - (System.currentTimeMillis() - lastPlaceMs));
            debugStatus("place cooldown " + cd + "ms");
        } else if (placed == -2) {
            debugStatus("no good spot vs " + currentTarget.getName().getString());
        } else if (placed == -3) {
            debugStatus("no end crystal in hotbar");
        }
    }

    // ------------------------------------------------------------------
    //                           Target
    // ------------------------------------------------------------------

    private LivingEntity findTarget(LocalPlayer player, ClientLevel level) {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        double rangeSq = targetRange.get() * targetRange.get();

        for (Player candidate : level.players()) {
            if (candidate == player) continue;
            if (!candidate.isAlive()) continue;
            if (candidate.isSpectator() || candidate.isCreative()) continue;
            double distSq = player.distanceToSqr(candidate);
            if (distSq > rangeSq) continue;
            if (distSq >= bestDist) continue;
            best = candidate;
            bestDist = distSq;
        }
        return best;
    }

    // ------------------------------------------------------------------
    //                        Attack crystals
    // ------------------------------------------------------------------

    private boolean attackBestCrystal(LocalPlayer player) {
        LivingEntity target = currentTarget;
        if (target == null) return false;

        if (syncCooldown.get() && player.getAttackStrengthScale(0.0F) < 0.9F) return false;

        EndCrystal best = null;
        float bestDamage = -1.0F;

        for (EndCrystal crystal : mc.level.getEntitiesOfClass(
                EndCrystal.class,
                player.getBoundingBox().inflate(hitRange.get()))) {
            if (!crystal.isAlive()) continue;

            Vec3 center = crystal.position();
            float selfDamage = explosionDamage(player, center);
            if (!selfDamageAllowed(player, selfDamage)) continue;

            float damage = explosionDamage(target, center);
            if (damage < minDamage.get().floatValue()) continue;
            if (damage <= bestDamage) continue;

            best = crystal;
            bestDamage = damage;
        }

        if (best == null) return false;

        if (rotate.get()) aimAt(player, best.position());

        mc.gameMode.attack(player, best);
        if (swingHand.get()) player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    // ------------------------------------------------------------------
    //                         Place crystals
    // ------------------------------------------------------------------

    /**
     * @return  1 = placed; -1 = on cooldown; -2 = no safe/damaging spot; -3 = no crystal in hotbar.
     */
    private int tryPlaceCrystal(LocalPlayer player, ClientLevel level) {
        if (System.currentTimeMillis() - lastPlaceMs < placeDelayMs.get()) return -1;

        Placement placement = findBestPlacement(player, level);
        if (placement == null) return -2;

        if (!switchToCrystal(player)) return -3;

        if (rotate.get()) aimAt(player, placement.facePoint);

        BlockHitResult hit = new BlockHitResult(placement.facePoint, Direction.UP, placement.support, false);
        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        if (swingHand.get()) player.swing(InteractionHand.MAIN_HAND);

        finishSwitch(player);
        lastPlaceMs = System.currentTimeMillis();
        return 1;
    }

    /** Snap yaw/pitch to look at the given world point so the server accepts the next interaction. */
    private static void aimAt(LocalPlayer player, Vec3 point) {
        double dx = point.x - player.getX();
        double dy = point.y - (player.getY() + player.getEyeHeight());
        double dz = point.z - player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontal)));
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yHeadRot = yaw;
        player.yBodyRot = yaw;
    }

    private Placement findBestPlacement(LocalPlayer player, ClientLevel level) {
        LivingEntity target = currentTarget;
        if (target == null) return null;

        int r = Mth.ceil(placeRange.get());
        BlockPos playerPos = player.blockPosition();
        boolean walls = ignoreWalls.get();
        double place = placeRange.get();

        Placement best = null;
        float bestDamage = Math.max(minDamage.get().floatValue(), 0.0F);

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-r, -2, -r),
                playerPos.offset(r,  2, r))) {

            if (!canPlaceCrystal(level, pos)) continue;

            Vec3 explosion = Vec3.atLowerCornerOf(pos).add(0.5, 1.0, 0.5);

            if (!walls && !isVisible(player, explosion)) continue;

            double footDist = player.position().distanceTo(explosion);
            double eyeDist  = player.getEyePosition().distanceTo(explosion);
            if (footDist > place && eyeDist > place) continue;

            float selfDamage = explosionDamage(player, explosion);
            if (!selfDamageAllowed(player, selfDamage)) continue;

            float damage = explosionDamage(target, explosion);
            if (damage <= bestDamage) continue;

            best = new Placement(pos.immutable(), explosion);
            bestDamage = damage;
        }
        return best;
    }

    private boolean canPlaceCrystal(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && !state.canBeReplaced()) return false;

        BlockState support = level.getBlockState(pos.below());
        if (!support.is(Blocks.OBSIDIAN)
                && !support.is(Blocks.CRYING_OBSIDIAN)
                && !support.is(Blocks.BEDROCK)) {
            return false;
        }

        AABB box = new AABB(pos);
        for (Entity entity : level.getEntities(null, box.inflate(0.05))) {
            if (entity instanceof LivingEntity living && living.isAlive()) return false;
        }
        return true;
    }

    private boolean isVisible(LocalPlayer player, Vec3 point) {
        HitResult hit = mc.level.clip(new ClipContext(
                player.getEyePosition(),
                point,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player));
        return hit.getType() == HitResult.Type.MISS;
    }

    // ------------------------------------------------------------------
    //                       Hotbar switching
    // ------------------------------------------------------------------

    private boolean switchToCrystal(LocalPlayer player) {
        if (player.getMainHandItem().is(Items.END_CRYSTAL)) return true;
        if (!autoSwitch.get()) return false;

        int crystalSlot = -1;
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).is(Items.END_CRYSTAL)) {
                crystalSlot = slot;
                break;
            }
        }
        if (crystalSlot == -1) return false;

        restoreSlot = selectedSlot(player);
        switchSlot(player, crystalSlot);
        return true;
    }

    private void finishSwitch(LocalPlayer player) {
        if (restoreSlot != -1) {
            switchSlot(player, restoreSlot);
            restoreSlot = -1;
        }
    }

    private static int selectedSlot(LocalPlayer player) {
        return ((PlayerInventoryAccessor) player.getInventory()).havoc$getSelectedSlot();
    }

    private void switchSlot(LocalPlayer player, int slot) {
        if (slot < 0 || slot > 8) return;
        if (selectedSlot(player) == slot) return;
        ((PlayerInventoryAccessor) player.getInventory()).havoc$setSelectedSlot(slot);
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    // ------------------------------------------------------------------
    //                      Explosion damage math
    //            (ported verbatim from xrose CrystalAura)
    // ------------------------------------------------------------------

    private boolean selfDamageAllowed(LocalPlayer player, float selfDamage) {
        if (selfDamage > maxSelfDamage.get().floatValue()) return false;
        if (!noSuicide.get()) return true;
        return selfDamage < player.getHealth() - 0.5F;
    }

    private static float explosionDamage(LivingEntity target, Vec3 explosion) {
        Vec3 center = target.getBoundingBox().getCenter();
        double dist = center.distanceTo(explosion);
        double radius = 12.0;
        if (dist >= radius) return 0.0F;

        double impact = (1.0 - dist / radius) * exposure(target, explosion, center);
        double raw = (impact * impact + impact) / 2.0 * 7.0 * 6.0 * 2.0 + 1.0;

        float armor = target.getArmorValue();
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float f = 2.0F + toughness / 4.0F;
        float clamped = Mth.clamp(armor - (float) raw / f, armor * 0.2F, 20.0F);
        return (float) raw * (1.0F - clamped / 25.0F);
    }

    private static float exposure(LivingEntity target, Vec3 explosion, Vec3 center) {
        HitResult hit = target.level().clip(new ClipContext(
                explosion, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target));
        return hit.getType() == HitResult.Type.MISS ? 1.0F : 0.7F;
    }

    private record Placement(BlockPos support, Vec3 facePoint) { }
}
