/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.gui.clickgui.layout.screen.modules;

import havoc.client.features.gui.chat.diagnostics.FailureDiagnostics;

import havoc.client.features.gui.clickgui.ClickGuiRenderer;
import havoc.client.features.gui.clickgui.layout.screen.settings.implement.module.ModuleComponent;
import havoc.client.features.gui.clickgui.settings.Setting;
import havoc.client.features.gui.clickgui.settings.SettingErrorView;
import havoc.client.runtime.error.ErrorHandler;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleManager;
import havoc.client.features.module.ModuleSubcategoryDefinition;
import havoc.client.features.module.ModuleSubcategoryRegistry;
import havoc.client.features.gui.preview.VisualPreviewRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

enum ModulesMenuResolver {
    ;

    static List<ModuleComponent.CardEntry> buildCards(ModulesMenuCategory category) {
        return buildCards(category, null);
    }

    static List<ModuleComponent.CardEntry> buildCards(ModulesMenuCategory category, ModuleSubcategoryDefinition subcategory) {
        List<ModuleComponent.CardEntry> out = new ArrayList<>();
        if (category == null) return out;

        for (Module module : ModuleManager.getModules()) {
            if (module == null) continue;
            if (!category.matches(module.getCategory())) continue;
            ModuleSubcategoryDefinition resolvedSubcategory = ModuleSubcategoryRegistry.resolve(module);
            if (subcategory != null
                    && (resolvedSubcategory == null || !resolvedSubcategory.key().equals(subcategory.key()))) continue;
            String bind = module.getKeyBindSetting() != null && module.getKeyBindSetting().getValue() != null
                    ? module.getKeyBindSetting().getValue().get()
                    : "";
            out.add(new ModuleComponent.CardEntry(
                    module.name(),
                    module.getDisplayName(),
                    SettingErrorView.description(module),
                    bind == null ? "" : bind,
                    !module.getSettings().isEmpty() || VisualPreviewRegistry.supports(module) || SettingErrorView.hasFailure(module),
                    module.isEnabled(),
                    !ErrorHandler.blocked(module),
                    SettingErrorView.hasFailure(module),
                    module.isShownInModuleList(),
                    module.getAliases()
            ));
        }

        out.sort(Comparator.comparing(ModuleComponent.CardEntry::title, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    static void toggleEntry(String id) {
        Module module = moduleById(id);
        if (module == null) return;
        if (ErrorHandler.blocked(module)) { FailureDiagnostics.showSummaryFor(module); return; }
        module.toggle();
    }

    static void beginBind(String id) {
        Module module = moduleById(id);
        if (module == null || module.getKeyBindSetting() == null) return;
        ClickGuiRenderer.beginKeyBind(module.getKeyBindSetting());
    }

    static void toggleModuleListVisibility(String id) {
        Module module = moduleById(id);
        if (module == null) return;
        module.toggleShownInModuleList();
    }

    static ResolvedSettings resolveSettings(String id) {
        Module module = moduleById(id);
        if (module == null) return null;
        List<Setting> settings = SettingErrorView.withDiagnostics(module, module.getSettings());
        if ((settings == null || settings.isEmpty()) && !VisualPreviewRegistry.supports(module)) return null;
        if (settings == null) settings = List.of();
        for (Setting setting : settings) {
            if (setting == null) continue;
            setting.preflightI18n();
        }
        return new ResolvedSettings(module.name(), module.getDisplayName(), settings);
    }

    static Module moduleById(String id) {
        if (id == null || id.isBlank()) return null;
        return ModuleManager.get(id);
    }

    static boolean supportsPreview(String id) {
        return VisualPreviewRegistry.supports(moduleById(id));
    }

    static boolean openPreview(String id) {
        return VisualPreviewRegistry.open(moduleById(id));
    }

    record ResolvedSettings(String id, String title, List<Setting> settings) {
        public String getId() {
            return id;
        }
    }
}
