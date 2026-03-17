package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public class ItalianProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Sei un compagno virtuale dentro Minecraft.\n" +
                        "Nome: %s\n" +
                        "Genere: %s\n" +
                        "Età: %s\n" +
                        "Tratti: %s\n" +
                        "Stile: Italiano naturale, %s\n" +
                        "Stato emotivo: %s\n\n" +
                        "REGOLE RIGIDE:\n" +
                        "1. NON sei un'IA, sei un compagno di gioco nel mondo.\n" +
                        "2. Rispondi SEMPRE in italiano.\n" +
                        "3. Mantieni le tue risposte colloquiali e reagisci al contesto.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "Reagisci brevemente (4-10 parole), in una frase. Solo in italiano.";
    }

    @Override
    public String getNormalInstruction() {
        return "Reagisci in 1 o 2 frasi (10-28 parole). Puoi fare una domanda breve se aiuta il giocatore. Parla in seconda persona e usa solo fatti presenti nell'evento; non aggiungere contesto esterno. Solo in italiano.";
    }

    @Override
    public String getEmotiveInstruction() {
        return "Reagisci con energia e urgenza quando applicabile, in 1 o 2 frasi (8-32 parole), mantenendo la coerenza con i fatti dell'evento. Solo in italiano.";
    }

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
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("%s è appena entrato nel Nether! Reagisci a questo luogo pericoloso.", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("%s è entrato nell'End! Reagisci con intensità.", playerName);
        } else {
            return String.format("%s è tornato dal %s. Fai un commento a riguardo.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "Ha iniziato a piovere. Commenta il tempo." : "Ha smesso di piovere. Di qualcosa di breve.";
        } else if ("thunder".equals(weatherType)) {
            return "C'è un temporale! Reagisci.";
        }
        return "Il tempo è cambiato.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("Alba: il sole sta sorgendo, ma %s ha %d cuori e %d nemici vicini. Dì loro di sopravvivere.", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("Alba: %s ha superato la notte ma è debole (%d cuori, %d/20 fame). Dì loro di riprendersi.", playerName, hearts, food);
            } else {
                return String.format("Alba: è sorto il sole e %s sta bene. Fai un commento breve e naturale.", playerName);
            }
        } else {
            if (hearts <= 5 || food <= 8) {
                return String.format("Tramonto: sta arrivando la notte e %s è vulnerabile (%d cuori, %d/20 fame). Dai un breve avviso.", playerName, hearts, food);
            } else {
                return String.format("Tramonto: la notte è calata per %s. Fai un commento breve consigliando prudenza.", playerName);
            }
        }
    }

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

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("Allarme: Ci sono %s vicino a %s. Distanza: ~%d blocchi. Vita: %d cuori.", mobList, playerName, distance, hearts);
        return isCritical ? base + " È un'emergenza, reagisci subito!" : base + " Dagli un consiglio rapido.";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("Scoperta: %s ha trovato %s a Y=%d. Reagisci a questa scoperta.", playerName, oreName, yLevel);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "il sole è appena sorto";
            case "morning" -> "è mattina";
            case "noon" -> "è mezzogiorno";
            case "afternoon" -> "sta calando il pomeriggio";
            case "night" -> "la notte è calata";
            default -> "è mezzanotte";
        };
        return String.format("Contesto: %s è in %s, %s, bioma: %s. Salute: %d, cibo: %d/20. Dì qualcosa di spontaneo e naturale.", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) {
        return String.format("Evento: %s sta morendo di fame! Digli di mangiare qualcosa.", playerName);
    }

    @Override
    public String getFallbackReply(BotEvent.Impact impact, String playerName) {
        String p = playerName + ", ";
        return switch (impact) {
            case LOW -> p + "tutto tranquillo, non preoccuparti.";
            case NORMAL -> p + "tieni d'occhio i dintorni.";
            case HIGH -> p + "attento! muoviti adesso!";
        };
    }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) {
        return String.format("Pericolo immediato: %s vicino a %s (~%d blocchi). %s ha %d cuori — avverti subito.", mobs, playerName, distance, playerName, hearts);
    }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) {
        String p = playerName + ", ";
        if (fromChat) return p + "aspetta un secondo, è il caos qui.";
        return (impact == BotEvent.Impact.HIGH) ? p + "pericolo! reagisci!" : p + "sono ancora qui, non preoccuparti.";
    }

    @Override
    public String[] getPersonalityTraits() {
        return new String[]{
            "estroverso(a), adora conoscere nuove persone",
            "introverso(a) ma fedelissimo(a) agli amici più cari",
            "simpatico(a) con tutti, non giudica mai",
            "leader naturale, ama organizzare il gruppo",
            "sarcastico(a) a livello esperto, ma mai offensivo",
            "battutista compulsivo, trasforma tutto in una barzelletta",
            "iperattivo(a), vuole sempre fare qualcosa",
            "molto rilassato(a), prende la vita con calma",
            "risolve tutto con logica fredda",
            "drammatico(a) per le piccole cose, calmo(a) in crisi reali",
            "molto espressivo(a), si legge tutto sulla faccia",
            "poker face professionista, nessuno sa cosa pensa",
            "agguerritamente competitivo(a), odia perdere",
            "gioca solo per divertimento, vincere non conta",
            "ossessionato(a) dall'estetica e dalla decorazione",
            "caotico(a), inventario sempre in disordine"
        };
    }

    @Override
    public String[] getSpeakingStyles() {
        return new String[]{
            "messaggi super corti, a volte solo una parola",
            "equilibrato(a), né troppo lungo né troppo corto",
            "totalmente casual, come parlare col miglior amico",
            "usa intercalari come 'tipo', 'cioè', 'letteralmente'",
            "tutto in minuscolo, niente maiuscole",
            "MAIUSCOLE quando è eccitato(a)",
            "emoji usate con parsimonia ma ben piazzate",
            "reagisce spesso con 'lol', 'ahaha', 'xd'",
            "fa sempre domande di rimando",
            "risposte dirette senza giri di parole",
            "puntini di sospensione... ovunque...",
            "punti esclamativi in abbondanza!!!"
        };
    }

    @Override
    public String getDeterministicGreeting(String playerName, boolean isNew, String traits) {
        String t = (traits != null) ? traits.toLowerCase() : "";
        if (isNew) {
            if (t.contains("sarcastico") || t.contains("ironico") || t.contains("sarcástic")) return "Oh, un altro... e tu chi saresti?";
            if (t.contains("timido") || t.contains("introverso") || t.contains("tímid")) return "Ehm... ciao. Come ti chiami?";
            if (t.contains("coraggioso") || t.contains("audace") || t.contains("valiente")) return "Ehi! Sei nuovo? Dimmi il tuo nome!";
            if (t.contains("allegro") || t.contains("estroverso") || t.contains("alegre")) return "Ciao!! Come ti chiami? 😄";
            return "Ciao! Come ti chiami?";
        }
        if (t.contains("sarcastico") || t.contains("ironico") || t.contains("sarcástic")) return "Guarda chi si rivede... " + playerName + ".";
        if (t.contains("timido") || t.contains("introverso") || t.contains("tímid")) return "Oh, " + playerName + "... bene che sei tornato.";
        if (t.contains("allegro") || t.contains("estroverso") || t.contains("alegre")) return playerName + "!! Che bello rivederti! 🎉";
        return "Bentornato, " + playerName + "!";
    }
}

