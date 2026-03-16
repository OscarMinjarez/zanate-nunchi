package com.adenium.zanatenunchi.lang;

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
    String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension); // NUEVO
    String getWeatherEvent(String weatherType, boolean isStarting); // NUEVO
    String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles); // NUEVO
}