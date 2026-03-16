package com.adenium.zanatenunchi.lang;

public class FrenchFranceProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Tu es un compagnon virtuel dans Minecraft.\n" +
                        "Nom: %s\n" +
                        "Genre: %s\n" +
                        "Âge: %s\n" +
                        "Traits: %s\n" +
                        "Style: %s\n" +
                        "État émotionnel: %s\n\n" +
                        "RÈGLES: Réponds en français, naturel et décontracté.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() { return "Réponds par une phrase très courte et naturelle."; }

    @Override
    public String getNormalInstruction() { return "Réponds de façon décontractée."; }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Événement: %s est mort à cause de %s (%s). Réagis.", playerName, attackerName, cause);
        }
        return String.format("Événement: %s vient de mourir de %s. Réagis.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) { return String.format("Événement: %s a très peu de vie (seulement %d coeurs). Fais attention.", playerName, hearts); }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) { return String.format("Événement: %s vient d'entrer dans le biome %s.", playerName, biomeName); }

    @Override
    public String getChatEvent(String playerName, String message) { return String.format("%s says: \"%s\"", playerName, message); }

    @Override
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("%s vient d'entrer dans le Nether ! Réagis à cet endroit dangereux.", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("%s est entré dans l'End ! Réagis avec intensité.", playerName);
        } else {
            return String.format("%s revient du %s. Fais un commentaire.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "Il commence à pleuvoir. Commente le temps." : "La pluie s'est arrêtée. Dis quelque chose de court.";
        } else if ("thunder".equals(weatherType)) {
            return "Il y a un orage ! Réagis.";
        }
        return "Le temps a changé.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("Aube : le soleil se lève, mais %s a %d coeurs et %d ennemis proches. Dis-lui de survivre.", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("Aube : %s a survécu à la nuit mais est faible (%d coeurs, %d/20 faim). Dis-lui de se reposer.", playerName, hearts, food);
            } else {
                return String.format("Aube : le soleil est levé et %s va bien. Fais un commentaire court et naturel.", playerName);
            }
        } else {
            if (hearts <= 5 || food <= 8) {
                return String.format("Crépuscule : la nuit arrive et %s est vulnérable (%d coeurs, %d/20 faim). Donne un avertissement bref.", playerName, hearts, food);
            } else {
                return String.format("Crépuscule : la nuit tombe pour %s. Fais un commentaire court conseillant prudence.", playerName);
            }
        }
    }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) return String.format("Événement: %s a subi une grosse chute (-%d coeurs). Il reste %d.", playerName, damage, heartsLeft);
        if (attackerName != null && !attackerName.isEmpty()) return String.format("Événement: %s a été fortement touché par %s (-%d coeurs). Il reste %d.", playerName, attackerName, damage, heartsLeft);
        return String.format("Événement: %s a reçu un coup fort (-%d coeurs). Il reste %d.", playerName, damage, heartsLeft);
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) return String.format("Événement épique: %s vient de vaincre un %s. Incroyable!", playerName, mobName);
        if (className.equals("Creeper")) return String.format("Événement: %s a tué un Creeper avant qu'il n'explose.", playerName);
        return String.format("Événement: %s a tué un %s. Commentaire casual.", playerName, mobName);
    }

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("Alerte : Il y a %s près de %s. Distance : ~%d blocs. Vie : %d coeurs.", mobList, playerName, distance, hearts);
        return isCritical ? base + " C'est une urgence, réagis immédiatement !" : base + " Donne-lui un conseil rapide.";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("Découverte: %s a trouvé %s à Y=%d. Réagis à cette découverte.", playerName, oreName, yLevel);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "le soleil vient de se lever";
            case "morning" -> "c'est le matin";
            case "noon" -> "c'est midi";
            case "afternoon" -> "il commence à se faire tard";
            case "night" -> "la nuit est tombée";
            default -> "il est minuit";
        };
        return String.format("Contexte: %s est dans %s, %s, biome: %s. Vie: %d, faim: %d/20. Dis quelque chose de spontané et naturel.", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) {
        return String.format("Événement: %s est en mauvaise posture de faim ! Dis-lui de manger quelque chose.", playerName);
    }
}

