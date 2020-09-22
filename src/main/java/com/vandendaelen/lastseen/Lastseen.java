package com.vandendaelen.lastseen;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vandendaelen.lastseen.business.PlayerDisconnectionHandler;
import me.shedaniel.cloth.api.common.events.v1.PlayerLeaveCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;

import java.time.LocalDateTime;
import java.util.List;

public class Lastseen implements ModInitializer {
    @Override
    public void onInitialize() {
        PlayerLeaveCallback.EVENT.register(
                serverPlayerEntity -> PlayerDisconnectionHandler.getInstance().addPlayerDisconnection(serverPlayerEntity, LocalDateTime.now())
        );

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) -> {
                    dispatcher.register(
                            CommandManager.literal("lastseen")
                                    .then(CommandManager.argument("username", StringArgumentType.string())
                                            .suggests(new CommandSuggestions() {
                                                @Override
                                                public List<String> suggestions() {
                                                    return PlayerDisconnectionHandler.getInstance().getUsernameList();
                                                }
                                            })
                                            .executes(context -> {
                                                    final PlayerDisconnectionHandler handler = PlayerDisconnectionHandler.getInstance();
                                                    String username = context.getArgument("username", String.class);
                                                    String result = handler.getPlayerLastTime(context.getSource().getMinecraftServer(), username);
                                                    if (result != null){
                                                        context.getSource().sendFeedback(new LiteralText(username + " : " + result), true);
                                                    }
                                                    else {
                                                        context.getSource().sendError(new LiteralText("Error : No player found"));
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                            ));
                }
        );
    }
}
