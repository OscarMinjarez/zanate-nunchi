package com.adenium.zanatenunchi.lang;

public class PolishProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Jesteś wirtualnym towarzyszem w Minecraft.\n" +
                        "Imię: %s\n" +
                        "Płeć: %s\n" +
                        "Wiek: %s\n" +
                        "Cechy: %s\n" +
                        "Styl: %s\n" +
                        "Aktualny nastrój: %s\n\n" +
                        "ZASADY: Odpowiadaj po polsku, naturalnie i swobodnie.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() { return "Odpowiedz jednowierszowo, bardzo krótko i naturalnie."; }

    @Override
    public String getNormalInstruction() { return "Odpowiedz swobodnie."; }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) return String.format("Wydarzenie: %s zginął z powodu %s (%s). Zareaguj.", playerName, attackerName, cause);
        return String.format("Wydarzenie: %s właśnie zginął z powodu %s. Zareaguj.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) { return String.format("Wydarzenie: %s ma bardzo mało życia (tylko %d serc). Uważaj.", playerName, hearts); }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) { return String.format("Wydarzenie: %s właśnie wszedł do biomu %s.", playerName, biomeName); }

    @Override
    public String getChatEvent(String playerName, String message) { return String.format("%s says: \"%s\"", playerName, message); }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) return String.format("Wydarzenie: %s otrzymał poważny upadek (-%d serc). Pozostało %d serc.", playerName, damage, heartsLeft);
        if (attackerName != null && !attackerName.isEmpty()) return String.format("Wydarzenie: %s został mocno uderzony przez %s (-%d serc). Pozostało %d serc.", playerName, attackerName, damage, heartsLeft);
        return String.format("Wydarzenie: %s otrzymał silne obrażenia (-%d serc). Pozostało %d serc.", playerName, damage, heartsLeft);
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) return String.format("Wydarzenie Epickie: %s właśnie pokonał %s. Niesamowite!", playerName, mobName);
        if (className.equals("Creeper")) return String.format("Wydarzenie: %s zabił Creeper przed wybuchem.", playerName);
        return String.format("Wydarzenie: %s zabił %s. Luźny komentarz.", playerName, mobName);
    }
}

