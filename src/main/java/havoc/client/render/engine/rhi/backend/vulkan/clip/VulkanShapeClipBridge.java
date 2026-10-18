/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rhi.backend.vulkan.clip;

import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.jetbrains.annotations.Nullable;
import havoc.client.render.engine.rhi.backend.vulkan.util.VulkanRenderStateBridge;

/**
 * Static entry used by Vulkan mixins without reaching through HavocRenderSystem repeatedly.
 */
public enum VulkanShapeClipBridge {
    ;
    private static VulkanStencilAttachmentManager attachments;

    public static void install(VulkanStencilAttachmentManager manager) {
        attachments = manager;
    }

    public static void uninstall(VulkanStencilAttachmentManager manager) {
        if (attachments == manager) attachments = null;
    }

    public static @Nullable GpuTextureView stencilAttachmentFor(RenderPassDescriptor descriptor) {
        if (!VulkanRenderStateBridge.currentRenderPassNeedsStencilAttachment()) return null;
        return attachments == null ? null : attachments.attachmentFor(descriptor);
    }

    public static boolean consumeStencilClear() {
        return attachments != null && attachments.consumeLastAttachmentClear();
    }
}
