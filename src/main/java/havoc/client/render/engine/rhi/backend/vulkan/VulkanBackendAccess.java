/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rhi.backend.vulkan;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderSystem;
import havoc.client.mixininterface.IVulkanBackendInfo;
import havoc.client.mixins.accessors.GpuDeviceAccessor;
import org.jetbrains.annotations.Nullable;

public enum VulkanBackendAccess {
    ;

    public static @Nullable IVulkanBackendInfo current() {
        GpuDevice device = RenderSystem.tryGetDevice();
        if (!(device instanceof GpuDeviceAccessor accessor)) return null;
        GpuDeviceBackend backend = accessor.havoc$getBackend();
        return backend instanceof IVulkanBackendInfo vulkan ? vulkan : null;
    }
}
