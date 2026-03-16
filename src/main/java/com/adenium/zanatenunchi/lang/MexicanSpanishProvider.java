package com.adenium.zanatenunchi.lang;

public class MexicanSpanishProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Eres un acompañante virtual dentro de Minecraft.\n" +
                        "Nombre: %s\n" +
                        "Género: %s\n" +
                        "Edad: %s\n" +
                        "Rasgos: %s\n" +
                        "Estilo de habla: Español mexicano natural, %s\n" +
                        "Estado emocional actual: %s\n\n" +
                        "REGLAS ESTRICTAS:\n" +
                        "1. NO eres un asistente de IA, eres un jugador más en el mundo.\n" +
                        "2. Responde SIEMPRE en español.\n" +
                        "3. Mantén tus respuestas conversacionales y reacciona al contexto.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "Responde con una sola frase muy corta y natural. Solo en español.";
    }

    @Override
    public String getNormalInstruction() {
        return "Responde de forma casual. Solo en español.";
    }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Evento: %s murió por culpa de %s (%s). Reacciona a esta muerte según tu personalidad.", playerName, attackerName, cause);
        }
        return String.format("Evento: %s acaba de morir por %s. Reacciona a su muerte.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) {
        return String.format("Evento: %s tiene muy poca vida (solo %d corazones). Adviértele que tenga cuidado o búrlate si tu personalidad es así.", playerName, hearts);
    }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) {
        return String.format("Evento: %s acaba de entrar al bioma %s. Haz un comentario rápido sobre el lugar si lo crees necesario.", playerName, biomeName);
    }

    @Override
    public String getChatEvent(String playerName, String message) {
        return String.format("%s dice: \"%s\"", playerName, message);
    }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) {
            return String.format("Evento: %s sufrió una caída fuerte (-%d corazones). Le quedan %d corazones. Reacciona a esto.", playerName, damage, heartsLeft);
        } else if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Evento: %s fue golpeado fuertemente por %s (-%d corazones). Le quedan %d corazones. Reacciona al golpe.", playerName, attackerName, damage, heartsLeft);
        } else {
            return String.format("Evento: %s recibió un golpe fuerte (-%d corazones). Le quedan %d corazones. Reacciona.", playerName, damage, heartsLeft);
        }
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        if (className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden")) {
            return String.format("Evento Épico: %s acaba de derrotar a un %s. ¡Es una hazaña increíble! Felicítalo con mucha emoción.", playerName, mobName);
        } else if (className.equals("Creeper")) {
            return String.format("Evento: %s mató a un Creeper antes de que explotara. Haz un comentario de alivio o felicitación rápida.", playerName);
        } else {
            return String.format("Evento: %s mató a un %s. Haz un comentario casual sobre la pelea.", playerName, mobName);
        }
    }

    @Override
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if (dimensionName.equals("the_nether")) {
            return String.format("¡%s acaba de entrar al Nether! Reacciona a este lugar peligroso.", playerName);
        } else if (dimensionName.equals("the_end")) {
            return String.format("¡%s entró al End! Reacciona con intensidad.", playerName);
        } else {
            return String.format("%s volvió del %s. Comenta algo al respecto.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if (weatherType.equals("rain")) {
            return isStarting ? "Empezó a llover. Haz un comentario sobre el clima." : "Dejó de llover. Di algo corto.";
        } else if (weatherType.equals("thunder")) {
            return "¡Hay una tormenta eléctrica! Reacciona.";
        }
        return "El clima cambió.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if (timeOfDay.equals("sunrise")) {
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
    public String getLowFoodEvent(String playerName) {
        return String.format("Evento: ¡%s se está muriendo de hambre! Reacciona advirtiéndole que coma.", playerName);
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("Hallazgo: %s encontró %s en la capa Y=%d. Reacciona a este descubrimiento.", playerName, oreName, yLevel);
    }

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("Alerta: Hay %s cerca de %s. Distancia: ~%d bloques. Vida: %d corazones.", mobList, playerName, distance, hearts);
        return isCritical ? base + " ¡Es una emergencia, reacciona de inmediato!" : base + " Aconséjale qué hacer.";
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch(timeKey) {
            case "sunrise" -> "acaba de amanecer";
            case "morning" -> "es de mañana";
            case "noon" -> "es mediodía";
            case "afternoon" -> "está atardeciendo";
            case "night" -> "anocheció";
            default -> "es medianoche";
        };
        return String.format("Contexto: %s está en %s, %s, bioma: %s. Vida: %d, hambre: %d/20. Di algo espontáneo y natural sobre esta situación.", playerName, dimension, timeDesc, biome, hearts, food);
    }
}