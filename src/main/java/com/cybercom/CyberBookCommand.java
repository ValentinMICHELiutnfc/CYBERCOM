package com.cybercom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CyberBookCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("cyberbook")
                .then(literal("encode")
                        .then(argument("target", EntityArgumentType.player())
                                .executes(ctx -> {
                                    ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");

                                    ItemStack heldItem = sender.getMainHandStack();
                                    try {
                                        ItemStack encodedBook = CyberBook.encodeBook(heldItem, target);
                                        sender.giveItemStack(encodedBook);
                                        ctx.getSource().sendFeedback(() -> Text.literal("Livre chiffré avec la clé publique de " + target.getName().getString()), false);
                                        ctx.getSource().sendFeedback(() -> Text.literal("Livre chiffré avec la clé publique de " + target.getGameProfile().name()), false);
                                    } catch (Exception ex) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Erreur: " + ex.getMessage()), false);
                                    }
                                    return 1;
                                })))
                .then(literal("encodewithkey")
                        .then(argument("n", LongArgumentType.longArg())
                                .then(argument("e", LongArgumentType.longArg())
                                        .executes(ctx -> {
                                            ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
                                            long n = LongArgumentType.getLong(ctx, "n");
                                            long e = LongArgumentType.getLong(ctx, "e");

                                            ItemStack heldItem = sender.getMainHandStack();
                                            try {
                                                ItemStack encodedBook = CyberBook.encodeBookWithKey(heldItem, new long[]{n, e});
                                                sender.giveItemStack(encodedBook);
                                                ctx.getSource().sendFeedback(() -> Text.literal("Livre chiffré avec la clé publique fournie"), false);
                                            } catch (Exception ex) {
                                                ctx.getSource().sendFeedback(() -> Text.literal("Erreur: " + ex.getMessage()), false);
                                            }
                                            return 1;
                                        }))))
                .then(literal("decode")
                        .executes(ctx -> {
                            ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();

                            ItemStack heldItem = sender.getMainHandStack();
                            try {
                                ItemStack decodedBook = CyberBook.decodeBook(heldItem, sender);
                                sender.giveItemStack(decodedBook);
                                ctx.getSource().sendFeedback(() -> Text.literal("Livre déchiffré avec votre clé privée"), false);
                            } catch (Exception ex) {
                                ctx.getSource().sendFeedback(() -> Text.literal("Erreur: " + ex.getMessage()), false);
                            }
                            return 1;
                        }))
                .then(literal("decodewithkey")
                        .then(argument("n", LongArgumentType.longArg())
                                .then(argument("d", LongArgumentType.longArg())
                                        .executes(ctx -> {
                                            ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
                                            long n = LongArgumentType.getLong(ctx, "n");
                                            long d = LongArgumentType.getLong(ctx, "d");

                                            ItemStack heldItem = sender.getMainHandStack();
                                            try {
                                                ItemStack decodedBook = CyberBook.decodeBookWithKey(heldItem, new long[]{n, d});
                                                sender.giveItemStack(decodedBook);
                                                ctx.getSource().sendFeedback(() -> Text.literal("Livre déchiffré avec la clé privée fournie"), false);
                                            } catch (Exception ex) {
                                                ctx.getSource().sendFeedback(() -> Text.literal("Erreur: " + ex.getMessage()), false);
                                            }
                                            return 1;
                                        }))))
        );
    }
}

