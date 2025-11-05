package com.cybercom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RSAMessageCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rsamsg")
                .then(argument("message", LongArgumentType.longArg())
                        .executes(ctx -> {
                            long message = LongArgumentType.getLong(ctx, "message");
                            String encodedMessage = RSAMessage.encodeMessage(message);
                            ctx.getSource().sendFeedback(() -> Text.literal("Encoded Message: " + encodedMessage), false);
                            return 1;
                        })));
    }
}
