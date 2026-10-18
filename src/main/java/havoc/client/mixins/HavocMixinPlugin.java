/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import havoc.client.util.logging.DebugLog;

import java.util.List;
import java.util.Set;

public final class HavocMixinPlugin implements IMixinConfigPlugin {
    // Sodium 26.2 uses Mojang RenderPipeline for terrain on both GL and Vulkan backends.
    private static final boolean STUB_SODIUM = false;
    private static final boolean STUB_IRIS = false;
    private static final Set<String> DISABLED_SODIUM_SHADOW_GRAPH_MIXINS = Set.of();
    private static final Set<String> OPTIONAL_SODIUM_MIXINS = Set.of(
            "havoc.client.mixins.sodium.SodiumArenaAggregatorStrideMixin",
            "havoc.client.mixins.sodium.SodiumBlockRendererMixin",
            "havoc.client.mixins.sodium.SodiumBlockMetadataCaptureMixin",
            "havoc.client.mixins.sodium.SodiumBlockVisibilityMixin",
            "havoc.client.mixins.sodium.SodiumChunkMeshFormatsMixin",
            "havoc.client.mixins.sodium.SodiumChunkBuilderMeshingTaskMixin",
            "havoc.client.mixins.sodium.SodiumChunkVertexMixin",
            "havoc.client.mixins.sodium.SodiumDefaultFluidRendererMixin",
            "havoc.client.mixins.sodium.SodiumFluidMetadataCaptureMixin",
            "havoc.client.mixins.sodium.SodiumFluidMetadataMeshingTaskMixin",
            "havoc.client.mixins.sodium.SodiumRenderRegionFluidMetadataMixin",
            "havoc.client.mixins.sodium.SodiumRenderSectionSurfaceMixin",
            "havoc.client.mixins.sodium.SodiumDefaultChunkRendererMixin",
            "havoc.client.mixins.sodium.SodiumQueuedSectionStorageMixin",
            "havoc.client.mixins.sodium.SodiumRenderSectionManagerAccessor",
            "havoc.client.mixins.sodium.SodiumRenderSectionManagerOcclusionMixin",
            "havoc.client.mixins.sodium.SodiumSortedRenderListsInvoker",
            "havoc.client.mixins.sodium.SodiumShaderChunkRendererMixin",
            "havoc.client.mixins.sodium.SodiumVertexConsumerTrackerMixin",
            "havoc.client.mixins.sodium.gui.SodiumFlatButtonAccessorMixin",
            "havoc.client.mixins.sodium.gui.SodiumCenteredWidgetAccessorMixin",
            "havoc.client.mixins.sodium.gui.SodiumSearchWidgetAccessorMixin",
            "havoc.client.mixins.sodium.gui.SodiumHeaderWidgetAccessorMixin",
            "havoc.client.mixins.sodium.gui.SodiumStatefulControlAccessorMixin",
            "havoc.client.mixins.sodium.gui.SodiumTooltipAccessorMixin",
            "havoc.client.mixins.sodium.gui.SodiumPromptAccessorMixin",
            "havoc.client.mixins.sodium.gui.SodiumScrollbarAccessorMixin",
            "havoc.client.mixins.sodium.gui.SodiumPageModHeaderIconAccessorMixin",
            "havoc.client.mixins.sodium.gui.SodiumOptionModHeaderIconAccessorMixin",
            "havoc.client.mixins.sodium.gui.SodiumOptionListInvokerMixin",
            "havoc.client.mixins.sodium.gui.SodiumVideoSettingsScreenMixin"
    );
    private static final Set<String> OPTIONAL_IRIS_MIXINS = Set.of(
            "havoc.client.mixins.iris.IrisClearPassMsaaMixin",
            "havoc.client.mixins.iris.IrisCommonUniformsMixin",
            "havoc.client.mixins.iris.IrisCustomUniformsMixin",
            "havoc.client.mixins.iris.IrisCompositePassMixin",
            "havoc.client.mixins.iris.IrisCompositeRendererMixin",
            "havoc.client.mixins.iris.IrisExtendedShaderGeometryMixin",
            "havoc.client.mixins.iris.IrisGameRendererInteropMixin",
            "havoc.client.mixins.iris.IrisGlFramebufferMsaaMixin",
            "havoc.client.mixins.iris.IrisHandRendererAccessor",
            "havoc.client.mixins.iris.IrisHandRendererMixin",
            "havoc.client.mixins.iris.IrisIncludeProcessorMixin",
            "havoc.client.mixins.iris.IrisOptionMenuMixin",
            "havoc.client.mixins.iris.gui.IrisBaseOptionElementWidgetAccessorMixin",
            "havoc.client.mixins.iris.gui.IrisElementRowAccessorMixin",
            "havoc.client.mixins.iris.gui.IrisHeaderEntryAccessorMixin",
            "havoc.client.mixins.iris.gui.IrisLinkElementWidgetAccessorMixin",
            "havoc.client.mixins.iris.gui.IrisProfileElementWidgetAccessorMixin",
            "havoc.client.mixins.iris.gui.IrisShaderPackScreenMixin",
            "havoc.client.mixins.iris.gui.IrisShaderPackScreenScrollMixin",
            "havoc.client.mixins.iris.gui.IrisSliderElementWidgetInvokerMixin",
            "havoc.client.mixins.iris.gui.IrisStringElementWidgetAccessorMixin",
            "havoc.client.mixins.iris.IrisProgramUniformsMixin",
            "havoc.client.mixins.iris.IrisRenderTargetsMsaaMixin",
            "havoc.client.mixins.iris.IrisShaderPackLoadMixin",
            "havoc.client.mixins.iris.IrisRenderingPipelineFinalizeMixin",
            "havoc.client.mixins.iris.IrisShaderKeyMixin",
            "havoc.client.mixins.iris.IrisVanillaHandInteropMixin"
    );
    private static final Set<String> DISABLED_WITH_IRIS_MIXINS = Set.of(
            "havoc.client.mixins.sodium.SodiumArenaAggregatorStrideMixin",
            "havoc.client.mixins.sodium.SodiumBlockRendererMixin",
            "havoc.client.mixins.sodium.SodiumChunkMeshFormatsMixin",
            "havoc.client.mixins.sodium.SodiumChunkBuilderMeshingTaskMixin",
            "havoc.client.mixins.sodium.SodiumChunkVertexMixin",
            "havoc.client.mixins.sodium.SodiumDefaultFluidRendererMixin",
            "havoc.client.mixins.sodium.SodiumDefaultChunkRendererMixin",
            "havoc.client.mixins.sodium.SodiumShaderChunkRendererMixin"
    );
    private static final Set<String> OPTIONAL_XAERO_MINIMAP_MIXINS = Set.of(
            "havoc.client.mixins.xaero.minimap.AbstractWaypointRenderProviderMixin",
            "havoc.client.mixins.xaero.minimap.WaypointWorldRendererMixin",
            "havoc.client.mixins.xaero.minimap.RadarStateUpdaterMixin",
            "havoc.client.mixins.xaero.minimap.ClientSyncedTrackedPlayerManagerMixin",
            "havoc.client.mixins.xaero.minimap.MinimapClientWorldDataMixin"
    );
    private static final Set<String> OPTIONAL_XAERO_LIB_MIXINS = Set.of(
            "havoc.client.mixins.xaero.lib.ClientConfigManagerMixin"
    );
    private static final Set<String> OPTIONAL_XAERO_WORLDMAP_MIXINS = Set.of(
            "havoc.client.mixins.xaero.worldmap.WorldMapWaypointAccessor",
            "havoc.client.mixins.xaero.worldmap.WaypointRenderProviderMixin"
    );
    private static final Set<String> OPTIONAL_MORECULLING_MIXINS = Set.of(
            "havoc.client.mixins.moreculling.CullingUtilsMixin"
    );
    private static volatile boolean loggedSodiumDecision;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (STUB_SODIUM && OPTIONAL_SODIUM_MIXINS.contains(mixinClassName)) {
            return false;
        }
        if (STUB_IRIS && OPTIONAL_IRIS_MIXINS.contains(mixinClassName)) {
            return false;
        }
        if (DISABLED_SODIUM_SHADOW_GRAPH_MIXINS.contains(mixinClassName)) {
            return false;
        }
        if (OPTIONAL_XAERO_MINIMAP_MIXINS.contains(mixinClassName)) {
            return FabricLoader.getInstance().isModLoaded("xaerominimap");
        }
        if (OPTIONAL_XAERO_WORLDMAP_MIXINS.contains(mixinClassName)) {
            return FabricLoader.getInstance().isModLoaded("xaeroworldmap");
        }
        if (OPTIONAL_XAERO_LIB_MIXINS.contains(mixinClassName)) {
            FabricLoader loader = FabricLoader.getInstance();
            return loader.isModLoaded("xaerolib")
                    || loader.isModLoaded("xaerominimap")
                    || loader.isModLoaded("xaeroworldmap");
        }
        if (OPTIONAL_MORECULLING_MIXINS.contains(mixinClassName)) {
            return FabricLoader.getInstance().isModLoaded("moreculling");
        }
        if (OPTIONAL_IRIS_MIXINS.contains(mixinClassName)) {
            return FabricLoader.getInstance().isModLoaded("iris");
        }
        if (DISABLED_WITH_IRIS_MIXINS.contains(mixinClassName)) {
            return !FabricLoader.getInstance().isModLoaded("iris");
        }
        if (!OPTIONAL_SODIUM_MIXINS.contains(mixinClassName)) {
            return true;
        }
        boolean sodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");
        if (!loggedSodiumDecision) {
            loggedSodiumDecision = true;
            DebugLog.warn("[SodiumCompat] optional mixins %s", sodiumLoaded ? "enabled" : "disabled");
        }
        return sodiumLoaded;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        // Source-only hotfix: register the audio lifecycle hook without replacing
        // the consumer's potentially newer havoc.mixins.json resource.
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                ? List.of("SoundEngineLifecycleMixin") : List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
