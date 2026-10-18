/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.uniform.impl;

import havoc.client.render.engine.core.HavocRenderSystem;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import havoc.client.render.engine.rhi.uniform.HavocUniformAllocator;

public enum PostFXUniforms {
    ;
    public static final int FLOAT_COUNT = 32;
    public static final int SIZE = new Std140SizeCalculator()
            .putFloat().putFloat().putFloat().putFloat()
            .putFloat().putFloat().putFloat().putFloat()
            .putFloat().putFloat().putFloat().putFloat()
            .putFloat().putFloat().putFloat().putFloat()
            .putFloat().putFloat().putFloat().putFloat()
            .putFloat().putFloat().putFloat().putFloat()
            .putFloat().putFloat().putFloat().putFloat()
            .putFloat().putFloat().putFloat().putFloat()
            .get();

    private static final Data DATA = new Data();
    private static final String UNIFORM_NAME = "Havoc - PostFX UBO";
    private static final int EXPECTED_WRITES_PER_FRAME = 16;

    public static void update(float[] values) {
        if (values == null || values.length < FLOAT_COUNT) return;
        System.arraycopy(values, 0, DATA.values, 0, FLOAT_COUNT);
        HavocRenderSystem.uniforms().write(UNIFORM_NAME, SIZE, EXPECTED_WRITES_PER_FRAME, DATA);
    }

    public static GpuBufferSlice get() {
        return HavocRenderSystem.uniforms().current(UNIFORM_NAME);
    }

    private static final class Data implements HavocUniformAllocator.UniformWriter {
        private final float[] values = new float[FLOAT_COUNT];

        @Override
        public void write(java.nio.ByteBuffer buffer) {
            Std140Builder b = Std140Builder.intoBuffer(buffer);
            for (int i = 0; i < FLOAT_COUNT; i++) {
                b.putFloat(values[i]);
            }
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }

}
