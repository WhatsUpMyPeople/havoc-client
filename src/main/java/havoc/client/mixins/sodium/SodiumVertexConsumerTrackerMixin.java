/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.sodium;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import havoc.client.util.logging.DebugLog;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.vertex.VertexConsumerTracker", remap = false)
public abstract class SodiumVertexConsumerTrackerMixin {
    @Redirect(
            method = "logBadConsumer",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V",
                    remap = false
            ),
            remap = false
    )
    private static void havoc$redirectBadConsumerWarn(Logger logger, String message, Object arg) {
        DebugLog.warn("[Sodium] %s", havoc$formatSlf4jSingleArg(message, arg));
    }

    @Unique
    private static String havoc$formatSlf4jSingleArg(String message, Object arg) {
        String safeMessage = String.valueOf(message);
        int placeholder = safeMessage.indexOf("{}");
        if (placeholder < 0) {
            return safeMessage + " " + arg;
        }
        return safeMessage.substring(0, placeholder)
                + arg
                + safeMessage.substring(placeholder + 2);
    }
}
