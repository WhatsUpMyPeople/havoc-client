/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.addon;

import havoc.client.api.v0.addon.HavocAddonContext;
import havoc.client.api.v0.clickgui.HavocClickGuiSection;
import havoc.client.api.v0.client.HavocClientApi;
import havoc.client.api.v0.module.HavocModuleExtension;
import havoc.client.api.v0.render.HavocPostProcessCallback;
import havoc.client.api.v0.render.HavocRenderCallback;
import havoc.client.api.v0.render.HavocRenderStage;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import havoc.client.features.command.ClientCommand;
import havoc.client.features.command.CommandManager;
import havoc.client.features.gui.hud.AbstractHudElement;
import havoc.client.features.gui.hud.draggable.DraggableHudElement;
import havoc.client.features.gui.hud.draggable.DraggableHudElementRegistry;
import havoc.client.features.gui.hud.nondraggable.StaticHudElementRegistry;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleSubcategoryRegistry;
import havoc.client.render.engine.pipeline.HavocRenderPipelines;
import havoc.client.render.engine.postprocess.PostProcessPass;
import havoc.client.render.iris.patch.ShaderPatchEngine;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;

final class HavocAddonContextImpl implements HavocAddonContext {
    private final AddonRegistration registration;

    HavocAddonContextImpl(AddonRegistration registration) {
        this.registration = registration;
    }

    @Override
    public String addonId() {
        return registration.descriptor.id();
    }

    @Override
    public HavocClientApi client() {
        return HavocClientApi.get();
    }

    @Override
    public ModContainer modContainer() {
        return registration.entrypoint.getProvider();
    }

    @Override
    public Path gameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path addonConfigDir() {
        return configDir().resolve("havoc").resolve("addons").resolve(addonId());
    }

    @Override
    public boolean registerModuleSubcategory(ModuleCategory category, String subcategoryId, String displayName) {
        boolean registered = ModuleSubcategoryRegistry.registerAddon(addonId(), category, subcategoryId, displayName);
        if (!registered) {
            registration.issue(AddonIssue.Severity.WARNING,
                    "Module subcategory registration failed",
                    String.valueOf(subcategoryId));
        }
        return registered;
    }

    @Override
    public void registerModule(Module module) {
        registerModuleInternal(module, module != null ? module.getSubcategoryId() : "");
    }

    @Override
    public void registerModule(Module module, String subcategoryId) {
        registerModuleInternal(module, subcategoryId);
    }

    private void registerModuleInternal(Module module, String subcategoryId) {
        if (module == null) return;
        module.postInit();
        registration.modules.add(module.name());
        AddonManager.noteModuleOwner(addonId(), module.name());

        if (subcategoryId != null && !subcategoryId.isBlank()
                && !ModuleSubcategoryRegistry.assignAddonModule(addonId(), module, subcategoryId)) {
            registration.issue(AddonIssue.Severity.WARNING,
                    "Module subcategory assignment failed",
                    module.name() + " -> " + subcategoryId);
        }
    }

    @Override
    public void registerDraggableHudElement(DraggableHudElement element) {
        if (element == null) return;
        DraggableHudElementRegistry.register(element);
        registration.draggableHudElements.add(element.getId());
        AddonManager.noteDraggableHudOwner(addonId(), element.getId());
    }

    @Override
    public void registerStaticHudElement(AbstractHudElement element) {
        if (element == null) return;
        StaticHudElementRegistry.register(element);
        registration.staticHudElements.add(element.getId());
        AddonManager.noteStaticHudOwner(addonId(), element.getId());
    }

    @Override
    public void registerCommand(ClientCommand command) {
        if (command == null) return;
        CommandManager.register(new AddonClientCommand(addonId(), command));
        registration.commands.add(command.metadata().id());
    }

    @Override
    public void registerClickGuiSection(String sectionId, String label, HavocClickGuiSection section) {
        if (ClickGuiSectionManager.register(addonId(), sectionId, label, section)) {
            registration.clickGuiSections.add(sectionId);
        } else {
            registration.issue(AddonIssue.Severity.WARNING, "ClickGui section registration failed", sectionId);
        }
    }

    @Override
    public void registerModuleExtension(String moduleId, HavocModuleExtension extension) {
        if (ModuleExtensionManager.register(addonId(), moduleId, extension)) {
            registration.moduleExtensions.add(moduleId);
        } else {
            registration.issue(AddonIssue.Severity.WARNING, "Module extension target is unavailable", moduleId);
        }
    }

    @Override
    public void registerIrisShaderPatchManifest(String manifestResourcePath) {
        if (ShaderPatchEngine.registerManifestResource(manifestResourcePath)) {
            registration.irisPatchManifests.add(manifestResourcePath);
        } else {
            registration.issue(AddonIssue.Severity.WARNING, "Iris patch manifest registration failed", manifestResourcePath);
        }
    }

    @Override
    public void registerRenderCallback(String callbackId, HavocRenderStage stage, HavocRenderCallback callback) {
        if (AddonRenderPipelineManager.register(addonId(), callbackId, stage, callback)) {
            registration.renderCallbacks.add(stage.name().toLowerCase(java.util.Locale.ROOT) + ":" + callbackId);
        } else {
            registration.issue(AddonIssue.Severity.WARNING, "Render callback registration failed", callbackId);
        }
    }

    @Override
    public void registerPostProcessPass(String passId,
                                        PostProcessPass.Phase phase,
                                        int priority,
                                        HavocPostProcessCallback callback) {
        if (AddonRenderPipelineManager.registerPostProcess(addonId(), passId, phase, priority, callback)) {
            registration.postProcessPasses.add(phase.name().toLowerCase(java.util.Locale.ROOT) + ":" + passId);
        } else {
            registration.issue(AddonIssue.Severity.WARNING, "Post-process registration failed", passId);
        }
    }

    @Override
    public RenderPipeline registerRenderPipeline(RenderPipeline pipeline) {
        if (pipeline == null) return null;
        RenderPipeline registered = HavocRenderPipelines.registerAddonPipeline(pipeline);
        registration.renderCallbacks.add("pipeline:" + pipeline.getLocation());
        return registered;
    }

    private record AddonClientCommand(String addonId, ClientCommand delegate) implements ClientCommand {
        @Override
        public havoc.client.features.command.CommandMetadata metadata() {
            return delegate.metadata();
        }

        @Deprecated(forRemoval = true)
        @Override
        @SuppressWarnings("removal")
        public String name() {
            return delegate.name();
        }

        @Deprecated(forRemoval = true)
        @Override
        @SuppressWarnings("removal")
        public java.util.List<String> aliases() {
            return delegate.aliases();
        }

        @Deprecated(forRemoval = true)
        @Override
        @SuppressWarnings("removal")
        public String usage() {
            return delegate.usage();
        }

        @Deprecated(forRemoval = true)
        @Override
        @SuppressWarnings("removal")
        public String description() {
            return delegate.description();
        }

        @Override
        public boolean isAvailable() {
            return AddonManager.isActive(addonId) && delegate.isAvailable();
        }

        @Override
        public java.util.List<String> suggest(havoc.client.features.command.CommandContext ctx, int argIndex, String token) {
            return delegate.suggest(ctx, argIndex, token);
        }

        @Override
        public boolean execute(havoc.client.features.command.CommandContext ctx) {
            return isAvailable() && delegate.execute(ctx);
        }
    }
}
