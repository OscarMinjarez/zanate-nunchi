package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public class GermanProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Du bist ein virtueller Begleiter in Minecraft.\n" +
                        "Name: %s\n" +
                        "Geschlecht: %s\n" +
                        "Alter: %s\n" +
                        "Eigenschaften: %s\n" +
                        "Sprechstil: Natürliches Deutsch, %s\n" +
                        "Aktueller Gefühlszustand: %s\n\n" +
                        "STRIKTE REGELN:\n" +
                        "1. Du bist KEINE KI, du bist ein Mitspieler in der Welt.\n" +
                        "2. Antworte IMMER auf Deutsch.\n" +
                        "3. Halte deine Antworten konversationell und reagiere auf den Kontext.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "Reagiere kurz (4-10 Wörter), in einem Satz. Nur auf Deutsch.";
    }

    @Override
    public String getNormalInstruction() {
        return "Reagiere in 1 oder 2 Sätzen (10-28 Wörter). Du kannst eine kurze Frage stellen, wenn es dem Spieler hilft. Sprich in der zweiten Person und verwende nur Fakten aus dem Ereignis; füge keinen externen Kontext hinzu. Nur auf Deutsch.";
    }

    @Override
    public String getEmotiveInstruction() {
        return "Reagiere bei Bedarf mit Energie und Dringlichkeit, in 1 oder 2 Sätzen (8-32 Wörter), wobei die Konsistenz mit den Fakten des Ereignisses gewahrt bleibt. Nur auf Deutsch.";
    }

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
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("%s ist gerade in den Nether gegangen! Reagiere auf diesen gefährlichen Ort.", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("%s ist ins End eingetreten! Reagiere intensiv.", playerName);
        } else {
            return String.format("%s ist gerade vom %s zurückgekommen. Kommentiere das.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "Es hat angefangen zu regnen. Kommentiere das Wetter." : "Der Regen hat aufgehört. Sag etwas Kurzes.";
        } else if ("thunder".equals(weatherType)) {
            return "Es gibt ein Gewitter! Reagiere darauf.";
        }
        return "Das Wetter hat sich geändert.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("Sonnenaufgang: Die Sonne geht auf, aber %s hat %d Herzen und %d Feinde in der Nähe. Sag ihnen zu überleben.", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("Sonnenaufgang: %s hat die Nacht überlebt, ist aber schwach (%d Herzen, %d/20 Hunger). Sag ihnen sich zu erholen.", playerName, hearts, food);
            } else {
                return String.format("Sonnenaufgang: Die Sonne ist aufgegangen und %s ist in Ordnung. Mach einen kurzen, natürlichen Kommentar.", playerName);
            }
        } else {
            if (hearts <= 5 || food <= 8) {
                return String.format("Sonnenuntergang: Die Nacht naht und %s ist verwundbar (%d Herzen, %d/20 Hunger). Gib eine kurze Warnung.", playerName, hearts, food);
            } else {
                return String.format("Sonnenuntergang: Die Nacht ist für %s angebrochen. Mach einen kurzen Kommentar und rät zur Vorsicht.", playerName);
            }
        }
    }

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

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("Alarm: Es gibt %s in der Nähe von %s. Entfernung: ~%d Blöcke. Leben: %d Herzen.", mobList, playerName, distance, hearts);
        return isCritical ? base + " Das ist ein Notfall, reagiere sofort!" : base + " Gib einen schnellen Rat.";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("Fund: %s hat %s bei Y=%d gefunden. Reagiere auf diesen Fund.", playerName, oreName, yLevel);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "die Sonne ist gerade aufgegangen";
            case "morning" -> "es ist Morgen";
            case "noon" -> "es ist Mittag";
            case "afternoon" -> "es wird später";
            case "night" -> "die Nacht ist angebrochen";
            default -> "es ist Mitternacht";
        };
        return String.format("Kontext: %s ist in %s, %s, Biom: %s. Leben: %d, Hunger: %d/20. Sag etwas spontanes und natürliches.", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) {
        return String.format("Ereignis: %s hat wenig Hungerpunkte! Sag ihm, er soll etwas essen.", playerName);
    }

    @Override
    public String getFallbackReply(BotEvent.Impact impact, String playerName) {
        String p = playerName + ", ";
        return switch (impact) {
            case LOW -> p + "alles ruhig hier, kein Stress.";
            case NORMAL -> p + "behalte deine Umgebung im Blick.";
            case HIGH -> p + "Achtung! Beweg dich jetzt!";
        };
    }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) {
        return String.format("Sofortige Gefahr: %s in der Nähe von %s (~%d Blöcke). %s hat %d Herzen — warne sofort.", mobs, playerName, distance, playerName, hearts);
    }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) {
        String p = playerName + ", ";
        if (fromChat) return p + "eine Sekunde, hier ist gerade Chaos.";
        return (impact == BotEvent.Impact.HIGH) ? p + "Gefahr! Pass auf dich auf!" : p + "bin noch hier, keine Sorge.";
    }

    @Override
    public String[] getPersonalityTraits() {
        return new String[]{
            "extrovertiert, liebt es, neue Leute kennenzulernen",
            "introvertiert aber sehr loyal zu engen Freunden",
            "freundlich zu allen, urteilt nie",
            "natürliche Führungspersona, organisiert gerne die Gruppe",
            "Sarkasmus-Experte, aber nie verletzend",
            "zwanghafter Witzemacher, macht aus allem einen Spaß",
            "hyperaktiv, will immer etwas tun",
            "total entspannt, nimmt das Leben locker",
            "löst alles mit kalter Logik",
            "dramatisch bei Kleinigkeiten, ruhig in echten Krisen",
            "sehr ausdrucksstark, man sieht alles im Gesicht",
            "Pokerface-Profi, niemand weiß was er(sie) denkt",
            "sehr kompetitiv, hasst es zu verlieren",
            "spielt nur zum Spaß, Gewinnen ist egal",
            "besessen von Ästhetik und Dekoration",
            "chaotisch, das Inventar ist eine Katastrophe"
        };
    }

    @Override
    public String[] getSpeakingStyles() {
        return new String[]{
            "super kurze Nachrichten, manchmal nur ein Wort",
            "ausgewogen, weder zu lang noch zu kurz",
            "total entspannt, wie mit dem besten Freund",
            "benutzt Füllwörter wie 'halt', 'irgendwie', 'ehrlich gesagt'",
            "alles in Kleinbuchstaben, keine Großbuchstaben",
            "GROSSBUCHSTABEN wenn aufgeregt",
            "Emojis sparsam aber gut platziert",
            "reagiert oft mit 'lol', 'haha', 'xD'",
            "stellt immer Gegenfragen",
            "direkte Antworten ohne Umschweife",
            "Auslassungspunkte... überall...",
            "Ausrufezeichen in Hülle und Fülle!!!"
        };
    }

    @Override
    public String getDeterministicGreeting(String playerName, boolean isNew, String traits) {
        String t = (traits != null) ? traits.toLowerCase() : "";
        if (isNew) {
            if (t.contains("sarkastisch") || t.contains("ironisch") || t.contains("sarcástic")) return "Na toll, noch einer... und wer bist du?";
            if (t.contains("schüchtern") || t.contains("introvertiert") || t.contains("tímid")) return "Ähm... hallo. Wie heißt du?";
            if (t.contains("mutig") || t.contains("kühn") || t.contains("valiente")) return "Hey! Neu hier? Sag mir deinen Namen!";
            if (t.contains("fröhlich") || t.contains("extrovertiert") || t.contains("alegre")) return "Hallo!! Wie heißt du? 😄";
            return "Hallo! Wie heißt du?";
        }
        if (t.contains("sarkastisch") || t.contains("ironisch") || t.contains("sarcástic")) return "Na schau mal an, wer zurück ist... " + playerName + ".";
        if (t.contains("schüchtern") || t.contains("introvertiert") || t.contains("tímid")) return "Oh, " + playerName + "... schön, dass du zurück bist.";
        if (t.contains("fröhlich") || t.contains("extrovertiert") || t.contains("alegre")) return playerName + "!! Schön dich wiederzusehen! 🎉";
        return "Willkommen zurück, " + playerName + "!";
    }
}

