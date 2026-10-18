/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.world;

/** Exact world seed captured from the ClientLevel constructor for deterministic renderer models. */
public interface ClientLevelEnvironmentSeedView {
    long havoc$environmentSeed();
}
