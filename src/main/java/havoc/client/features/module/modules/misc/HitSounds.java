/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.misc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import havoc.client.config.DisableSettingI18n;
import havoc.client.config.values.BooleanMapValue;
import havoc.client.config.values.BooleanValue;
import havoc.client.config.values.EnumValue;
import havoc.client.config.values.NumberValue;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import havoc.client.features.relations.CategoryRules;
import havoc.client.features.relations.CategoryType;
import havoc.client.features.relations.EntityFilters;
import havoc.client.util.sound.SoundAsset;
import havoc.client.util.sound.SoundCatalog;
import havoc.client.util.sound.SoundKey;
import havoc.client.util.sound.SoundOptions;

@ModuleInfo(
        id = "hitsounds",
        displayName = "HitSounds",
        category = ModuleCategory.MISC, subcategory = ModuleSubcategory.CLIENT,
        description = "module.hitsounds.description")
public class HitSounds extends Module {

    private static final String SETTING_SOURCES = "sources";
    private static final String SETTING_VOLUME = "volume";
    private static final String SETTING_DEFAULT_SOUND = "default_sound";
    private static final String SETTING_GROUP_OVERRIDES = "group_overrides";
    private static final String SETTING_FRIEND_SOUND = "friend_sound";
    private static final String SETTING_ENEMY_SOUND = "enemy_sound";
    private static final String SETTING_STAFF_SOUND = "staff_sound";
    private static final String SETTING_ENTITY_SOUND = "entity_sound";
    private static final String SETTING_OVERRIDE_IGNORED = "override_ignored";
    private static final String SETTING_IGNORED_ENTITY_SOUND = "ignored_entity_sound";

    private final BooleanMapValue sources = group(
            "hitsounds_sources",
            SETTING_SOURCES,
            new java.util.LinkedHashMap<>() {{
                put("players", true);
                put("entities", true);
            }}
    );
    private final NumberValue<Float> volumeValue =
            num("hitsounds_volume", SETTING_VOLUME, 1.0f, 0.0f, 1.0f);
    @DisableSettingI18n(name = false, options = true)
    private final EnumValue<SoundType> playersSound = enumSetting(
            "hitsounds_players_sound",
            SETTING_DEFAULT_SOUND,
            SoundType.BELL,
            SoundType.values()
    );
    private final BooleanValue groupsEnabled =
            bool("hitsounds_groups_enabled", SETTING_GROUP_OVERRIDES, true);
    @DisableSettingI18n(name = false, options = true)
    private final EnumValue<SoundType> friendSound = enumSetting(
            "hitsounds_friend_sound",
            SETTING_FRIEND_SOUND,
            SoundType.BUBBLE,
            SoundType.values()
    );
    @DisableSettingI18n(name = false, options = true)
    private final EnumValue<SoundType> enemySound = enumSetting(
            "hitsounds_enemy_sound",
            SETTING_ENEMY_SOUND,
            SoundType.CRITICAL,
            SoundType.values()
    );
    @DisableSettingI18n(name = false, options = true)
    private final EnumValue<SoundType> staffSound = enumSetting(
            "hitsounds_staff_sound",
            SETTING_STAFF_SOUND,
            SoundType.CRIME,
            SoundType.values()
    );

    @DisableSettingI18n(name = false, options = true)
    private final EnumValue<SoundType> entitiesSound = enumSetting(
            "hitsounds_entities_sound",
            SETTING_ENTITY_SOUND,
            SoundType.BONK,
            SoundType.values()
    );
    private final BooleanValue entitiesOverrideIgnored =
            bool("hitsounds_entities_override_ignored", SETTING_OVERRIDE_IGNORED, false);
    @DisableSettingI18n(name = false, options = true)
    private final EnumValue<SoundType> entitiesIgnoredSound = enumSetting(
            "hitsounds_entities_ignored_sound",
            SETTING_IGNORED_ENTITY_SOUND,
            SoundType.BUBBLE,
            SoundType.values()
    );

    public void handleHit(Entity target) {
        if (!isEnabled() || target == null) return;

        if (target instanceof Player p) {
            if (!sources.get("players")) return;
            playForPlayer(p);
        } else {
            if (!sources.get("entities")) return;
            playForEntity(target);
        }
    }


    private void playForPlayer(Player p) {
        if (!groupsEnabled.get()) {
            playSoundKey(playersSound.get());
            return;
        }

        CategoryType type = CategoryRules.determine(p.getGameProfile().name());
        switch (type) {
            case FRIEND -> playSoundKey(friendSound.get());
            case BEDWARS_SELF -> playSoundKey(friendSound.get());
            case ENEMY -> playSoundKey(enemySound.get());
            case BEDWARS_ENEMY -> playSoundKey(enemySound.get());
            case STAFF -> playSoundKey(staffSound.get());
            default -> playSoundKey(playersSound.get());
        }
    }

    private void playForEntity(Entity e) {
        if (entitiesOverrideIgnored.get()) {
            var id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
            if (id != null && EntityFilters.get().isIgnoredEntity(id.toString())) {
                playSoundKey(entitiesIgnoredSound.get());
                return;
            }
        }
        playSoundKey(entitiesSound.get());
    }

    private void playSoundKey(SoundType key) {
        if (key == null) return;
        double vol = Math.min(1.0, volumeValue.get());
        key.play(SoundOptions.gain(vol));
    }

    @SoundCatalog(namespace = "havoc", root = "sounds/hitsounds", idPrefix = "hitsounds")
    private enum SoundType implements EnumValue.IdProvider, SoundKey {
        @SoundAsset("bell.wav")
        BELL("bell"),
        @SoundAsset("bonk.wav")
        BONK("bonk"),
        @SoundAsset("bubble.wav")
        BUBBLE("bubble"),
        @SoundAsset("crime.wav")
        CRIME("crime"),
        @SoundAsset("critical.wav")
        CRITICAL("critical");

        private final String id;

        SoundType(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }
}
