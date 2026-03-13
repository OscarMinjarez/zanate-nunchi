package com.adenium.zanatenunchi.observers;

import com.adenium.zanatenunchi.ai.PersonalityGenerator;
import com.adenium.zanatenunchi.blackboard.Blackboard;
import com.adenium.zanatenunchi.blackboard.BotEvent;
import com.adenium.zanatenunchi.blackboard.BotEvent.Impact;
import com.adenium.zanatenunchi.util.NameParser;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ChatObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger("ChatObserver");

    private final Blackboard blackboard;
    private PersonalityGenerator personalityGenerator;

    public ChatObserver(Blackboard blackboard) {
        this.blackboard = blackboard;
    }

    public void setPersonalityGenerator(PersonalityGenerator generator) {
        this.personalityGenerator = generator;
    }

    public void register() {
        registerPlayerJoin();
        registerChatMessage();
        LOGGER.info("ChatObserver registrado");
    }

    private void registerPlayerJoin() {
        ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) -> {
            ServerPlayer player = handler.player;
            String uuid = player.getUUID().toString();
            String language = blackboard.getPlayerLanguage(uuid);

            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                }

                // Si el jugador ya está esperando nombre, no hacer nada
                if (blackboard.isAwaitingName(uuid)) {
                    return;
                }

                // Verificar si necesita generar personalidad para este jugador
                if (!blackboard.hasPlayerPersonality(uuid)) {
                    // Marcar como pendiente de personalidad
                    blackboard.addPendingPersonality(uuid);

                    // Bootstrap inmediato para que el onboarding no use un bot genérico.
                    if (personalityGenerator != null) {
                        personalityGenerator.ensureImmediatePlayerPersonality(uuid, language);
                    }
                }

                // Proceder con saludo sin bloquear por personalidad.
                if (!blackboard.hasPlayer(uuid)) {
                    BotEvent event = new BotEvent(
                            player.getUUID(),
                            "GREETING_NEW_PLAYER",
                            Impact.HIGH,
                            System.currentTimeMillis(),
                            true
                    );
                    blackboard.publishEvent(event);
                } else {
                    BotEvent event = new BotEvent(
                            player.getUUID(),
                            "GREETING_RETURNING_PLAYER",
                            Impact.NORMAL,
                            System.currentTimeMillis(),
                            true
                    );
                    blackboard.publishEvent(event);
                }
            });
        });
    }

    private void registerChatMessage() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            String uuid = sender.getUUID().toString();
            String content = message.signedContent().trim();

            // Si estamos esperando nombre, procesarlo SIEMPRE aunque la personalidad siga pendiente.
            if (blackboard.isAwaitingName(uuid)) {
                blackboard.removeAwaitingName(uuid);
                String parsedName = NameParser.extractName(content);
                blackboard.registerNewPlayer(uuid, parsedName);
                LOGGER.info("Nombre extraído: '{}' del mensaje: '{}'", parsedName, content);

                BotEvent event = new BotEvent(
                        sender.getUUID(),
                        "CHAT_NAME_RECEIVED:" + parsedName,
                        Impact.HIGH,
                        System.currentTimeMillis(),
                        true
                );
                blackboard.publishEvent(event);
                return;
            }

            // Esperar a que tenga personalidad antes de procesar mensajes
            if (!blackboard.hasPlayerPersonality(uuid)) {
                // Si está pendiente de personalidad, ignorar por ahora
                if (blackboard.isPendingPersonality(uuid)) {
                    return;
                }
                // Si no tiene y no está pendiente, generar
                String language = blackboard.getPlayerLanguage(uuid);
                blackboard.addPendingPersonality(uuid);
                if (personalityGenerator != null) {
                    personalityGenerator.generatePlayerPersonalityAsync(uuid, language);
                }
                return;
            }

            if (!blackboard.hasPlayer(uuid)) {
                BotEvent event = new BotEvent(
                        sender.getUUID(),
                        "GREETING_NEW_PLAYER",
                        Impact.HIGH,
                        System.currentTimeMillis(),
                        true
                );
                blackboard.publishEvent(event);
            } else {
                BotEvent event = new BotEvent(
                        sender.getUUID(),
                        "CHAT_MESSAGE:" + content,
                        Impact.HIGH,
                        System.currentTimeMillis(),
                        true
                );
                blackboard.publishEvent(event);
            }
        });
    }
}

