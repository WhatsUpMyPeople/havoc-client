/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.visuals;

import net.minecraft.util.Mth;
import havoc.client.config.values.NumberValue;
import havoc.client.events.EventHandler;
import havoc.client.events.impl.LightmapModifyEvent;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;

@ModuleInfo(
        id = "fullbright",
        displayName = "FullBright",
        category = ModuleCategory.VISUALS, subcategory = ModuleSubcategory.COSMETIC,
        description = "module.fullbright.description")
public class FullBright extends Module {

    private static final String SETTING_MIN_LIGHT = "min_light";
    private final NumberValue<Integer> minLight =
            num("fullBrightMinLight", SETTING_MIN_LIGHT, 15, 1, 15);

    public int getMinLight() {
        return isEnabled() ? minLight.get() : 0;
    }

    public float getMinLightStrength() {
        return isEnabled() ? Mth.clamp(minLight.get(), 1, 15) / 15.0f : 0.0f;
    }

    @EventHandler
    private void onLightmapState(LightmapModifyEvent event) {
        if (!isEnabled()) return;
        event.raiseMinimumLight(getMinLightStrength());
    }
}
