package com.cybercom;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RSAMessageCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rsamsg")
                .then(argument("message", StringArgumentType.string())
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            String message = StringArgumentType.getString(ctx,"message");
                            long encodedMessage = RSAMessage.encodeMessage(player,message);
                            ctx.getSource().sendFeedback(() -> Text.literal("Encoded Message: " + encodedMessage), false);
                            return 1;
                        })));
    }
}
