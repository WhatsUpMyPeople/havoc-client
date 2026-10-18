/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.movement;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import havoc.client.config.values.BooleanValue;
import havoc.client.config.values.EnumValue;
import havoc.client.config.values.NumberValue;
import havoc.client.events.EventHandler;
import havoc.client.events.impl.MovementInputEvent;
import havoc.client.events.impl.PlayerSafeWalkEvent;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import havoc.client.util.entity.EagleUtil;

@ModuleInfo(
        id = "safewalk",
        displayName = "SafeWalk",
        category = ModuleCategory.MOVEMENT, subcategory = ModuleSubcategory.BASIC,
        description = "module.safewalk.description")
public final class SafeWalk extends Module {

    private final Minecraft mc = Minecraft.getInstance();
    private final EnumValue<Mode> mode = enumMode("mode", Mode.ON_EDGE);
    private final EnumValue<OnEdgeMode> onEdgeMode =
            visibleWhen(enumMode("on_edge_mode", OnEdgeMode.STOP), this::isOnEdgeMode);
    private final NumberValue<Double> edgeDistance =
            visibleWhen(num("edge_distance", 0.1, 0.01, 0.5), this::isOnEdgeMode);
    private final NumberValue<Integer> keepTicks =
            visibleWhen(num("keep_ticks", 2, 1, 20), this::isOnEdgeMode);
    private final NumberValue<Integer> sneakTicks =
            visibleWhen(num("sneak_ticks", 0, 0, 20), this::isOnEdgeMode);
    private final BooleanValue jump =
            visibleWhen(bool("jump", false), this::isOnEdgeMode);
    private final EagleUtil.EdgeRecovery edgeRecovery = new EagleUtil.EdgeRecovery();

    private boolean isOnEdgeMode() {
        return mode.get() == Mode.ON_EDGE;
    }

    @Override
    public void onDisable() {
        edgeRecovery.reset();
    }

    public boolean handleExternalOnEdge(LocalPlayer player, MovementInputEvent event) {
        if (player == null || event == null || player.getAbilities().flying) {
            edgeRecovery.reset();
            return false;
        }

        if (!player.onGround() && !event.isSneak()) {
            edgeRecovery.reset();
            return false;
        }

        return edgeRecovery.handleOnEdge(
                player,
                event,
                edgeDistance.get(),
                onEdgeMode.get().recoveryMode(),
                keepTicks.get(),
                sneakTicks.get(),
                jump.get()
        );
    }

    @EventHandler
    private void onSafeWalk(PlayerSafeWalkEvent event) {
        if (!isEnabled() || mode.get() != Mode.SAFE) {
            return;
        }

        LocalPlayer player = mc.player;
        if (player == null || !player.onGround() || player.getAbilities().flying) {
            return;
        }

        event.setSafeWalk(true);
    }

    @EventHandler
    private void onMovementInput(MovementInputEvent event) {
        if (!isEnabled() || mode.get() != Mode.ON_EDGE) {
            edgeRecovery.reset();
            return;
        }

        LocalPlayer player = mc.player;
        if (player == null || player.getAbilities().flying) {
            edgeRecovery.reset();
            return;
        }

        if (!player.onGround() && !event.isSneak()) {
            edgeRecovery.reset();
            return;
        }

        handleExternalOnEdge(player, event);
    }

    public enum Mode {
        SAFE,
        ON_EDGE
    }

    @RequiredArgsConstructor
    public enum OnEdgeMode {
        STOP(EagleUtil.RecoveryMode.STOP),
        INVERT(EagleUtil.RecoveryMode.INVERT),
        CENTER(EagleUtil.RecoveryMode.CENTER);

        private final EagleUtil.RecoveryMode recoveryMode;

        public EagleUtil.RecoveryMode recoveryMode() {
            return recoveryMode;
        }
    }
}
