/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.xaero.worldmap;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import havoc.client.compat.xaero.XaeroWorldMapIntegration;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.mods.SupportXaeroMinimap;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointRenderContext;
import xaero.map.mods.gui.WaypointRenderProvider;

import java.util.ArrayDeque;
import java.util.Queue;

@Pseudo
@Mixin(WaypointRenderProvider.class)
public final class WaypointRenderProviderMixin {

    @Shadow
    @Final
    private SupportXaeroMinimap minimap;

    @Unique
    private Queue<Waypoint> havoc$pendingWaypoints;

    @Unique
    private boolean havoc$originalHadNext;

    @Inject(method = "begin*", at = @At("HEAD"))
    private void havoc$begin(ElementRenderLocation location,
                                 WaypointRenderContext context,
                                 CallbackInfo ci) {
        havoc$pendingWaypoints = new ArrayDeque<>();
        havoc$pendingWaypoints.addAll(XaeroWorldMapIntegration.waypoints(minimap));
        havoc$originalHadNext = false;
    }

    @Inject(method = "hasNext*", at = @At("RETURN"), cancellable = true)
    private void havoc$hasNext(ElementRenderLocation location,
                                   WaypointRenderContext context,
                                   CallbackInfoReturnable<Boolean> cir) {
        havoc$originalHadNext = cir.getReturnValue();
        cir.setReturnValue(havoc$originalHadNext
                || (havoc$pendingWaypoints != null && !havoc$pendingWaypoints.isEmpty()));
    }

    @Inject(method = "getNext*", at = @At("HEAD"), cancellable = true)
    private void havoc$getNext(ElementRenderLocation location,
                                   WaypointRenderContext context,
                                   CallbackInfoReturnable<Waypoint> cir) {
        if (havoc$originalHadNext) {
            havoc$originalHadNext = false;
            return;
        }
        if (havoc$pendingWaypoints != null && !havoc$pendingWaypoints.isEmpty()) {
            cir.setReturnValue(havoc$pendingWaypoints.poll());
        }
    }

    @Inject(method = "end*", at = @At("HEAD"))
    private void havoc$end(ElementRenderLocation location,
                               WaypointRenderContext context,
                               CallbackInfo ci) {
        havoc$pendingWaypoints = null;
        havoc$originalHadNext = false;
    }
}
