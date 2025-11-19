// src/main/java/com/cybercom/RSACommand.java
package com.cybercom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;

import static com.cybercom.CYBERCOM.LOGGER;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RSACommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rsa")
                .then(literal("generate")
                        .then(argument("inf", LongArgumentType.longArg(0))
                                .then(argument("lg", LongArgumentType.longArg(0))
                                        .executes(ctx -> {
                                            long inf = LongArgumentType.getLong(ctx, "inf");
                                            long lg = LongArgumentType.getLong(ctx, "lg");
                                            long[] cle = RSA.choixCle(inf, lg);
                                            if (cle != null) {
                                                ctx.getSource().sendFeedback(() -> Text.literal("p=" + cle[0] + " q=" + cle[1] + " e=" + cle[2]), false);
                                            } else {
                                                ctx.getSource().sendFeedback(() -> Text.literal("Échec génération"), false);
                                            }
                                            return 1;
                                        }))))
                .then(literal("clepublic")
                        .then(argument("p", LongArgumentType.longArg(0))
                                .then(argument("q",LongArgumentType.longArg(0))
                                        .then(argument("e",LongArgumentType.longArg(0))
                                                .executes(ctx -> {
                                                    long p = LongArgumentType.getLong(ctx,"p");
                                                    long q = LongArgumentType.getLong(ctx,"q");
                                                    long e = LongArgumentType.getLong(ctx, "e");
                                                    long[] clePublic = RSA.clePublique(p,q,e);
                                                    if(clePublic != null) {
                                                        ctx.getSource().sendFeedback(() -> Text.literal("n=" + clePublic[0] + "e=" + clePublic[1]), false);
                                                    } else {
                                                        ctx.getSource().sendFeedback(() -> Text.literal("Échec génération clé publique"),false);
                                                    }
                                                    return 1;
                                                })))))
                .then(literal("encode")
                        .then(argument("M", LongArgumentType.longArg())
                                .then(argument("n", LongArgumentType.longArg())
                                        .then(argument("e", LongArgumentType.longArg())
                                                .executes(ctx -> {
                                                    long M = LongArgumentType.getLong(ctx, "M");
                                                    long n = LongArgumentType.getLong(ctx, "n");
                                                    long e = LongArgumentType.getLong(ctx, "e");
                                                    long y = RSA.codageRSA(M, n, e);
                                                    if (y == -1) ctx.getSource().sendFeedback(() -> Text.literal("Erreur: condition non satisfaite"), false);
                                                    else ctx.getSource().sendFeedback(() -> Text.literal("encoded=" + y), false);
                                                    return 1;
                                                })))))
                .then(literal("decode")
                        .then(argument("Y", LongArgumentType.longArg())
                                .then(argument("n", LongArgumentType.longArg())
                                        .then(argument("d", LongArgumentType.longArg())
                                                .executes(ctx -> {
                                                    long Y = LongArgumentType.getLong(ctx, "Y");
                                                    long n = LongArgumentType.getLong(ctx, "n");
                                                    long d = LongArgumentType.getLong(ctx, "d");
                                                    long x = RSA.decodageRSA(Y, n, d);
                                                    if (x == -1) ctx.getSource().sendFeedback(() -> Text.literal("Erreur: condition non satisfaite"), false);
                                                    else ctx.getSource().sendFeedback(() -> Text.literal("decoded=" + x), false);
                                                    return 1;
                                                })))))
                .then(literal("getKeys").executes(ctx -> {
                    try {
                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                        LOGGER.info("Getting keys for player: " + player.getName().getString());

                        String PUB_KEY = player.getAttached(ModDataComponents.PUBLIC_KEY);
                        LOGGER.info("Public key retrieved: " + (PUB_KEY == null ? "NULL" : PUB_KEY));

                        if (PUB_KEY == null) {
                            LOGGER.warn("Public key is null, attempting to initialize...");
                            RSAMessage.initkeys(player);
                            PUB_KEY = player.getAttached(ModDataComponents.PUBLIC_KEY);
                            LOGGER.info("After init, public key: " + (PUB_KEY == null ? "STILL NULL" : PUB_KEY));

                            if (PUB_KEY == null) {
                                ctx.getSource().sendFeedback(() -> Text.literal("Erreur: Impossible d'initialiser les clés"), false);
                                return 0;
                            }
                        }

                        String PRIVATE_KEY = player.getAttached(ModDataComponents.PRIVATE_KEY);
                        LOGGER.info("Private key retrieved: " + (PRIVATE_KEY == null ? "NULL" : PRIVATE_KEY));

                        if (PRIVATE_KEY == null) {
                            ctx.getSource().sendFeedback(() -> Text.literal("Aucune clé privée trouvée pour ce joueur"), false);
                            return 0;
                        }

                        // Create final copies for lambda
                        final String finalPubKey = PUB_KEY;
                        final String finalPrivKey = PRIVATE_KEY;
                        ctx.getSource().sendFeedback(() -> Text.literal("PUBLIC KEY : " + finalPubKey + " | PRIVATE KEY : " + finalPrivKey), false);
                        return 1;
                    } catch (Exception ex) {
                        LOGGER.error("rsa getKeys failed", ex);
                        ctx.getSource().sendFeedback(() -> Text.literal("Erreur interne: " + ex.getMessage()), false);
                        return 0;
                    }
                }))
        );

    }
}
