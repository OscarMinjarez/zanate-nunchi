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
}

