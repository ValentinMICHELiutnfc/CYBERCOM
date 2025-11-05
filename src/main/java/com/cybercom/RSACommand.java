// src/main/java/com/cybercom/RSACommand.java
package com.cybercom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
                                                }))))));
    }
}
