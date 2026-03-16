package com.adenium.zanatenunchi.lang;


public interface IBotLanguageProvider {

    // Prompt de Sistema
    String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias);

    // Modificadores de respuesta
    String getShortInstruction();
    String getNormalInstruction();

    // Eventos del mundo (Reemplazan los textos quemados en los Observers)
    String getDeathEvent(String cause, String attackerName, String playerName);
    String getLowHealthEvent(String playerName, int hearts);
    String getBiomeChangeEvent(String playerName, String biomeName);
    String getChatEvent(String playerName, String message);

    String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft);
    String getMobKillEvent(String playerName, String mobName, String className);
}