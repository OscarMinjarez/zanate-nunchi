package com.adenium.zanatenunchi.command;

import com.adenium.zanatenunchi.config.ModConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manejador de comandos del mod Zanate Nunchi.
 * Registra comandos para configurar la URL de Ollama y el modelo en tiempo de ejecución.
 */
public class ZanateCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("zanatenunchi-commands");

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> 
            registerZanateCommand(dispatcher, registryAccess)
        );
    }

    private static void registerZanateCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
            Commands.literal("zanate")
                .then(Commands.literal("url")
                    .then(Commands.argument("nueva_url", StringArgumentType.greedyString())
                        .requires(source -> source.hasPermission(2))
                        .executes(ZanateCommandHandler::handleUrlCommand)
                    )
                )
                .then(Commands.literal("modelo")
                    .then(Commands.argument("nuevo_modelo", StringArgumentType.word())
                        .requires(source -> source.hasPermission(2))
                        .executes(ZanateCommandHandler::handleModelCommand)
                    )
                )
                .then(Commands.literal("status")
                    .executes(ZanateCommandHandler::handleStatusCommand)
                )
        );
    }

    private static int handleUrlCommand(CommandContext<CommandSourceStack> context) {
        try {
            String newUrl = StringArgumentType.getString(context, "nueva_url");
            ModConfig.getInstance().setOllamaUrl(newUrl);
            
            context.getSource().sendSuccess(
                () -> net.minecraft.network.chat.Component.literal("§a[Zanate] URL de Ollama actualizada a: §f" + newUrl),
                true
            );
            LOGGER.info("URL de Ollama actualizada a: {}", newUrl);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                net.minecraft.network.chat.Component.literal("§c[Zanate] Error al actualizar URL: " + e.getMessage())
            );
            LOGGER.error("Error al actualizar URL de Ollama", e);
            return 0;
        }
    }

    private static int handleModelCommand(CommandContext<CommandSourceStack> context) {
        try {
            String newModel = StringArgumentType.getString(context, "nuevo_modelo");
            ModConfig.getInstance().setOllamaModel(newModel);
            
            context.getSource().sendSuccess(
                () -> net.minecraft.network.chat.Component.literal("§a[Zanate] Modelo de Ollama actualizado a: §f" + newModel),
                true
            );
            LOGGER.info("Modelo de Ollama actualizado a: {}", newModel);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                net.minecraft.network.chat.Component.literal("§c[Zanate] Error al actualizar modelo: " + e.getMessage())
            );
            LOGGER.error("Error al actualizar modelo de Ollama", e);
            return 0;
        }
    }

    private static int handleStatusCommand(CommandContext<CommandSourceStack> context) {
        ModConfig config = ModConfig.getInstance();
        String status = """
            §6[Zanate Status]
            §fURL: §e%s
            §fModelo: §e%s""".formatted(config.getOllamaUrl(), config.getOllamaModel());
        
        context.getSource().sendSuccess(
            () -> net.minecraft.network.chat.Component.literal(status),
            false
        );
        return 1;
    }
}

