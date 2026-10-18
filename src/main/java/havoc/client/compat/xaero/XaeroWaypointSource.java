/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.compat.xaero;

import java.util.List;

/** A Havoc subsystem that can expose immutable waypoints to Xaero adapters. */
public interface XaeroWaypointSource {
    /** Stable namespace used to distinguish this source from every other subsystem. */
    String id();

    List<XaeroWaypointSnapshot> snapshots();
}
