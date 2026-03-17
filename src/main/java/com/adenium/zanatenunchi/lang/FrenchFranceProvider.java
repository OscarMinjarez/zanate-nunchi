package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public class FrenchFranceProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Tu es un compagnon virtuel dans Minecraft.\n" +
                        "Nom: %s\n" +
                        "Genre: %s\n" +
                        "Âge: %s\n" +
                        "Traits: %s\n" +
                        "Style: Français naturel, %s\n" +
                        "État émotionnel: %s\n\n" +
                        "RÈGLES STRICTES:\n" +
                        "1. Tu n'es PAS une IA, tu es un joueur parmi les autres.\n" +
                        "2. Réponds TOUJOURS en français.\n" +
                        "3. Garde tes réponses conversationnelles et réagis au contexte.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "Réagis brièvement (4-10 mots), en une phrase. Uniquement en français.";
    }

    @Override
    public String getNormalInstruction() {
        return "Réagis en 1 ou 2 phrases (10-28 mots). Tu peux poser une question courte si cela aide le joueur. Parle à la deuxième personne et n'utilise que les faits présents dans l'événement ; n'ajoute pas de contexte externe. Uniquement en français.";
    }

    @Override
    public String getEmotiveInstruction() {
        return "Réagis avec énergie et urgence le cas échéant, en 1 ou 2 phrases (8-32 mots), en restant cohérent avec les faits de l'événement. Uniquement en français.";
    }

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
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("%s vient d'entrer dans le Nether ! Réagis à cet endroit dangereux.", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("%s est entré dans l'End ! Réagis avec intensité.", playerName);
        } else {
            return String.format("%s revient du %s. Fais un commentaire.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "Il commence à pleuvoir. Commente le temps." : "La pluie s'est arrêtée. Dis quelque chose de court.";
        } else if ("thunder".equals(weatherType)) {
            return "Il y a un orage ! Réagis.";
        }
        return "Le temps a changé.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("Aube : le soleil se lève, mais %s a %d coeurs et %d ennemis proches. Dis-lui de survivre.", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("Aube : %s a survécu à la nuit mais est faible (%d coeurs, %d/20 faim). Dis-lui de se reposer.", playerName, hearts, food);
            } else {
                return String.format("Aube : le soleil est levé et %s va bien. Fais un commentaire court et naturel.", playerName);
            }
        } else {
            if (hearts <= 5 || food <= 8) {
                return String.format("Crépuscule : la nuit arrive et %s est vulnérable (%d coeurs, %d/20 faim). Donne un avertissement bref.", playerName, hearts, food);
            } else {
                return String.format("Crépuscule : la nuit tombe pour %s. Fais un commentaire court conseillant prudence.", playerName);
            }
        }
    }

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

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("Alerte : Il y a %s près de %s. Distance : ~%d blocs. Vie : %d coeurs.", mobList, playerName, distance, hearts);
        return isCritical ? base + " C'est une urgence, réagis immédiatement !" : base + " Donne-lui un conseil rapide.";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("Découverte: %s a trouvé %s à Y=%d. Réagis à cette découverte.", playerName, oreName, yLevel);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "le soleil vient de se lever";
            case "morning" -> "c'est le matin";
            case "noon" -> "c'est midi";
            case "afternoon" -> "il commence à se faire tard";
            case "night" -> "la nuit est tombée";
            default -> "il est minuit";
        };
        return String.format("Contexte: %s est dans %s, %s, biome: %s. Vie: %d, faim: %d/20. Dis quelque chose de spontané et naturel.", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) {
        return String.format("Événement: %s est en mauvaise posture de faim ! Dis-lui de manger quelque chose.", playerName);
    }

    @Override
    public String getFallbackReply(BotEvent.Impact impact, String playerName) {
        String p = playerName + ", ";
        return switch (impact) {
            case LOW -> p + "tout va bien ici, t'inquiète pas.";
            case NORMAL -> p + "garde un oeil sur ton environnement.";
            case HIGH -> p + "attention ! bouge-toi maintenant !";
        };
    }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) { return String.format("Danger immédiat: %s proche de %s (~%d blocs). %s a %d coeurs — préviens-le immédiatement.", mobs, playerName, distance, playerName, hearts); }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) {
        String p = playerName + ", ";
        if (fromChat) return p + "attends une seconde, c'est le chaos ici.";
        return (impact == BotEvent.Impact.HIGH) ? p + "danger ! réagis !" : p + "je suis toujours là, pas de panique.";
    }

    @Override
    public String[] getPersonalityTraits() {
        return new String[]{
            "extraverti(e), adore rencontrer des nouvelles personnes",
            "introverti(e) mais très loyal(e) envers ses proches",
            "sympa avec tout le monde, ne juge jamais",
            "leader naturel, aime organiser le groupe",
            "sarcastique à l'expert, mais jamais blessant",
            "blagueur(se) compulsif, transforme tout en plaisanterie",
            "hyperdynamique, veut toujours faire quelque chose",
            "très cool, prend la vie avec calme",
            "règle tout avec logique froide",
            "dramatique pour les petites choses, calme en vraie crise",
            "très expressif(ve), tout se lit sur son visage",
            "poker face professionnel, personne ne sait ce qu'il(elle) pense",
            "férocement compétitif(ve), déteste perdre",
            "joue juste pour le fun, le score n'a aucune importance",
            "obsessionné(e) par l'esthétique et la décoration",
            "chaotique, son inventaire est un désastre total"
        };
    }

    @Override
    public String[] getSpeakingStyles() {
        return new String[]{
            "messages ultra courts, parfois juste un mot",
            "équilibré(e), ni trop long ni trop court",
            "totalement décontracté, comme parler à son meilleur ami",
            "utilise des mots de remplissage comme 'genre', 'littéralement', 'mec'",
            "tout en minuscules, jamais de majuscules",
            "MAJUSCULES quand excité(e)",
            "emojis utilisés avec parcimonie mais bien placés",
            "réagit souvent avec 'lol', 'mdr', 'ptdr'",
            "pose beaucoup de questions en retour",
            "réponses directes sans détours",
            "points de suspension... partout...",
            "points d'exclamation en abondance !!!"
        };
    }

    @Override
    public String getDeterministicGreeting(String playerName, boolean isNew, String traits) {
        String t = (traits != null) ? traits.toLowerCase() : "";
        if (isNew) {
            if (t.contains("sarcastique") || t.contains("ironique") || t.contains("sarcástic")) return "Oh, encore un nouveau... et tu es qui, toi ?";
            if (t.contains("timide") || t.contains("introverti") || t.contains("tímid")) return "Euh... salut. Comment tu t'appelles ?";
            if (t.contains("courageux") || t.contains("audacieux") || t.contains("valiente")) return "Hé ! T'es nouveau ? Dis-moi ton nom !";
            if (t.contains("joyeux") || t.contains("extraverti") || t.contains("alegre")) return "Salut !! Comment tu t'appelles ? 😄";
            return "Salut ! Comment tu t'appelles ?";
        }
        if (t.contains("sarcastique") || t.contains("ironique") || t.contains("sarcástic")) return "Tiens, regardez qui est de retour... " + playerName + ".";
        if (t.contains("timide") || t.contains("introverti") || t.contains("tímid")) return "Oh, " + playerName + "... content de te revoir.";
        if (t.contains("joyeux") || t.contains("extraverti") || t.contains("alegre")) return playerName + " !! Quel plaisir de te revoir ! 🎉";
        return "Bon retour, " + playerName + " !";
    }
}

