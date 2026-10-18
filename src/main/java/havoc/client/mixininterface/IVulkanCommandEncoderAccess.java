/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */
package havoc.client.mixininterface;

import org.lwjgl.vulkan.VkCommandBuffer;

/** Access to Mojang's current Vulkan command buffer so native Havoc commands share its submission. */
public interface IVulkanCommandEncoderAccess {
    VkCommandBuffer havoc$commandBuffer();

    /** True while Mojang owns an active dynamic-rendering pass on this encoder. */
    boolean havoc$renderPassActive();

    /** Mojang submission generation; used only for submit-local transient native resources. */
    long havoc$currentSubmitIndex();

    /** Last submission Mojang has observed as complete on the GPU. */
    long havoc$completedSubmitIndex();
}
