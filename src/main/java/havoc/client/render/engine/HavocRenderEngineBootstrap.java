/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine;

import havoc.client.addon.ClickGuiSectionManager;
import havoc.client.render.engine.pipeline.HavocRenderPipelines;
import havoc.client.util.resources.asset.AssetAutoLoader;
import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.iris.IrisRuntime;
import net.minecraft.client.Minecraft;

public enum HavocRenderEngineBootstrap {
    ;

    public static void init() {
        // Read the generated ClickGUI component index alongside the rest of client bootstrap.
        ClickGuiSectionManager.prepareDiscovery();
        HavocRenderPipelines.initialize();
        // RHI backend selection must be lazy: onInitializeClient can run before RenderSystem has
        // created the final GpuDevice, which would permanently select the wrong backend on Vulkan.
        Renderer2D.init();
        IrisRuntime.registerHavocPipelines();
        Minecraft mc = Minecraft.getInstance();
        AssetAutoLoader.initialize(mc != null ? mc.getResourceManager() : null);
    }

}
