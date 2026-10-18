/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.mixins.accessors.ShaderLoaderDefinitionsAccessor;
import havoc.client.mixins.accessors.ShaderSourceKeyAccessor;
import havoc.client.render.engine.shader.HavocShaderSources;
import havoc.client.render.iris.IrisCompatibilityGuards;
import havoc.client.util.logging.DebugLog;
import havoc.client.util.resources.RenderResourceReadiness;
import havoc.client.util.resources.ResourceReloadHooks;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(ShaderManager.class)
public abstract class ShaderLoaderMixin {
    @Unique
    private static final Identifier HAVOC_ENTITY_VERTEX = Identifier.fromNamespaceAndPath("havoc", "shaders/core/entity_dither.vsh");
    @Unique
    private static final Identifier HAVOC_ENTITY_FRAGMENT = Identifier.fromNamespaceAndPath("havoc", "shaders/core/entity_dither.fsh");
    @Unique
    private static final Identifier VANILLA_ENTITY_ID = Identifier.withDefaultNamespace("core/entity");
    @Unique
    private static boolean havoc$loggedEntityOverride;
    @Unique
    private static boolean havoc$loggedExtendedShaderSources;
    @Unique
    private static Constructor<?> havoc$shaderSourceKeyConstructor;

    @Unique
    private static boolean havoc$addExtendedShaderSources(ResourceManager resourceManager, Map<Object, String> shaderSources) {
        Map<Identifier, Resource> resources = resourceManager.listResources("shaders", HavocShaderSources::isHavocExtendedSource);
        if (resources.isEmpty()) {
            return false;
        }

        int added = 0;
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            ShaderType type = HavocShaderSources.typeByLocation(resourceId);
            if (type == null) continue;

            String source = HavocShaderSources.load(resourceManager, resourceId, entry.getValue());
            shaderSources.put(havoc$newShaderSourceKey(resourceId, type), source);
            added++;

            Identifier canonicalId = HavocShaderSources.canonicalShaderId(resourceId);
            if (!canonicalId.equals(resourceId)) {
                shaderSources.put(havoc$newShaderSourceKey(canonicalId, type), source);
                added++;
            }
        }

        if (added > 0 && !havoc$loggedExtendedShaderSources) {
            havoc$loggedExtendedShaderSources = true;
            DebugLog.renderThread("[ShaderSource] registered Havoc .vert/.frag sources in ShaderManager: " + added + " keys");
        }
        return added > 0;
    }

    @Unique
    private static boolean havoc$allowVanillaEntityShaderOverride() {
        if (IrisCompatibilityGuards.suppressHavocTerrainShaderOverrides()) {
            return false;
        }

        return true;
    }

    @Unique
    private static boolean havoc$applyEntityOverride(ResourceManager resourceManager, Map<Object, String> shaderSources) {
        String entityFragmentSrc = havoc$loadShaderSource(resourceManager, HAVOC_ENTITY_FRAGMENT);
        String entityVertexSrc = havoc$loadShaderSource(resourceManager, HAVOC_ENTITY_VERTEX);
        if (((entityFragmentSrc == null || entityFragmentSrc.isEmpty()) || (entityVertexSrc == null || entityVertexSrc.isEmpty())) && !havoc$loggedEntityOverride) {
            havoc$loggedEntityOverride = true;
            DebugLog.warn("[ShaderOverride] entity fade override skipped: havoc entity_dither shader missing");
        }

        if ((entityFragmentSrc == null || entityFragmentSrc.isEmpty()) || (entityVertexSrc == null || entityVertexSrc.isEmpty())) {
            return false;
        }

        boolean entityFragmentChanged = false;
        boolean entityVertexChanged = false;

        for (Map.Entry<Object, String> entry : shaderSources.entrySet()) {
            Object key = entry.getKey();
            if (entityVertexSrc != null && !entityVertexSrc.isEmpty() && havoc$isEntityVertexKey(key)) {
                entry.setValue(entityVertexSrc);
                entityVertexChanged = true;
                continue;
            }
            if (entityFragmentSrc != null && !entityFragmentSrc.isEmpty() && havoc$isEntityFragmentKey(key)) {
                entry.setValue(entityFragmentSrc);
                entityFragmentChanged = true;
            }
        }

        if ((!entityFragmentChanged || !entityVertexChanged) && !havoc$loggedEntityOverride) {
            havoc$loggedEntityOverride = true;
            DebugLog.warn("[ShaderOverride] entity fade override skipped: entity shader key not found");
        }

        if (entityFragmentChanged && entityVertexChanged && !havoc$loggedEntityOverride) {
            havoc$loggedEntityOverride = true;
            DebugLog.renderThread("[ShaderOverride] override applied: core/entity -> havoc entity_dither");
        }

        return entityFragmentChanged || entityVertexChanged;
    }

    @Unique
    private static boolean havoc$isEntityFragmentKey(Object key) {
        if (!(key instanceof ShaderSourceKeyAccessor accessor)) return false;
        return VANILLA_ENTITY_ID.equals(accessor.havoc$getId()) && accessor.havoc$getType() == ShaderType.FRAGMENT;
    }

    @Unique
    private static boolean havoc$isEntityVertexKey(Object key) {
        if (!(key instanceof ShaderSourceKeyAccessor accessor)) return false;
        return VANILLA_ENTITY_ID.equals(accessor.havoc$getId()) && accessor.havoc$getType() == ShaderType.VERTEX;
    }

    @Unique
    private static String havoc$loadShaderSource(ResourceManager resourceManager, Identifier id) {
        try {
            return HavocShaderSources.load(resourceManager, id);
        } catch (Exception e) {
            DebugLog.error("[ShaderOverride] read shader override failed: " + id, e);
            return null;
        }
    }

    @Unique
    private static Object havoc$newShaderSourceKey(Identifier id, ShaderType type) {
        try {
            if (havoc$shaderSourceKeyConstructor == null) {
                Class<?> keyClass = Class.forName("net.minecraft.client.renderer.ShaderManager$ShaderSourceKey");
                Constructor<?> constructor = keyClass.getDeclaredConstructor(Identifier.class, ShaderType.class);
                constructor.setAccessible(true);
                havoc$shaderSourceKeyConstructor = constructor;
            }
            return havoc$shaderSourceKeyConstructor.newInstance(id, type);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create ShaderManager shader source key for " + type + " " + id, e);
        }
    }

    @Inject(method = "apply", at = @At("HEAD"))
    private void havoc$beforeShaderReload(ShaderManager.Configs definitions,
                                              ResourceManager resourceManager,
                                              ProfilerFiller profiler,
                                              CallbackInfo ci) {
        RenderResourceReadiness.markReloading("shader reload");
    }

    @Inject(method = "apply", at = @At("TAIL"))
    private void havoc$afterShaderReload(ShaderManager.Configs definitions,
                                             ResourceManager resourceManager,
                                             ProfilerFiller profiler,
                                             CallbackInfo ci) {
        ResourceReloadHooks.onReload(resourceManager);
    }

    @ModifyReturnValue(method = "prepare", at = @At("RETURN"))
    private ShaderManager.Configs havoc$extendShaderSources(ShaderManager.Configs definitions,
                                                                ResourceManager resourceManager,
                                                                ProfilerFiller profiler) {
        ShaderLoaderDefinitionsAccessor accessor = (ShaderLoaderDefinitionsAccessor) (Object) definitions;
        Map<?, String> shaderSources = accessor.havoc$getShaderSources();
        Map<Object, String> replaced = new LinkedHashMap<>(shaderSources.size() + 64);
        replaced.putAll(shaderSources);

        boolean changed = havoc$addExtendedShaderSources(resourceManager, replaced);
        if (havoc$allowVanillaEntityShaderOverride()) {
            changed |= havoc$applyEntityOverride(resourceManager, replaced);
        }

        if (!changed) {
            return definitions;
        }

        Map<Identifier, PostChainConfig> postChains = accessor.havoc$getPostChains();
        @SuppressWarnings({"rawtypes", "unchecked"})
        ShaderManager.Configs updated = new ShaderManager.Configs((Map) replaced, postChains);
        return updated;
    }
}
