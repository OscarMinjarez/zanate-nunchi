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
}
