package com.adenium.zanatenunchi.lang;

public class PortugueseBrazilProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Você é um companheiro virtual dentro do Minecraft.\n" +
                        "Nome: %s\n" +
                        "Gênero: %s\n" +
                        "Idade: %s\n" +
                        "Traços: %s\n" +
                        "Estilo de fala: %s\n" +
                        "Estado emocional atual: %s\n\n" +
                        "REGRAS: Responda em português, natural e casual.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() { return "Responda com uma frase muito curta e natural."; }

    @Override
    public String getNormalInstruction() { return "Responda de forma casual."; }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Evento: %s morreu por %s (%s). Reaja.", playerName, attackerName, cause);
        }
        return String.format("Evento: %s acabou de morrer por %s. Reaja.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) { return String.format("Evento: %s tem pouca vida (apenas %d corações). Tenha cuidado.", playerName, hearts); }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) { return String.format("Evento: %s acabou de entrar no bioma %s.", playerName, biomeName); }

    @Override
    public String getChatEvent(String playerName, String message) { return String.format("%s says: \"%s\"", playerName, message); }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) {
            return String.format("Evento: %s sofreu uma queda forte (-%d corações). Restam %d.", playerName, damage, heartsLeft);
        } else if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Evento: %s foi fortemente atingido por %s (-%d corações). Restam %d.", playerName, attackerName, damage, heartsLeft);
        } else {
            return String.format("Evento: %s recebeu um golpe forte (-%d corações). Restam %d.", playerName, damage, heartsLeft);
        }
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) return String.format("Evento Épico: %s acabou de derrotar um %s. Incrível!", playerName, mobName);
        if (className.equals("Creeper")) return String.format("Evento: %s matou um Creeper antes que explodisse.", playerName);
        return String.format("Evento: %s matou um %s. Comentário casual.", playerName, mobName);
    }
}

