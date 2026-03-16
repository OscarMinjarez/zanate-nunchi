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
}

