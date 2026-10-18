/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixins;

import havoc.client.mixininterface.IBiome;
import havoc.client.mixins.accessors.BiomeClimateSettingsAccessor;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Biome.class)
public abstract class BiomeMixin implements IBiome {
    @Unique
    private float havoc$downfall;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void havoc$captureDownfall(
            @Coerce Object climateSettings,
            EnvironmentAttributeMap attributes,
            BiomeSpecialEffects specialEffects,
            BiomeGenerationSettings generationSettings,
            MobSpawnSettings mobSettings,
            CallbackInfo ci
    ) {
        havoc$downfall = ((BiomeClimateSettingsAccessor) climateSettings).havoc$getDownfall();
    }

    @Override
    public float havoc$getDownfall() {
        return havoc$downfall;
    }
}
