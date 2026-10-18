/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.sound;

@SoundCatalog(namespace = "havoc", root = "sounds/misc", idPrefix = "alerts")
public enum MiscAlertSound implements SoundKey {
    @SoundAsset(value = "warning.wav", id = "warning")
    WARNING
}
