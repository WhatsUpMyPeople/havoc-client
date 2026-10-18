/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.combat;

import havoc.client.util.block.interaction.BlockInteractionPipeline;
import havoc.client.util.player.inventory.InventoryActionKind;
import havoc.client.util.player.inventory.InventorySearchScope;
import havoc.client.util.player.inventory.InventorySwap;
import havoc.client.util.player.inventory.InventorySwapMode;
import havoc.client.util.player.inventory.InventorySwapRequest;
import havoc.client.util.player.inventory.InventorySwapVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Predicate;

/** Shared inventory and block-use mechanics for explosive combat modules. */
public final class CombatBlockUseUtil {
    private CombatBlockUseUtil() {
    }

    public static boolean useHeldOrSwap(Minecraft mc,
                                        InteractionHand heldHand,
                                        Predicate<ItemStack> predicate,
                                        BlockHitResult hitResult,
                                        InventorySearchScope scope,
                                        InventorySwapVisibility visibility,
                                        boolean restore) {
        if (!canUse(mc, hitResult) || predicate == null) return false;
        if (heldHand != null) {
            return useOnWithRubberHand(mc, heldHand, hitResult);
        }

        return InventorySwap.INSTANCE.execute(InventorySwapRequest.builder(
                        predicate,
                        () -> useOnWithRubberHand(mc, InteractionHand.MAIN_HAND, hitResult)
                )
                .scope(scope != null ? scope : InventorySearchScope.FULL)
                .visibility(visibility != null ? visibility : InventorySwapVisibility.SILENT)
                .restore(restore)
                .actionKind(InventoryActionKind.BLOCK_INTERACT)
                .build());
    }

    public static boolean hasHeldOrInventoryItem(InteractionHand heldHand,
                                                  Predicate<ItemStack> predicate,
                                                  InventorySearchScope scope) {
        if (heldHand != null) return true;
        if (predicate == null) return false;
        return InventorySwap.INSTANCE.findSlotForMode(predicate, silentModeFor(scope)).found();
    }

    public static boolean useOn(Minecraft mc, InteractionHand hand, BlockHitResult hitResult) {
        if (!canUse(mc, hitResult) || hand == null) return false;
        InteractionResult result = BlockInteractionPipeline.INSTANCE.useOnBlock(
                CombatBlockUseUtil.class, mc.player, hand, -1, 0, hitResult, false
        );
        mc.player.swing(hand);
        return result != InteractionResult.FAIL;
    }

    public static boolean useOnWithRubberHand(Minecraft mc, InteractionHand hand, BlockHitResult hitResult) {
        if (!canUse(mc, hitResult) || hand == null) return false;
        return RubberHandUseUtil.runBlockUse(mc, () -> useOn(mc, hand, hitResult));
    }

    private static InventorySwapMode silentModeFor(InventorySearchScope scope) {
        InventorySearchScope safeScope = scope != null ? scope : InventorySearchScope.FULL;
        return switch (safeScope) {
            case HOTBAR -> InventorySwapMode.SILENT;
            case INVENTORY -> InventorySwapMode.INVENTORY_SILENT;
            case FULL -> InventorySwapMode.SILENT_FULL;
        };
    }

    private static boolean canUse(Minecraft mc, BlockHitResult hitResult) {
        return mc != null && mc.player != null && mc.gameMode != null && hitResult != null;
    }
}
