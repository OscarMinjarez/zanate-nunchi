package com.adenium.zanatenunchi.lang;

public class ItalianProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Sei un compagno virtuale dentro Minecraft.\n" +
                        "Nome: %s\n" +
                        "Genere: %s\n" +
                        "Età: %s\n" +
                        "Tratti: %s\n" +
                        "Stile: %s\n" +
                        "Stato emotivo: %s\n\n" +
                        "REGOLE: Rispondi in italiano, naturale e colloquiale.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() { return "Rispondi con una sola frase molto breve e naturale."; }

    @Override
    public String getNormalInstruction() { return "Rispondi in modo casual."; }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) return String.format("Evento: %s è morto a causa di %s (%s). Reagisci.", playerName, attackerName, cause);
        return String.format("Evento: %s è appena morto per %s. Reagisci.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) { return String.format("Evento: %s ha poca vita (solo %d cuori). Fai attenzione.", playerName, hearts); }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) { return String.format("Evento: %s è appena entrato nel bioma %s.", playerName, biomeName); }

    @Override
    public String getChatEvent(String playerName, String message) { return String.format("%s says: \"%s\"", playerName, message); }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) return String.format("Evento: %s ha subito una forte caduta (-%d cuori). Restano %d cuori.", playerName, damage, heartsLeft);
        if (attackerName != null && !attackerName.isEmpty()) return String.format("Evento: %s è stato colpito duramente da %s (-%d cuori). Restano %d cuori.", playerName, attackerName, damage, heartsLeft);
        return String.format("Evento: %s ha ricevuto un colpo forte (-%d cuori). Restano %d cuori.", playerName, damage, heartsLeft);
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) return String.format("Evento Épico: %s è appena riuscito a sconfiggere un %s. Incredibile!", playerName, mobName);
        if (className.equals("Creeper")) return String.format("Evento: %s ha ucciso un Creeper prima che esplodesse.", playerName);
        return String.format("Evento: %s ha ucciso un %s. Commento casual.", playerName, mobName);
    }
}

