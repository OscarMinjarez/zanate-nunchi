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

    @Override
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if (dimensionName.equals("the_nether")) {
            return String.format("%s just entered the Nether! React to this dangerous place.", playerName);
        } else if (dimensionName.equals("the_end")) {
            return String.format("%s entered the End! React with intensity to this final dimension.", playerName);
        } else {
            return String.format("%s came back from the %s. Make a comment about it.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if (weatherType.equals("rain")) {
            return isStarting ? "It just started raining. Comment on the weather." : "It stopped raining. Say something short.";
        } else if (weatherType.equals("thunder")) {
            return "There's a thunderstorm happening! React to it.";
        }
        return "Weather changed.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if (timeOfDay.equals("sunrise")) {
            if (nearbyHostiles >= 2) {
                return String.format("Sunrise: The sun is coming up, but %s has %d hearts and %d hostiles nearby. Tell them to survive.", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("Sunrise: %s survived the night but is weak (%d hearts, %d/20 food). Tell them to recover.", playerName, hearts, food);
            } else {
                return String.format("Sunrise: The sun is up and %s is fine. Make a short, natural comment.", playerName);
            }
        } else { // sunset
            if (hearts <= 5 || food <= 8) {
                return String.format("Sunset: Night is falling and %s is vulnerable (%d hearts, %d/20 food). Give a brief warning.", playerName, hearts, food);
            } else {
                return String.format("Sunset: Night just fell for %s. Make a short comment advising caution.", playerName);
            }
        }
    }

    @Override
    public String getLowFoodEvent(String playerName) {
        return String.format("Event: %s is starving! React by telling them to eat something.", playerName);
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("Discovery: %s found %s at Y=%d. React to this discovery.", playerName, oreName, yLevel);
    }

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("Alert: There are %s near %s. Distance: ~%d blocks. Health: %d hearts.", mobList, playerName, distance, hearts);
        return isCritical ? base + " This is an emergency, react immediately!" : base + " Give them some quick advice.";
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch(timeKey) {
            case "sunrise" -> "the sun just came up";
            case "morning" -> "it's morning";
            case "noon" -> "it's noon";
            case "afternoon" -> "it's getting late";
            case "night" -> "night just fell";
            default -> "it's midnight";
        };
        return String.format("Context: %s is in %s, %s, biome: %s. Health: %d, food: %d/20. Say something spontaneous and natural about this situation.", playerName, dimension, timeDesc, biome, hearts, food);
    }
}