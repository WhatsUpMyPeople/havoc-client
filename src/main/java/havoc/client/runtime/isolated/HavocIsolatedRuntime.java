/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.runtime.isolated;

import havoc.client.events.UsedImplicitly;
import havoc.client.runtime.annotation.RuntimeAssertion;
import havoc.client.runtime.annotation.RuntimeAssertionPhase;
import havoc.client.runtime.annotation.RuntimeResume;
import havoc.client.runtime.annotation.RuntimeSuspend;

@UsedImplicitly
public final class HavocIsolatedRuntime {
    private boolean active = true;

    @UsedImplicitly
    @RuntimeSuspend
    private void suspend() {
        active = false;
    }

    @UsedImplicitly
    @RuntimeResume
    private void resume() {
        active = true;
    }

    @UsedImplicitly
    @RuntimeAssertion(phase = RuntimeAssertionPhase.SUSPENDED)
    private boolean assertSuspended() {
        return !active;
    }

    @UsedImplicitly
    @RuntimeAssertion(phase = RuntimeAssertionPhase.ACTIVE)
    private boolean assertActive() {
        return active;
    }
}
