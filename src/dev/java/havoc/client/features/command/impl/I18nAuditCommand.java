/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.command.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import havoc.client.config.values.BooleanMapValue;
import havoc.client.events.Events;
import havoc.client.events.impl.I18nPreflightCollectEvent;
import havoc.client.features.command.ClientCommand;
import havoc.client.features.command.CommandContext;
import havoc.client.features.command.CommandInfo;
import havoc.client.features.command.CommandOutput;
import havoc.client.features.gui.clickgui.settings.GroupSetting;
import havoc.client.features.gui.clickgui.settings.ModeSetting;
import havoc.client.features.gui.clickgui.settings.Setting;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleManager;
import havoc.client.util.screen.ClientScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import havoc.client.features.gui.mainmenu.HavocMainMenuScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * Dumps the translation-key fallback chain of every setting, option and module to JSON, so a
 * script can check en_us/ru_ru statically without launching the game once per language.
 *
 * <p>The chains come from the same {@link Setting} methods the GUI resolves labels with, which
 * is why this runs in the game instead of guessing keys from source. Start the client with
 * {@code -Dhavoc.i18n.audit=<file>} to write the report on the title screen and quit, or
 * run {@code @i18naudit <file>} in game.</p>
 */
@CommandInfo(
        id = "i18naudit",
        usage = "@i18naudit <output.json>",
        descriptionKey = "command.i18naudit.description"
)
public final class I18nAuditCommand implements ClientCommand {
    private static final String AUTO_PROPERTY = "havoc.i18n.audit";
    private static boolean autoRunDone;

    public I18nAuditCommand() {
        String target = System.getProperty(AUTO_PROPERTY);
        if (target == null || target.isBlank()) return;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // The main menu means resources, modules and HUD elements have all loaded.
            Screen screen = ClientScreen.current(client);
            if (autoRunDone || !(screen instanceof TitleScreen || screen instanceof HavocMainMenuScreen)) return;
            autoRunDone = true;
            try {
                int entries = write(Path.of(target));
                System.out.println("[i18n-audit] wrote " + entries + " entries to " + target);
            } catch (Exception e) {
                System.out.println("[i18n-audit] failed: " + e);
            }
            client.stop();
        });
    }

    @Override
    public boolean execute(CommandContext ctx) {
        String target = ctx.arg(0);
        if (target == null || target.isBlank()) {
            CommandOutput.send("Usage: " + metadata().usage());
            return true;
        }
        try {
            CommandOutput.success("i18n audit: " + write(Path.of(target)) + " entries -> " + target);
        } catch (Exception e) {
            CommandOutput.error("i18n audit failed: " + e.getMessage());
        }
        return true;
    }

    private static int write(Path target) throws Exception {
        JsonArray settings = new JsonArray();
        I18nPreflightCollectEvent event = Events.BUS.post(new I18nPreflightCollectEvent("i18n audit"));
        for (I18nPreflightCollectEvent.Entry entry : event.entries()) {
            if (entry == null || entry.setting() == null) continue;
            settings.add(describe(entry.path(), entry.setting()));
        }

        JsonArray modules = new JsonArray();
        Field description = Module.class.getDeclaredField("description");
        description.setAccessible(true);
        for (Module module : ModuleManager.getModules()) {
            JsonObject row = new JsonObject();
            row.addProperty("id", module.name());
            row.addProperty("descriptionKey", (String) description.get(module));
            modules.add(row);
        }

        JsonObject root = new JsonObject();
        root.add("modules", modules);
        root.add("settings", settings);
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.writeString(target, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
        return settings.size() + modules.size();
    }

    private static JsonObject describe(String path, Setting setting) throws Exception {
        JsonObject row = new JsonObject();
        row.addProperty("path", path);
        row.addProperty("id", setting.getId());
        row.addProperty("type", setting.getClass().getSimpleName());
        boolean nameEnabled = flag(setting, "i18nNameEnabled");
        boolean optionsEnabled = flag(setting, "i18nOptionsEnabled");
        row.addProperty("nameEnabled", nameEnabled);
        row.add("keys", array(setting.getTranslationKeys()));

        JsonObject options = new JsonObject();
        if (optionsEnabled) {
            Method optionKeys = Setting.class.getDeclaredMethod("optionTranslationKeys", String.class);
            optionKeys.setAccessible(true);
            for (String option : optionIds(setting)) {
                @SuppressWarnings("unchecked")
                List<String> keys = (List<String>) optionKeys.invoke(setting, option);
                options.add(option, array(keys));
            }
        }
        row.add("options", options);
        return row;
    }

    private static Collection<String> optionIds(Setting setting) throws Exception {
        if (setting instanceof ModeSetting) {
            Method ids = ModeSetting.class.getDeclaredMethod("optionIds");
            ids.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) ids.invoke(setting);
            return result;
        }
        if (setting instanceof GroupSetting group && group.getConfigValue() instanceof BooleanMapValue map) {
            return map.getAll().keySet().stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private static boolean flag(Setting setting, String name) throws Exception {
        Field field = Setting.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getBoolean(setting);
    }

    private static JsonArray array(List<String> values) {
        JsonArray out = new JsonArray();
        for (String value : values) {
            if (value != null && !value.isBlank()) out.add(value);
        }
        return out;
    }
}
