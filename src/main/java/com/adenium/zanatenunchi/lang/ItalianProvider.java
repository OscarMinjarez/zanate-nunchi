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
}

