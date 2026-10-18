/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.iris;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.include.IncludeProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import havoc.client.render.iris.patch.ShaderPatchEngine;
import havoc.client.util.logging.DebugLog;

import java.util.Map;
import java.util.Objects;

@Pseudo
@Mixin(value = IncludeProcessor.class, remap = false)
public abstract class IrisIncludeProcessorMixin {
    @Shadow
    @Final
    private Map<AbsolutePackPath, ImmutableList<String>> cache;
    @Unique
    private ShaderPatchEngine.Session havoc$patchSession;
    @Unique
    private String havoc$patchSessionPackName;

    @Unique
    private static String havoc$currentShaderPackName() {
        String loadingPackName = ShaderPatchEngine.loadingShaderPackName();
        if (loadingPackName != null && !loadingPackName.isBlank()) {
            return loadingPackName;
        }
        try {
            return Iris.getCurrentPackName();
        } catch (Throwable throwable) {
            DebugLog.warn("[IrisPatch] Unable to query current Iris shaderpack name: %s", throwable.getClass().getSimpleName());
            return null;
        }
    }

    @ModifyReturnValue(method = "getIncludedFile", at = @At("RETURN"), remap = false)
    private ImmutableList<String> havoc$compileShaderpackPatch(ImmutableList<String> lines, AbsolutePackPath path) {
        if (lines == null || path == null) {
            return lines;
        }

        String packName = havoc$currentShaderPackName();
        if (havoc$patchSession == null || !Objects.equals(packName, havoc$patchSessionPackName)) {
            havoc$patchSessionPackName = packName;
            havoc$patchSession = ShaderPatchEngine.newSession(packName);
            DebugLog.info("[IrisPatch] IncludeProcessor hook active: shaderPack='%s' active=%s firstPath=%s",
                    packName == null ? "" : packName,
                    havoc$patchSession.isActive(),
                    path.getPathString());
        }

        if (!havoc$patchSession.isActive()) {
            return lines;
        }

        IncludeProcessor processor = (IncludeProcessor) (Object) this;
        return havoc$patchSession.patch(
                path.getPathString(),
                lines,
                candidate -> havoc$loadPreflightSource(processor, candidate)
        );
    }

    @Unique
    private ImmutableList<String> havoc$loadPreflightSource(IncludeProcessor processor, String candidate) {
        try {
            return processor.getIncludedFile(AbsolutePackPath.fromAbsolutePath(candidate));
        } finally {
            // Session preflight intentionally suppresses patch application while it expands every
            // declared target. IncludeProcessor caches that expanded source before our RETURN hook.
            // Leaving those entries alive makes a cached parent permanently embed pristine nested
            // includes, so targets such as Photon raytracer.glsl are never visited during the real
            // shader compile. Preflight is observational: discard all cache entries it populated so
            // the actual compile traverses the include graph again with patch application enabled.
            cache.clear();
        }
    }
}
