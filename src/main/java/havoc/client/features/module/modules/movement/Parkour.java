/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.movement;

import net.minecraft.client.Minecraft;
import havoc.client.config.values.NumberValue;
import havoc.client.events.EventHandler;
import havoc.client.events.impl.MovementInputEvent;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;

@ModuleInfo(
        id = "parkour",
        displayName = "Parkour",
        category = ModuleCategory.MOVEMENT, subcategory = ModuleSubcategory.BASIC,
        description = "module.parkour.description")
public final class Parkour extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    private final NumberValue<Float> jumpFactor = num("jump_factor", 0.01f, 0.001f, 0.3f);

    @EventHandler
    private void onMovementInput(MovementInputEvent event) {
        if (!isEnabled() || mc.player == null || mc.level == null) {
            return;
        }

        if (!mc.player.onGround()) {
            return;
        }
        if (event.isJump()) {
            return;
        }
        if (event.isSneak()) {
            return;
        }
        if (!event.isForward() && !event.isBackward() && !event.isLeft() && !event.isRight()) {
            return;
        }

        boolean hasGroundBelow = mc.level.getBlockCollisions(
                mc.player,
                mc.player.getBoundingBox()
                        .inflate(-jumpFactor.get(), 0.0, -jumpFactor.get())
                        .move(0.0, -0.99, 0.0)
        ).iterator().hasNext();

        if (!hasGroundBelow && mc.player.getDeltaMovement().y <= 0.0) {
            event.setJump(true);
        }
    }
}
