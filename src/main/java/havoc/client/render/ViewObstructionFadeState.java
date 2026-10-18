/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render;

public interface ViewObstructionFadeState {
    boolean havoc$isViewObstructionFadeActive();

    float havoc$getViewObstructionFadeAlpha();

    void havoc$setViewObstructionFadeActive(boolean active);

    void havoc$setViewObstructionFadeAlpha(float alpha);

    boolean havoc$isSeeInvisibleFadeActive();

    void havoc$setSeeInvisibleFadeActive(boolean active);
}
