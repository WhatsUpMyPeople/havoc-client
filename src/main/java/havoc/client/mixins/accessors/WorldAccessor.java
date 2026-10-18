/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.accessors;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Level.class)
public interface WorldAccessor {

    @Accessor("isClientSide")
    boolean havoc$isClient();

    @Accessor("rainLevel")
    float havoc$getRainGradientRaw();

    @Accessor("oRainLevel")
    float havoc$getLastRainGradientRaw();

    @Accessor("thunderLevel")
    float havoc$getThunderGradientRaw();

    @Accessor("oThunderLevel")
    float havoc$getLastThunderGradientRaw();
}


