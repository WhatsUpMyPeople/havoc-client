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

/** Shared PRE_HAND controls for texture-masked world refraction. */
public enum WorldMaskedPostUniforms {
    ;

    public static final int SIZE = new Std140SizeCalculator()
            .putFloat()
            .putFloat()
            .putFloat()
            .putFloat()
            .get();

    private static final String UNIFORM_NAME = "Havoc - WorldMaskedPost UBO";
    private static final Data DATA = new Data();

    public static void update(float timeSeconds, float width, float height, float globalStrength) {
        DATA.time = timeSeconds;
        DATA.invWidth = width > 0.0f ? 1.0f / width : 1.0f;
        DATA.invHeight = height > 0.0f ? 1.0f / height : 1.0f;
        DATA.globalStrength = globalStrength;
        HavocRenderSystem.uniforms().write(UNIFORM_NAME, SIZE, 4, DATA);
    }

    public static GpuBufferSlice get() {
        return HavocRenderSystem.uniforms().current(UNIFORM_NAME);
    }

    private static final class Data implements HavocUniformAllocator.UniformWriter {
        private float time;
        private float invWidth;
        private float invHeight;
        private float globalStrength;

        @Override
        public void write(java.nio.ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                    .putFloat(time)
                    .putFloat(invWidth)
                    .putFloat(invHeight)
                    .putFloat(globalStrength);
        }

        @Override
        public boolean equals(Object other) {
            return false;
        }
    }
}
