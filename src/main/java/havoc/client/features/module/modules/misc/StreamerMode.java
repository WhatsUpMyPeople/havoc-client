/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.misc;

import havoc.client.config.values.BooleanValue;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import havoc.client.features.module.Modules;

/**
 * Hides every visible Havoc rendering surface while every module keeps
 * running underneath. Toggle with Right Shift (configurable).
 *
 * <p>What stays on while this module is enabled:
 * <ul>
 *   <li>Combat modules — KillAura still attacks, AutoCrystal still places, etc.</li>
 *   <li>Movement modules — Flight, Speed, Scaffold, Step all keep working.</li>
 *   <li>Player modules — AutoArmor, AutoReplenish, Blink, etc.</li>
 *   <li>Keybinds still toggle their targets.</li>
 * </ul>
 *
 * <p>What gets hidden while this module is enabled (each toggleable):
 * <ul>
 *   <li><b>HUD</b> — the draggable element registry and the static tab/bar/tooltip
 *     overlays check {@link #shouldHideHud()} at the top of their render loop and
 *     early-exit when it returns true.</li>
 *   <li><b>World visuals</b> — ESP, Chams, Tracers, HoleESP, NameTags, Trails,
 *     KillEffect, TotemFX, DamageTint, HitEffect, Freecam camera indicator,
 *     JumpCircles and other visuals-category modules check
 *     {@link #shouldHideWorldEffects()} in their render methods.</li>
 *   <li><b>Chat prefix</b> — Notifier skips the {@code [Havoc]} prefix on local
 *     chat messages so a stream chat scroll doesn't reveal the client.</li>
 *   <li><b>Notifications</b> — toast/corner notifications are suppressed.</li>
 *   <li><b>ClickGUI branding</b> — the "HAVOC" wordmark + version chip in the
 *     ClickGUI header are hidden (the module list stays visible so you can still
 *     toggle features while the GUI is open on-screen).</li>
 * </ul>
 *
 * <p>Default bind: {@code RIGHT_SHIFT}. Rebind in the ClickGUI if you've already
 * assigned Right Shift to something else (the main ClickGUI default was moved
 * off Right Shift when this module was added so the two don't collide).
 */
@ModuleInfo(
        id = "streamermode",
        displayName = "StreamerMode",
        aliases = {"StreamSafe", "RecordMode", "CleanHud"},
        category = ModuleCategory.MISC,
        subcategory = ModuleSubcategory.UTILITY,
        description = "module.streamermode.description")
public final class StreamerMode extends Module {

    // Each toggle controls one surface the module can hide. All default true,
    // so the common case "enable the module, everything Havoc-y disappears"
    // works with no extra configuration.
    private final BooleanValue hideHud =
            bool("streamerHideHud", "hide_hud", true);

    private final BooleanValue hideWorldEffects =
            bool("streamerHideWorldEffects", "hide_world_effects", true);

    private final BooleanValue hideClickGuiBranding =
            bool("streamerHideClickguiBranding", "hide_clickgui_branding", true);

    private final BooleanValue hideChatPrefix =
            bool("streamerHideChatPrefix", "hide_chat_prefix", true);

    private final BooleanValue hideNotifications =
            bool("streamerHideNotifications", "hide_notifications", true);

    {
        setDefaultBind("RIGHT_SHIFT");
    }

    // ======================================================================
    // Public static query API — called from render loops throughout the client.
    // Every getter is null-safe against the module not being registered yet
    // (during very early boot), so a render pass that fires before the module
    // registry is ready behaves as if StreamerMode is off.
    // ======================================================================

    /** Returns true if the HUD overlay (every Havoc draggable + static element) should be suppressed this frame. */
    public static boolean shouldHideHud() {
        StreamerMode m = Modules.get(StreamerMode.class);
        return m != null && m.isEnabled() && m.hideHud.get();
    }

    /** Returns true if in-world Havoc effects (ESP, Chams, Tracers, Trails, Kill/TotemFX, etc.) should be suppressed. */
    public static boolean shouldHideWorldEffects() {
        StreamerMode m = Modules.get(StreamerMode.class);
        return m != null && m.isEnabled() && m.hideWorldEffects.get();
    }

    /** Returns true if the ClickGUI header should hide the HAVOC wordmark + version chip. */
    public static boolean shouldHideClickGuiBranding() {
        StreamerMode m = Modules.get(StreamerMode.class);
        return m != null && m.isEnabled() && m.hideClickGuiBranding.get();
    }

    /** Returns true if local chat notifications (Notifier output) should skip the [Havoc] prefix. */
    public static boolean shouldHideChatPrefix() {
        StreamerMode m = Modules.get(StreamerMode.class);
        return m != null && m.isEnabled() && m.hideChatPrefix.get();
    }

    /** Returns true if toast/popup notifications should be suppressed entirely. */
    public static boolean shouldHideNotifications() {
        StreamerMode m = Modules.get(StreamerMode.class);
        return m != null && m.isEnabled() && m.hideNotifications.get();
    }

    /** Convenience: true if the module is currently active in any form. */
    public static boolean isActive() {
        StreamerMode m = Modules.get(StreamerMode.class);
        return m != null && m.isEnabled();
    }
}
