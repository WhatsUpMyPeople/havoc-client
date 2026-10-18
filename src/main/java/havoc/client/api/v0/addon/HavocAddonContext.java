/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.api.v0.addon;

import havoc.client.events.UsedImplicitly;
import havoc.client.features.command.ClientCommand;
import havoc.client.api.v0.clickgui.HavocClickGuiSection;
import havoc.client.features.gui.hud.AbstractHudElement;
import havoc.client.features.gui.hud.draggable.DraggableHudElement;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.api.v0.module.HavocModuleExtension;
import havoc.client.api.v0.render.HavocPostProcessCallback;
import havoc.client.api.v0.render.HavocRenderCallback;
import havoc.client.api.v0.render.HavocRenderStage;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import havoc.client.render.engine.postprocess.PostProcessPass;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;

public interface HavocAddonContext extends HavocAddonRuntimeContext {
    @UsedImplicitly
    ModContainer modContainer();
    @UsedImplicitly
    Path gameDir();

    Path configDir();
    @UsedImplicitly
    Path addonConfigDir();
    /**
     * Registers an addon-owned second-level module category. The id is local to this addon and
     * primary module category; Havoc namespaces it internally, so ids may be reused safely.
     */
    @UsedImplicitly
    boolean registerModuleSubcategory(ModuleCategory category, String subcategoryId, String displayName);

    @UsedImplicitly
    void registerModule(Module module);

    /** Registers a module and explicitly assigns it to an addon-owned subcategory. */
    @UsedImplicitly
    void registerModule(Module module, String subcategoryId);
    @UsedImplicitly
    void registerDraggableHudElement(DraggableHudElement element);
    @UsedImplicitly
    void registerStaticHudElement(AbstractHudElement element);
    @UsedImplicitly
    void registerCommand(ClientCommand command);
    @UsedImplicitly
    void registerClickGuiSection(String sectionId, String label, HavocClickGuiSection section);
    @UsedImplicitly
    void registerModuleExtension(String moduleId, HavocModuleExtension extension);
    @UsedImplicitly
    void registerIrisShaderPatchManifest(String manifestResourcePath);
    @UsedImplicitly
    void registerRenderCallback(String callbackId, HavocRenderStage stage, HavocRenderCallback callback);
    @UsedImplicitly
    void registerPostProcessPass(String passId, PostProcessPass.Phase phase, int priority, HavocPostProcessCallback callback);
    @UsedImplicitly
    RenderPipeline registerRenderPipeline(RenderPipeline pipeline);
}
