/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.deferred;

/** Tri-state smoke-test override. DEFAULT preserves normal renderer/profile policy. */
public enum DeferredFeatureOverride {
    DEFAULT,
    FORCE_ENABLED,
    FORCE_DISABLED
}
