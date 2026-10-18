/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.world.environment;

import net.minecraft.resources.Identifier;

/** Registered celestial-state producer selected explicitly by a dimension profile. */
public interface CelestialModel {
    Identifier id();

    CelestialState capture(EnvironmentCaptureContext context);

    default void reset() {
    }
}
