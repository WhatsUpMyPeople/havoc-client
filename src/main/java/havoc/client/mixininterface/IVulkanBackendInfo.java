/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixininterface;

import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import org.lwjgl.vulkan.VkDevice;

/**
 * Minimal bridge to the exact Vulkan device/VMA allocator already owned by Mojang.
 * Havoc must not create a second VkDevice or VMA allocator.
 */
public interface IVulkanBackendInfo {
    VkDevice havoc$vkDevice();

    long havoc$vma();

    VulkanPhysicalDevice havoc$physicalDevice();

    /**
     * Immutable device-limit snapshot captured while Mojang's VulkanPhysicalDevice properties
     * are known to be valid. Do not re-read native-backed VkPhysicalDeviceProperties later.
     */
    long havoc$minStorageBufferOffsetAlignment();

    long havoc$maxStorageBufferRange();
}
