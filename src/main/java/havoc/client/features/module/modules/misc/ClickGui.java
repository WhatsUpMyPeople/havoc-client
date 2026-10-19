/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.misc;

import havoc.client.config.DisableSettingI18n;
import havoc.client.config.values.BooleanMapValue;
import havoc.client.config.values.NumberValue;
import havoc.client.features.gui.clickgui.ClickGuiEditorScreen;
import havoc.client.features.gui.clickgui.ClickGuiPickerScreen;
import havoc.client.features.gui.clickgui.ClickGuiRenderer;
import havoc.client.features.gui.clickgui.ClickGuiScreen;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import havoc.client.util.screen.ClientScreen;
import havoc.client.features.gui.clickgui.sound.GuiSound;
import net.minecraft.client.Minecraft;

import java.util.LinkedHashMap;
import java.util.Map;

@ModuleInfo(id = "clickgui", displayName = "ClickGUI", aliases = {"menu", "settings"}, category = ModuleCategory.MISC, subcategory = ModuleSubcategory.CLIENT,
        description = "module.clickgui.description")
public class ClickGui extends Module {
    private static boolean suppressScreenClose = false;

    @DisableSettingI18n(name = false, options = true)
    private final BooleanMapValue guiSoundToggles = group("gui_sounds", defaultGuiSounds());
    private final NumberValue<Double> guiSoundVolume = num("gui_sound_volume", 0.75, 0.0, 2.0);

    {
        // Default moved off RIGHT_SHIFT to make room for the StreamerMode toggle,
        // which is far more useful on the dominant idle-hand key. Rebind the
        // ClickGUI in-game if you prefer the old key.
        setDefaultBind("RIGHT_CONTROL");
    }

    public static boolean isSuppressScreenClose() {
        return suppressScreenClose;
    }

    public static void setSuppressScreenClose(boolean suppress) {
        suppressScreenClose = suppress;
    }

    private static Map<String, Boolean> defaultGuiSounds() {
        Map<String, Boolean> out = new LinkedHashMap<>();
        out.put("button", true);
        out.put("buttonyes", true);
        out.put("boolean_on", true);
        out.put("boolean_off", true);
        out.put("guibinding", true);
        out.put("guibindingnull", true);
        out.put("guibindingstart", true);
        out.put("guibindreset", true);
        out.put("guichangemode", true);
        out.put("gui_open", true);
        out.put("gui_close", true);
        out.put("guicoloropen", true);
        out.put("guicolorselect", true);
        out.put("guihover", true);
        out.put("guimodulehover", true);
        out.put("moduleopen", true);
        out.put("moduleclose", true);
        out.put("moduleonopen", true);
        out.put("moduleonclose", true);
        out.put("guiscroll", true);
        out.put("guislidermove", true);
        return out;
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        boolean clickGuiScreen = mc != null && (ClientScreen.current() instanceof ClickGuiScreen
                || ClientScreen.current() instanceof ClickGuiPickerScreen
                || ClientScreen.current() instanceof ClickGuiEditorScreen);

        if (!suppressScreenClose && clickGuiScreen) {
            GuiSound.CLOSE.feedback();
            ClickGuiRenderer.beginCloseAnimation();
            return;
        }

        ClickGuiRenderer.finishCloseImmediately();
        if (mc != null && clickGuiScreen && !suppressScreenClose) {
            ClientScreen.show(mc, null);
        }
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || (ClientScreen.current() != null && !ClickGuiRenderer.isClosingForExit())) {
            setEnabled(false);
            return;
        }
        ClickGuiRenderer.beginOpenAnimation();
        ClickGuiRenderer.openClickGuiScreen();
    }

    public boolean isGuiSoundEnabled(String key) {
        return guiSoundToggles.get(key);
    }

    public float getGuiSoundVolume() {
        return guiSoundVolume.get().floatValue();
    }
}
