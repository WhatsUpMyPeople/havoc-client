/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.sodium;

import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.sodium.SodiumWorldVisibilityView;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Non-invasive Sodium visibility observer used by renderer services. */
@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class DevDeferredSodiumWorldRendererMixin implements SodiumWorldVisibilityView {
    @Shadow(remap = false)
    public abstract boolean isBoxVisible(double minX, double minY, double minZ,
                                         double maxX, double maxY, double maxZ);

    @Override
    public boolean havoc$isBoxVisible(AABB box) {
        return isBoxVisible(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    @Inject(method = "setupTerrain(Lnet/minecraft/client/Camera;Lnet/caffeinemc/mods/sodium/client/render/viewport/Viewport;Lnet/caffeinemc/mods/sodium/client/util/FogParameters;ZZLorg/joml/Matrix4f;)V", at = @At("HEAD"), remap = false)
    private void havoc$primaryVisibilityPending(Camera camera, Viewport viewport, FogParameters fog,
                                                    boolean useOcclusionCulling, boolean updateChunksImmediately,
                                                    org.joml.Matrix4f projection, CallbackInfo ci) {
        HavocRenderSystem.sodium().markPrimaryVisibilityPending();
    }

    @Inject(method = "setupTerrain(Lnet/minecraft/client/Camera;Lnet/caffeinemc/mods/sodium/client/render/viewport/Viewport;Lnet/caffeinemc/mods/sodium/client/util/FogParameters;ZZLorg/joml/Matrix4f;)V", at = @At("RETURN"), remap = false)
    private void havoc$primaryVisibilityReady(Camera camera, Viewport viewport, FogParameters fog,
                                                  boolean useOcclusionCulling, boolean updateChunksImmediately,
                                                  org.joml.Matrix4f projection, CallbackInfo ci) {
        HavocRenderSystem.sodium().markPrimaryVisibilityReady();
    }
}
