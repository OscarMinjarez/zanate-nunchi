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
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("%s właśnie wszedł do Netheru! Zareaguj na to niebezpieczne miejsce.", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("%s wszedł do Endu! Zareaguj intensywnie.", playerName);
        } else {
            return String.format("%s wrócił z %s. Skomentuj to.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "Zaczęło padać. Skomentuj pogodę." : "Przestało padać. Powiedz coś krótkiego.";
        } else if ("thunder".equals(weatherType)) {
            return "Jest burza z piorunami! Zareaguj.";
        }
        return "Pogoda się zmieniła.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("Wschód słońca: Słońce wschodzi, ale %s ma %d serc i %d wrogów w pobliżu. Powiedz, żeby przetrwał.", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("Wschód słońca: %s przetrwał noc, ale jest słaby (%d serc, %d/20 głód). Powiedz, żeby się zregenerował.", playerName, hearts, food);
            } else {
                return String.format("Wschód słońca: Słońce wstało i %s ma się dobrze. Skomentuj krótko.", playerName);
            }
        } else {
            if (hearts <= 5 || food <= 8) {
                return String.format("Zachód słońca: Noc nadchodzi i %s jest wrażliwy (%d serc, %d/20 głód). Daj krótkie ostrzeżenie.", playerName, hearts, food);
            } else {
                return String.format("Zachód słońca: Noc nadeszła dla %s. Skomentuj krótko i ostrzeż.", playerName);
            }
        }
    }

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

