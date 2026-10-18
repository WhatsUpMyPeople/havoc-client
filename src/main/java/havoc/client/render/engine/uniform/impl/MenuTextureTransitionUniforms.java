/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.uniform.impl;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.rhi.uniform.HavocUniformAllocator;

/** Transition state for time-of-day menu textures. params.x = eased blend to the current texture. */
public enum MenuTextureTransitionUniforms {
    ;

    public static final int SIZE = new Std140SizeCalculator()
            .putVec4()
            .get();

    private static final String UNIFORM_NAME = "Havoc - Menu Texture Transition UBO";
    private static final int EXPECTED_WRITES_PER_FRAME = 4;
    private static final Data DATA = new Data();

    public static void update(float blend) {
        DATA.params[0] = Math.max(0.0f, Math.min(1.0f, blend));
        DATA.params[1] = 0.0f;
        DATA.params[2] = 0.0f;
        DATA.params[3] = 0.0f;
        HavocRenderSystem.uniforms().write(
                UNIFORM_NAME,
                SIZE,
                EXPECTED_WRITES_PER_FRAME,
                DATA
        );
    }

    public static GpuBufferSlice get() {
        return HavocRenderSystem.uniforms().current(UNIFORM_NAME);
    }

    private static final class Data implements HavocUniformAllocator.UniformWriter {
        private final float[] params = new float[4];

        @Override
        public void write(java.nio.ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                    .putFloat(params[0]).putFloat(params[1]).putFloat(params[2]).putFloat(params[3]);
        }
    }
}
