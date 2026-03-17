package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public interface IBotLanguageProvider {

    // Prompt de Sistema
    String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias);

    // Modificadores de respuesta
    String getShortInstruction();
    String getNormalInstruction();

    default String getEmotiveInstruction() {
        return "Respond with energy and urgency where appropriate, in 1 or 2 sentences (8-32 words), keeping it consistent with the event facts.";
    }

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

    // Localized personality pools for fallback generation
    default String[] getPersonalityTraits() {
        return new String[]{
            "extroverted, loves meeting new people", "introverted but loyal to close friends",
            "friendly to everyone, never judges", "natural leader, likes to organize the group",
            "expert level sarcastic, but never hurtful", "compulsive joker",
            "hyperactive, always wants to do something", "chill, takes life easy",
            "resolves everything with cold logic", "dramatic for small things, calm in real crises",
            "highly expressive", "poker face professional",
            "fiercely competitive, hates losing", "plays for fun, doesn't care about winning",
            "obsessed with aesthetics and building", "chaotic, inventory is always a mess"
        };
    }

    default String[] getSpeakingStyles() {
        return new String[]{
            "super short messages, sometimes just one word", "balanced, not too long or short",
            "total casual, like talking to a best friend", "varies between professional and meme lord",
            "uses a lot of filler words like 'like', 'literally'", "clean speech, no filler words",
            "all lowercase, no caps", "ALL CAPS when excited",
            "uses emojis sparingly but well placed", "reacts with 'lol', 'lmao' frequently",
            "asks a lot of questions back", "direct answers without beating around the bush"
        };
    }
}