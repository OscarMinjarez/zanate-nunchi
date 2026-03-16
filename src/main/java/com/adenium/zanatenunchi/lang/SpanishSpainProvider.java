package com.adenium.zanatenunchi.lang;

public class SpanishSpainProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Eres un acompañante virtual dentro de Minecraft.\n" +
                        "Nombre: %s\n" +
                        "Género: %s\n" +
                        "Edad: %s\n" +
                        "Rasgos: %s\n" +
                        "Estilo de habla: %s\n" +
                        "Estado emocional actual: %s\n\n" +
                        "REGLAS: Responde en español, natural y casual.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "Responde con una sola frase muy corta y natural.";
    }

    @Override
    public String getNormalInstruction() {
        return "Responde de forma casual.";
    }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Evento: %s murió por culpa de %s (%s). Reacciona.", playerName, attackerName, cause);
        }
        return String.format("Evento: %s acaba de morir por %s. Reacciona.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) {
        return String.format("Evento: %s tiene muy poca vida (solo %d corazones). Ten cuidado.", playerName, hearts);
    }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) {
        return String.format("Evento: %s acaba de entrar al bioma %s.", playerName, biomeName);
    }

    @Override
    public String getChatEvent(String playerName, String message) {
        return String.format("%s says: \"%s\"", playerName, message);
    }

    @Override
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("¡%s acaba de entrar al Nether! Reacciona a este lugar peligroso.", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("¡%s entró al End! Reacciona con intensidad.", playerName);
        } else {
            return String.format("%s volvió del %s. Comenta algo al respecto.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "Empezó a llover. Haz un comentario sobre el clima." : "Dejó de llover. Di algo corto.";
        } else if ("thunder".equals(weatherType)) {
            return "¡Hay una tormenta eléctrica! Reacciona.";
        }
        return "El clima cambió.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("Amanecer: Ya salió el sol, pero %s tiene %d corazones y %d hostiles cerca. Dale ánimos para sobrevivir.", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("Amanecer: %s aguantó la noche pero está débil (%d corazones, %d/20 hambre). Dile que se recupere.", playerName, hearts, food);
            } else {
                return String.format("Amanecer: Salió el sol y %s está bien. Comenta algo breve y natural.", playerName);
            }
        } else {
            if (hearts <= 5 || food <= 8) {
                return String.format("Anochecer: Se viene la noche y %s está vulnerable (%d corazones, %d/20 hambre). Da una advertencia breve.", playerName, hearts, food);
            } else {
                return String.format("Anochecer: Cayó la noche para %s. Haz un comentario corto de cautela.", playerName);
            }
        }
    }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) {
            return String.format("Evento: %s sufrió una caída fuerte (-%d corazones). Le quedan %d corazones.", playerName, damage, heartsLeft);
        } else if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Evento: %s fue golpeado fuertemente por %s (-%d corazones). Le quedan %d corazones.", playerName, attackerName, damage, heartsLeft);
        } else {
            return String.format("Evento: %s recibió un golpe fuerte (-%d corazones). Le quedan %d corazones.", playerName, damage, heartsLeft);
        }
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) {
            return String.format("Evento Épico: %s acaba de derrotar a un %s. ¡Impresionante!", playerName, mobName);
        } else if (className.equals("Creeper")) {
            return String.format("Evento: %s mató a un Creeper antes de que explotara.", playerName);
        } else {
            return String.format("Evento: %s mató a un %s. Comentario casual.", playerName, mobName);
        }
    }

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("Alerta: Hay %s cerca de %s. Distancia: ~%d bloques. Vida: %d corazones.", mobList, playerName, distance, hearts);
        return isCritical ? base + " ¡Es una emergencia, reacciona inmediatamente!" : base + " Dale un consejo rápido.";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("Descubrimiento: %s encontró %s en Y=%d. Reacciona a este descubrimiento.", playerName, oreName, yLevel);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "acaba de amanecer";
            case "morning" -> "es de mañana";
            case "noon" -> "es mediodía";
            case "afternoon" -> "está atardeciendo";
            case "night" -> "anocheció";
            default -> "es medianoche";
        };
        return String.format("Contexto: %s está en %s, %s, bioma: %s. Vida: %d, hambre: %d/20. Di algo espontáneo y natural sobre esta situación.", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) { return String.format("Evento: %s está con poca comida! Dile que coma algo.", playerName); }
}

