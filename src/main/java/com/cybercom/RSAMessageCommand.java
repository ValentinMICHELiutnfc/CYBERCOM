package com.cybercom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RSAMessageCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rsamsg")
                .then(literal("encode")
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                    String message = StringArgumentType.getString(ctx, "message");
                                    String publicKey = player.getAttached(ModDataComponents.PUBLIC_KEY);

                                    if (publicKey == null) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Erreur: Aucune clé publique"), false);
                                        return 0;
                                    }

                                    try {
                                        String encodedMessage = RSAMessage.encodeMessageWithKey(publicKey, message);
                                        ctx.getSource().sendFeedback(() -> Text.literal("Message encodé: " + encodedMessage), false);
                                    } catch (Exception ex) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Erreur: " + ex.getMessage()), false);
                                        return 0;
                                    }
                                    return 1;
                                })))
                .then(literal("decode")
                        .then(argument("encryptedMessage", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                    String encryptedMessage = StringArgumentType.getString(ctx, "encryptedMessage");
                                    String privateKey = player.getAttached(ModDataComponents.PRIVATE_KEY);

                                    if (privateKey == null) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Erreur: Aucune clé privée"), false);
                                        return 0;
                                    }

                                    try {
                                        String decodedMessage = RSAMessage.decodeMessageWithKey(privateKey, encryptedMessage);
                                        ctx.getSource().sendFeedback(() -> Text.literal("Message décodé: " + decodedMessage), false);
                                    } catch (Exception ex) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Erreur: Impossible de décoder"), false);
                                        return 0;
                                    }
                                    return 1;
                                })))
        );
    }
}
