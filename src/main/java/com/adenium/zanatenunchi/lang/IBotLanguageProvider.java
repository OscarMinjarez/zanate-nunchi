package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public interface IBotLanguageProvider {

    // Prompt de Sistema
    String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias);

    // Modificadores de respuesta
    String getShortInstruction();
    String getNormalInstruction();

    // Eventos de Combate y Daño (Ya en uso)
    String getDeathEvent(String cause, String attackerName, String playerName);
    String getLowHealthEvent(String playerName, int hearts);
    String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft);
    String getMobKillEvent(String playerName, String mobName, String className);

    // Eventos de Chat (Ya en uso)
    String getChatEvent(String playerName, String message);

    // Eventos del Mundo y Entorno (Existentes + Nuevos)
    String getBiomeChangeEvent(String playerName, String biomeName);
    String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension);
    String getWeatherEvent(String weatherType, boolean isStarting);
    String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles);

    // Eventos de Estado del Jugador
    String getLowFoodEvent(String playerName);
    String getOreFoundEvent(String playerName, String oreName, int yLevel);
    String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical);
    String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food);

    // Fallbacks y Emergencias
    String getFallbackReply(BotEvent.Impact impact, String playerName);
    String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName);
    String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat);
    String getDeterministicGreeting(String playerName, boolean isNew, String traits);
}