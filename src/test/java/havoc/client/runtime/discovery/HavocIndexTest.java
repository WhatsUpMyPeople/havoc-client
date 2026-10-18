/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.runtime.discovery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class HavocIndexTest {
    @Test
    void containsTopLevelAndNestedBuiltinComponents() {
        assertTrue(HavocIndex.classNames(HavocIndex.Kind.MODULE)
                .contains("havoc.client.features.module.modules.visuals.Zoom"));
        assertTrue(HavocIndex.classNames(HavocIndex.Kind.SOUND)
                .contains("havoc.client.features.gui.hud.draggable.impl.HudNotifier$NotificationSound"));
        assertTrue(HavocIndex.classNames(HavocIndex.Kind.ASSET_HOOK)
                .contains("havoc.client.util.sound.SoundSystem"));
        assertTrue(HavocIndex.classNames(HavocIndex.Kind.CONFIG)
                .contains("havoc.client.features.relations.PlayerRelations"));
    }

    @Test
    void packageFilterDoesNotLeakOtherComponentPackages() {
        assertTrue(HavocIndex.classNames(
                        HavocIndex.Kind.COMMAND,
                        "havoc.client.features.command.impl"
                ).stream()
                .allMatch(name -> name.startsWith("havoc.client.features.command.impl.")));
    }
}
