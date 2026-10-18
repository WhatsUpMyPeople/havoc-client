/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.deferred;

/** Runtime predicate evaluated before optional pass resources are allocated. */
@FunctionalInterface
public interface DeferredPassCondition {
    DeferredPassCondition ALWAYS = context -> true;

    boolean test(DeferredPassContext context);
}
