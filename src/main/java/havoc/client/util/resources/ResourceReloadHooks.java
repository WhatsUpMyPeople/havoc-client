/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.util.resources;

import havoc.client.features.module.Modules;
import havoc.client.features.gui.clickgui.settings.I18nDuplicateScanner;
import havoc.client.features.gui.clickgui.settings.I18nPreflightManager;
import net.minecraft.server.packs.resources.ResourceManager;
import havoc.client.features.module.modules.visuals.NameTags;
import havoc.client.util.resources.asset.AssetAutoLoader;
import havoc.client.render.engine.prewarm.RenderPrewarmManager;
import havoc.client.render.engine.material.MaterialRegistry;
import havoc.client.render.iris.IrisRuntime;
import havoc.client.util.logging.DebugLog;
import havoc.client.util.media.MediaSessionService;

public enum ResourceReloadHooks {
    ;

    public static void onReload(ResourceManager manager) {
        if (manager == null) return;

        IrisRuntime.invalidateIntegration("resource_reload");

        boolean shaderResourcesPublished = false;
        try {
            // Static resource-backed systems are discovered through @AssetLoad.
            AssetAutoLoader.reload(manager);
            IrisRuntime.invalidateImportedGeometryResidency();
            MaterialRegistry.global().reload(manager);

            I18nDuplicateScanner.scan(manager, "resource reload");
            I18nPreflightManager.preflight("resource reload");

            // Per-instance/dynamic caches keep their explicit owners.
            NameTags tags = Modules.get(NameTags.class);
            if (tags != null) {
                tags.onResourceReload();
            }

            MediaSessionService.get().onResourceReload();

            try {
                IrisRuntime.registerHavocPipelines();
            } catch (RuntimeException t) {
                DebugLog.error("[Havoc] Iris pipeline registration failed after resource reload", t);
            }

            // Shader compilation is a discovered post-reload asset hook.
            AssetAutoLoader.postReload(manager);
            shaderResourcesPublished = true;

            try {
                RenderPrewarmManager.invalidateDeferred();
                RenderPrewarmManager.prewarm("shader reload complete");
            } catch (RuntimeException t) {
                DebugLog.error("[Havoc] Render prewarm failed after resource reload", t);
            }
        } catch (RuntimeException e) {
            DebugLog.error("Havoc resource reload failed before render resources were published", e);
            throw e;
        } finally {
            // A failed reload must not authorize custom UI/pipeline compilation.
            // Optional prewarm failures do not invalidate already published shaders.
            if (shaderResourcesPublished) {
                RenderResourceReadiness.markReady("shader reload complete");
                RenderPrewarmManager.onRenderResourcesReady();
            }
        }
    }
}
