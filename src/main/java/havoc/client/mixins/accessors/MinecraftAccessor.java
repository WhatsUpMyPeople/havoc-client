/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins.accessors;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.FriendsService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.RemoteFriendListUpdateHandler;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.server.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Accessor("rightClickDelay")
    int havoc$getItemUseCooldown();

    @Accessor("rightClickDelay")
    void havoc$setItemUseCooldown(int itemUseCooldown);

    @Mutable
    @Accessor("user")
    void havoc$setSession(User session);

    @Mutable
    @Accessor("services")
    void havoc$setServices(Services services);

    @Mutable
    @Accessor("profileFuture")
    void havoc$setProfileFuture(CompletableFuture<ProfileResult> profileFuture);

    @Mutable
    @Accessor("userApiService")
    void havoc$setUserApiService(UserApiService userApiService);

    @Mutable
    @Accessor("userPropertiesFuture")
    void havoc$setUserPropertiesFuture(CompletableFuture<UserApiService.UserProperties> userPropertiesFuture);

    @Mutable
    @Accessor("playerSocialManager")
    void havoc$setPlayerSocialManager(PlayerSocialManager playerSocialManager);

    @Accessor("remoteFriendListUpdateHandler")
    RemoteFriendListUpdateHandler havoc$getRemoteFriendListUpdateHandler();

    @Mutable
    @Accessor("remoteFriendListUpdateHandler")
    void havoc$setRemoteFriendListUpdateHandler(RemoteFriendListUpdateHandler remoteFriendListUpdateHandler);

    @Mutable
    @Accessor("telemetryManager")
    void havoc$setTelemetryManager(ClientTelemetryManager telemetryManager);

    @Mutable
    @Accessor("profileKeyPairManager")
    void havoc$setProfileKeyPairManager(ProfileKeyPairManager profileKeyPairManager);

    @Mutable
    @Accessor("reportingContext")
    void havoc$setReportingContext(ReportingContext reportingContext);

    @Invoker("createUserApiService")
    static UserApiService havoc$createUserApiService(YggdrasilAuthenticationService service, GameConfig config) {
        throw new AssertionError();
    }
}
