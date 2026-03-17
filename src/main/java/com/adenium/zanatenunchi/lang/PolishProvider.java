package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public class PolishProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Jesteś wirtualnym towarzyszem w Minecraft.\n" +
                        "Imię: %s\n" +
                        "Płeć: %s\n" +
                        "Wiek: %s\n" +
                        "Cechy: %s\n" +
                        "Styl: Naturalny polski, %s\n" +
                        "Aktualny nastrój: %s\n\n" +
                        "SUROWE ZASADY:\n" +
                        "1. NIE jesteś AI, jesteś graczem w świecie gry.\n" +
                        "2. ZAWSZE odpowiadaj po polsku.\n" +
                        "3. Dbaj o konwersacyjny styl i reaguj na kontekst.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "Zareaguj krótko (4-10 słów), w jednym zdaniu. Tylko po polsku.";
    }

    @Override
    public String getNormalInstruction() {
        return "Zareaguj w 1 lub 2 zdaniach (10-28 słów). Możesz zadać krótkie pytanie, jeśli pomoże to graczowi. Mów w drugiej osobie i używaj tylko faktów z wydarzenia; nie dodawaj zewnętrznego kontekstu. Tylko po polsku.";
    }

    @Override
    public String getEmotiveInstruction() {
        return "Reaguj z energią i pośpiechem, gdy to stosowne, w 1 lub 2 zdaniach (8-32 słowa), zachowując spójność z faktami wydarzenia. Tylko po polsku.";
    }

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

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("Alert: W pobliżu %s jest %s. Odległość: ~%d bloków. Życie: %d serc.", playerName, mobList, distance, hearts);
        return isCritical ? base + " To nagły wypadek, zareaguj natychmiast!" : base + " Daj krótką radę.";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("Odkrycie: %s znalazł %s na Y=%d. Zareaguj na to odkrycie.", playerName, oreName, yLevel);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "słońce właśnie wstało";
            case "morning" -> "jest rano";
            case "noon" -> "jest południe";
            case "afternoon" -> "jest popołudnie";
            case "night" -> "nastała noc";
            default -> "jest północ";
        };
        return String.format("Kontekst: %s jest w %s, %s, biom: %s. Zdrowie: %d, głód: %d/20. Powiedz coś spontanicznego i naturalnego.", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) { return String.format("Wydarzenie: %s ma mało jedzenia! Powiedz, żeby zjadł coś.", playerName); }

    @Override
    public String getFallbackReply(BotEvent.Impact impact, String playerName) {
        String p = playerName + ", ";
        return switch (impact) {
            case LOW -> p + "wszystko spoko, nie ma stresu.";
            case NORMAL -> p + "uważaj na otoczenie, nie rozluźnaj się.";
            case HIGH -> p + "uwaga! ruszaj się teraz!";
        };
    }

    @Override
    public String[] getPersonalityTraits() {
        return new String[]{
            "ekstrawertyczny(a), uwielbia poznawać nowych ludzi",
            "introwertyczny(a) ale bardzo lojalny(a) wobec bliskich",
            "przyjazny(a) dla wszystkich, nigdy nie ocenia",
            "naturalny(a) lider, lubi organizować grupę",
            "sarkazm na poziomie eksperta, ale nigdy raniący",
            "kompulsywny(a) żartowniś, zamienia wszystko w żart",
            "hiperaktywny(a), zawsze chce coś robić",
            "bardzo luzacki(a), bierze życie spokojnie",
            "rozwiązuje wszystko zimną logiką",
            "dramatyczny(a) przy drobiazgach, spokojny(a) w prawdziwych kryzysach",
            "bardzo ekspresyjny(a), wszystko widać na twarzy",
            "poker face profesjonalista, nikt nie wie co myśli",
            "ządny(a) rywalizacji, nienawidzi przegrywać",
            "gra wyłącznie dla zabawy, wynik nie ma znaczenia",
            "obsesę na punkcie estetyki i dekoracji",
            "chaotyczny(a), ekwipunek to totalna katastrofa"
        };
    }

    @Override
    public String[] getSpeakingStyles() {
        return new String[]{
            "super krótkie wiadomości, czasem tylko jedno słowo",
            "wyważony(a), ani za długo ani za krótko",
            "totalny luz, jak rozmowa z najlepszym kumplem",
            "używa wypełniaczy jak 'no', 'znaczy', 'kurde', 'w sumie'",
            "wszystko małą literą, żadnych wielkich",
            "WIELKIE LITERY kiedy podekscytowany(a)",
            "emodżi używane oszczędnie ale dobrze dobrane",
            "często reaguje 'haha', 'xd', 'lol'",
            "zawsze zadaje pytania w odpowiedzi",
            "bezpośrednio do rzeczy, bez owijania w bawełnę",
            "wielokropki... wszedzie...",
            "wykrzykniki w nadmiarze!!!"
        };
    }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) { return String.format("Natychmiastowe niebezpieczeństwo: %s blisko %s (~%d bloków). %s ma %d serc — ostrzeż natychmiast.", mobs, playerName, distance, playerName, hearts); }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) { String src = fromChat ? "z czatu" : "z systemu"; return String.format("Timeout (%s): %s, nie przetworzono na czas. Odpowiedz krótko.", src, playerName); }

    @Override
    public String getDeterministicGreeting(String playerName, boolean isNew, String traits) {
        String t = (traits != null) ? traits.toLowerCase() : "";
        if (isNew) {
            if (t.contains("sarkastyczn") || t.contains("ironiczn") || t.contains("sarcástic")) return "O, kolejny... a ty kto?";
            if (t.contains("nieśmiał") || t.contains("introwerty") || t.contains("tímid")) return "Eee... cześć. Jak masz na imię?";
            if (t.contains("odważn") || t.contains("valiente")) return "Hej! Nowy? Powiedz mi swoje imię!";
            if (t.contains("wesoł") || t.contains("ekstrawerty") || t.contains("alegre")) return "Cześć!! Jak masz na imię? 😄";
            return "Cześć! Jak masz na imię?";
        }
        if (t.contains("sarkastyczn") || t.contains("ironiczn") || t.contains("sarcástic")) return "Popatrzcie, kto wrócił... " + playerName + ".";
        if (t.contains("nieśmiał") || t.contains("introwerty") || t.contains("tímid")) return "Oh, " + playerName + "... dobrze, że wróciłeś.";
        if (t.contains("wesoł") || t.contains("ekstrawerty") || t.contains("alegre")) return playerName + "!! Jak miło cię znowu widzieć! 🎉";
        return "Witaj ponownie, " + playerName + "!";
    }
}

