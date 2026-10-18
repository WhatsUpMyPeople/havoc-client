/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine;

import havoc.client.util.resources.asset.AssetAutoLoader;
import havoc.client.util.resources.asset.AssetLoad;
import havoc.client.util.resources.asset.TextureCatalog;
import havoc.client.util.logging.DebugLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

@TextureCatalog
public enum TextureStorage {
    ;
    public static final Identifier BLOOM =
            Identifier.fromNamespaceAndPath("havoc", "textures/bloom.png");
    public static final Identifier FIRE_FLY =
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/firefly.png");
    public static final Identifier DEFAULT_CIRCLE =
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/default_circle.png");
    public static final Identifier JUMP_CIRCLE =
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/jump_circle.png");
    public static final Identifier BUBBLE =
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/bubble.png");
    public static final Identifier FUNNEL_EYE =
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/funnel/funnel_eye.png");
    public static final Identifier FUNNEL_DISTORTION =
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/funnel/distortion.png");
    public static final Identifier PARTICLE_STARS =
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/star.png");
    public static final Identifier[] RANDOM_PARTICLES = new Identifier[]{
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p1.png"),
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p2.png"),
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p3.png"),
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p4.png"),
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p5.png"),
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p7.png"),
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p8.png"),
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p9.png"),
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p10.png"),
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p11.png"),
            Identifier.fromNamespaceAndPath("havoc", "textures/particles/p12.png")
    };
    public static final Identifier CAPTURE =
            Identifier.fromNamespaceAndPath("havoc", "textures/hud/elements/capture.png");

    @AssetLoad(order = 200)
    public static void preload() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        TextureManager tm = mc.getTextureManager();
        int loaded = AssetAutoLoader.preloadTextures(tm);
        DebugLog.renderThread("[Havoc][Assets] preloaded %d textures", loaded);
    }
}
