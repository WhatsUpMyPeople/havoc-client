/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rhi.shader;

public interface RhiComputePipeline extends AutoCloseable {
    String label();

    default ShaderResourceLayout resources() {
        return ShaderResourceLayout.EMPTY;
    }

    @Override
    void close();
}
