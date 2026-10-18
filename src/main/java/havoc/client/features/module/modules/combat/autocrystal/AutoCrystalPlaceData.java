/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.combat.autocrystal;

import havoc.client.util.combat.ExplosionDamageCandidate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

public record AutoCrystalPlaceData(
        BlockPos pos,
        BlockHitResult bhr,
        float damage,
        float selfDamage,
        boolean overrideDamage
) implements ExplosionDamageCandidate {
}
