/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rhi.backend.gl;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;
import havoc.client.mixininterface.IGlBackendInfo;
import havoc.client.mixins.accessors.GpuDeviceAccessor;

public enum GlBackendAccess {
    ;

    public static @Nullable IGlBackendInfo current() {
        GpuDevice device = RenderSystem.tryGetDevice();
        if (!(device instanceof GpuDeviceAccessor accessor)) {
            return null;
        }
        GpuDeviceBackend backend = accessor.havoc$getBackend();
        return backend instanceof IGlBackendInfo glBackend ? glBackend : null;
    }
}
