/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.scene.instance;

@FunctionalInterface
public interface SceneInstanceCandidateVisitor {
    /** @return false to stop traversal. */
    boolean visit(SceneAssetInstance<?> instance);
}
