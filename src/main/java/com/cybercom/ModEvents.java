package com.cybercom;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModEvents {
    public static void register() {
        // Initialize keys when player joins the server (first connection)
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            var publicKey = player.getAttached(ModDataComponents.PUBLIC_KEY);
            var privateKey = player.getAttached(ModDataComponents.PRIVATE_KEY);

            if (publicKey == null || privateKey == null) {
                RSAMessage.initkeys(player);
                CYBERCOM.LOGGER.info("Initialized RSA keys for player on JOIN: " + player.getName().getString());
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register(ModEvents::onPlayerRespawn);
        ServerPlayerEvents.COPY_FROM.register(ModEvents::onPlayerCopy);
    }

    private static void onPlayerRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        // Initialize keys if they don't exist (first join or after death)
        var publicKey = newPlayer.getAttached(ModDataComponents.PUBLIC_KEY);
        var privateKey = newPlayer.getAttached(ModDataComponents.PRIVATE_KEY);

        if (publicKey == null || privateKey == null) {
            RSAMessage.initkeys(newPlayer);
            CYBERCOM.LOGGER.info("Initialized RSA keys for player: " + newPlayer.getName().getString());
        }
    }

    private static void onPlayerCopy(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        if (alive) {
            // Copy keys from old player to new player (dimension change, etc.)
            var oldPublicKey = oldPlayer.getAttached(ModDataComponents.PUBLIC_KEY);
            var oldPrivateKey = oldPlayer.getAttached(ModDataComponents.PRIVATE_KEY);

            if (oldPublicKey != null && oldPrivateKey != null) {
                newPlayer.setAttached(ModDataComponents.PUBLIC_KEY, oldPublicKey);
                newPlayer.setAttached(ModDataComponents.PRIVATE_KEY, oldPrivateKey);
            } else {
                // If old player had no keys, initialize new ones
                RSAMessage.initkeys(newPlayer);
                CYBERCOM.LOGGER.info("Initialized RSA keys for player (from copy): " + newPlayer.getName().getString());
            }
        }
    }
}
