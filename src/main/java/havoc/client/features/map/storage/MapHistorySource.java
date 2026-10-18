/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.features.map.storage;

/** Stable on-disk source ids. Never reorder existing values. */
public enum MapHistorySource {
    LOCAL_ENTITY_EXACT,
    LOCATOR_EXACT,
    MAPLINK_EXACT,
    HEURISTIC_TRIANGULATED,
    DUPLEX_TRIANGULATED,
    LOCATOR_BEARING,
    LOCATOR_APPROXIMATE
}
