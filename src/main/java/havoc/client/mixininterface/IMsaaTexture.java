/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixininterface;

public interface IMsaaTexture {
    void havoc$setSamples(int samples);

    int havoc$getSamples();

    boolean havoc$isMsaa();

    int havoc$getGlId();
}
