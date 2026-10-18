/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.combat.autoanchor;

import havoc.client.util.combat.ExplosionDamageCandidate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

public record AutoAnchorData(
        BlockPos pos,
        BlockHitResult hitResult,
        float damage,
        float selfDamage,
        boolean overrideDamage,
        boolean existingAnchor,
        int charges
) implements ExplosionDamageCandidate {
    public boolean charged() {
        return existingAnchor && charges > 0;
    }
}
