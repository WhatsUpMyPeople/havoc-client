/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.render.engine.scene.spatial;

@FunctionalInterface
public interface SceneSpatialVisitor {
    /** @return false to stop traversal early. */
    boolean visit(SceneSpatialRecord record);
}
