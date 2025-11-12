package com.cybercom;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModEvents {
    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register(ModEvents::onPlayerCreate);
        ServerPlayerEvents.COPY_FROM.register(ModEvents::onPlayerCopy);
    }

    private static void onPlayerCreate(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        if (!alive) {
            RSAMessage.initkeys(newPlayer);
        }
    }

    private static void onPlayerCopy(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        if (alive) {
            var oldPublicKey = oldPlayer.get(ModDataComponents.PUBLIC_KEY);
            var oldPrivateKey = oldPlayer.get(ModDataComponents.PRIVATE_KEY);
            newPlayer.setComponent(ModDataComponents.PUBLIC_KEY, oldPublicKey);
            newPlayer.setComponent(ModDataComponents.PRIVATE_KEY, oldPrivateKey);
        }
    }
}
