/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui.runtime.asset;

import havoc.client.render.engine.renderer.ui.runtime.core.UiProps;

@FunctionalInterface
public interface UiAssetProvider {
    UiAssetRef resolve(UiProps props, String type, String id, float intrinsicWidth, float intrinsicHeight);
}
