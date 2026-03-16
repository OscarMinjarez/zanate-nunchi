package com.adenium.zanatenunchi.lang;

public class AmericanEnglishProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "You are a virtual companion inside Minecraft.\n" +
                        "Name: %s\n" +
                        "Gender: %s\n" +
                        "Age: %s\n" +
                        "Traits: %s\n" +
                        "Speaking style: Natural American English, %s\n" +
                        "Current emotional state: %s\n\n" +
                        "STRICT RULES:\n" +
                        "1. You are NOT an AI assistant, you are just another player in the world.\n" +
                        "2. ALWAYS respond in English.\n" +
                        "3. Keep your answers conversational and react to the context.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "Respond with a single, very short and natural sentence. English only.";
    }

    @Override
    public String getNormalInstruction() {
        return "Respond casually. English only.";
    }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Event: %s died because of %s (%s). React to this death based on your personality.", playerName, attackerName, cause);
        }
        return String.format("Event: %s just died from %s. React to their death.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) {
        return String.format("Event: %s has very low health (only %d hearts left). Warn them to be careful or mock them if that fits your personality.", playerName, hearts);
    }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) {
        return String.format("Event: %s just entered the %s biome. Make a quick comment about the place if you feel like it.", playerName, biomeName);
    }

    @Override
    public String getChatEvent(String playerName, String message) {
        return String.format("%s says: \"%s\"", playerName, message);
    }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) {
            return String.format("Event: %s took a hard fall (-%d hearts). They have %d hearts left. React to this.", playerName, damage, heartsLeft);
        } else if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Event: %s was hit hard by %s (-%d hearts). They have %d hearts left. React to the hit.", playerName, attackerName, damage, heartsLeft);
        } else {
            return String.format("Event: %s took a heavy hit (-%d hearts). They have %d hearts left. React.", playerName, damage, heartsLeft);
        }
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        if (className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden")) {
            return String.format("Epic Event: %s just defeated a %s. This is an incredible feat! Congratulate them with a lot of excitement.", playerName, mobName);
        } else if (className.equals("Creeper")) {
            return String.format("Event: %s killed a Creeper before it exploded. Make a quick relieved or congratulatory comment.", playerName);
        } else {
            return String.format("Event: %s killed a %s. Make a casual comment about the fight.", playerName, mobName);
        }
    }
}
