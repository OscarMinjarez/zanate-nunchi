package com.adenium.zanatenunchi.lang;

public class GermanProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Du bist ein virtueller Begleiter in Minecraft.\n" +
                        "Name: %s\n" +
                        "Geschlecht: %s\n" +
                        "Alter: %s\n" +
                        "Eigenschaften: %s\n" +
                        "Sprechstil: %s\n" +
                        "Aktueller Gefühlszustand: %s\n\n" +
                        "REGELN: Antworte auf Deutsch, locker und natürlich.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() { return "Antworte mit einem sehr kurzen, natürlichen Satz."; }

    @Override
    public String getNormalInstruction() { return "Antworte locker."; }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) return String.format("Ereignis: %s ist durch %s (%s) gestorben. Reagiere.", playerName, attackerName, cause);
        return String.format("Ereignis: %s ist gerade an %s gestorben. Reagiere.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) { return String.format("Ereignis: %s hat sehr wenig Leben (nur %d Herzen). Sei vorsichtig.", playerName, hearts); }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) { return String.format("Ereignis: %s hat gerade den Biom %s betreten.", playerName, biomeName); }

    @Override
    public String getChatEvent(String playerName, String message) { return String.format("%s says: \"%s\"", playerName, message); }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) return String.format("Ereignis: %s hat eine starke Fallverletzung erlitten (-%d Herzen). Es bleiben %d Herzen.", playerName, damage, heartsLeft);
        if (attackerName != null && !attackerName.isEmpty()) return String.format("Ereignis: %s wurde schwer von %s getroffen (-%d Herzen). Es bleiben %d Herzen.", playerName, attackerName, damage, heartsLeft);
        return String.format("Ereignis: %s hat einen schweren Treffer erlitten (-%d Herzen). Es bleiben %d Herzen.", playerName, damage, heartsLeft);
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) return String.format("Ereignis Episch: %s hat gerade einen %s besiegt. Unglaublich!", playerName, mobName);
        if (className.equals("Creeper")) return String.format("Ereignis: %s hat einen Creeper getötet, bevor er explodierte.", playerName);
        return String.format("Ereignis: %s hat einen %s getötet. Lockerer Kommentar.", playerName, mobName);
    }
}

