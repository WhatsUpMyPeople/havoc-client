/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixininterface;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.FrameBufferCache;
import com.mojang.blaze3d.opengl.VertexArrayCache;

public interface IGlBackendInfo {
    DirectStateAccess havoc$directStateAccess();

    FrameBufferCache havoc$frameBufferCache();

    VertexArrayCache havoc$vertexArrayCache();

    /** Mojang-resolved GlDevice policy; do not re-probe ARB_direct_state_access in Havoc. */
    boolean havoc$nativeDirectStateAccess();

    /** Mojang-resolved GlDevice policy; do not re-probe KHR_debug in Havoc. */
    boolean havoc$khrDebug();

    /** Raw support flags captured from the exact GLCapabilities instance Mojang creates for GlDevice. */
    boolean havoc$computeShaders();

    boolean havoc$tessellationShaders();

    boolean havoc$geometryShaders();

    boolean havoc$shaderStorageBuffers();

    boolean havoc$imageLoadStore();

    boolean havoc$multiBind();

    boolean havoc$copyImage();

    boolean havoc$attachmentInvalidation();

    /** Driver identity captured from the active Mojang GL context; informational, not a capability re-probe. */
    String havoc$vendor();

    String havoc$renderer();
}
