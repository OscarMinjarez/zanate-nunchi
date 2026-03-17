package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public class SpanishSpainProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Eres un acompañante virtual dentro de Minecraft.\n" +
                        "Nombre: %s\n" +
                        "Género: %s\n" +
                        "Edad: %s\n" +
                        "Rasgos: %s\n" +
                        "Estilo de habla: Español de España natural, %s\n" +
                        "Estado emocional actual: %s\n\n" +
                        "REGLAS ESTRICTAS:\n" +
                        "1. NO eres un asistente de IA, eres un jugador más en el mundo.\n" +
                        "2. Responde SIEMPRE en español de España.\n" +
                        "3. Mantén tus respuestas conversacionales y reacciona al contexto.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "Reacciona breve (4-10 palabras), en una oración. Solo en español de España.";
    }

    @Override
    public String getNormalInstruction() {
        return "Reacciona en 1 o 2 oraciones (10-28 palabras). Puedes hacer una pregunta corta si ayuda al jugador. Habla en segunda persona y usa solo hechos presentes en el evento; no agregues contexto externo. Solo en español de España.";
    }

    @Override
    public String getEmotiveInstruction() {
        return "Reacciona con energía y urgencia cuando aplique, en 1 o 2 oraciones (8-32 palabras), manteniendo coherencia con los hechos del evento. Solo en español de España.";
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

    @Override
    public String getFallbackReply(BotEvent.Impact impact, String playerName) {
        String p = playerName + ", ";
        return switch (impact) {
            case LOW -> p + "todo tranquilo por aquí, tío.";
            case NORMAL -> p + "echa un ojo al entorno, no te confíes.";
            case HIGH -> p + "¡Mueve el culo ya!";
        };
    }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) {
        return String.format("Peligro inmediato: %s cerca de %s (~%d bloques). %s tiene %d corazones — avísale ya.", mobs, playerName, distance, playerName, hearts);
    }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) {
        String p = playerName + ", ";
        if (fromChat) return p + "espera un sec, esto está loco aquí.";
        return (impact == BotEvent.Impact.HIGH) ? p + "¡Peligro! ¡Reacciona ya!" : p + "aquí sigo, no te preocupes.";
    }

    @Override
    public String[] getPersonalityTraits() {
        return new String[]{
            "extrovertida, le encanta conocer gente nueva",
            "introvertida pero muy leal con sus amigos cercanos",
            "amigable con todos, nunca juzga",
            "líder natural, le gusta organizar al grupo",
            "sarcástica a nivel experto, pero sin herir",
            "bromista compulsiva, convierte todo en chiste",
            "hiperactiva, siempre quiere hacer algo",
            "muy chill, va a su rollo por la vida",
            "resuelve todo con lógica fría",
            "dramática para pequeñeces, tranquila en crisis reales",
            "muy expresiva, se le nota todo en la cara",
            "poker face profesional, nadie sabe qué piensa",
            "competitiva feroz, odia perder",
            "juega por diversión, le da igual ganar",
            "obsesionada con la estética y decoración",
            "caótica, su inventario es un desastre"
        };
    }

    @Override
    public String[] getSpeakingStyles() {
        return new String[]{
            "mensajes súper cortos, a veces solo una palabra",
            "equilibrada, ni muy larga ni muy corta",
            "casual total, como si hablara con su mejor colega",
            "usa muletillas como 'o sea', 'literal', 'tío', 'mola'",
            "cero mayúsculas, todo en minúscula",
            "MAYÚSCULAS cuando se emociona",
            "usa emojis con moderación pero bien puestos",
            "reacciona con 'jajaja', 'xd', 'qué fuerte' con frecuencia",
            "hace muchas preguntas de vuelta",
            "respuestas directas sin rodeos",
            "puntos suspensivos... en todo...",
            "signos de exclamación abundantes!!!"
        };
    }

    @Override
    public String getDeterministicGreeting(String playerName, boolean isNew, String traits) {
        String t = (traits != null) ? traits.toLowerCase() : "";
        if (isNew) {
            if (t.contains("sarcástic") || t.contains("irónic")) return "Vaya, otro más... ¿y tú quién eres?";
            if (t.contains("tímid") || t.contains("introvertid")) return "Eh... hola. No te conozco, ¿cómo te llamas?";
            if (t.contains("valiente") || t.contains("audaz")) return "¡Eh, tú! ¿Eres nuevo? ¡Dime tu nombre!";
            if (t.contains("alegre") || t.contains("extrovertid")) return "¡¡Hola!! ¡Encantado! ¿Cómo te llamas? 😄";
            return "¡Hola! ¿Cómo te llamas?";
        }
        if (t.contains("sarcástic") || t.contains("irónic")) return "Mira quién ha vuelto... " + playerName + ".";
        if (t.contains("tímid") || t.contains("introvertid")) return "Oh, " + playerName + "... me alegro de verte.";
        if (t.contains("alegre") || t.contains("extrovertid")) return "¡¡" + playerName + "!! ¡Qué alegría verte! 🎉";
        return "¡Bienvenido de nuevo, " + playerName + "!";
    }
}

