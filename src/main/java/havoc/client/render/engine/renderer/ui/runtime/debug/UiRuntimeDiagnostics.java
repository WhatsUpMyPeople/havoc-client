/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui.runtime.debug;

import havoc.client.render.engine.renderer.ui.runtime.core.UiNode;

public final class UiRuntimeDiagnostics {
    private final UiPerfCounters counters = new UiPerfCounters();

    public UiPerfCounters counters() {
        return counters;
    }

    public String dump(UiNode root) {
        return UiTreeDumper.dump(root);
    }
}
