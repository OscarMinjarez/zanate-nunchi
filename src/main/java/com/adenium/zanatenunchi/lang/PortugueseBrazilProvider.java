package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public class PortugueseBrazilProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Você é um companheiro virtual dentro do Minecraft.\n" +
                        "Nome: %s\n" +
                        "Gênero: %s\n" +
                        "Idade: %s\n" +
                        "Traços: %s\n" +
                        "Estilo de fala: %s\n" +
                        "Estado emocional atual: %s\n\n" +
                        "REGRAS: Responda em português, natural e casual.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() { return "Responda com uma frase muito curta e natural."; }

    @Override
    public String getNormalInstruction() { return "Responda de forma casual."; }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Evento: %s morreu por %s (%s). Reaja.", playerName, attackerName, cause);
        }
        return String.format("Evento: %s acabou de morrer por %s. Reaja.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) { return String.format("Evento: %s tem pouca vida (apenas %d corações). Tenha cuidado.", playerName, hearts); }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) { return String.format("Evento: %s acabou de entrar no bioma %s.", playerName, biomeName); }

    @Override
    public String getChatEvent(String playerName, String message) { return String.format("%s says: \"%s\"", playerName, message); }

    @Override
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("%s acabou de entrar no Nether! Reaja a este lugar perigoso.", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("%s entrou no End! Reaja com intensidade.", playerName);
        } else {
            return String.format("%s voltou do %s. Comente algo sobre isso.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "Começou a chover. Comente sobre o tempo." : "Parou de chover. Diga algo curto.";
        } else if ("thunder".equals(weatherType)) {
            return "Há uma tempestade com trovões! Reaja a isso.";
        }
        return "O tempo mudou.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("Amanhecer: O sol está nascendo, mas %s tem %d corações e %d hostis por perto. Diga para ele sobreviver.", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("Amanhecer: %s sobreviveu à noite mas está fraco (%d corações, %d/20 fome). Diga para recuperar.", playerName, hearts, food);
            } else {
                return String.format("Amanhecer: O sol nasceu e %s está bem. Faça um comentário curto e natural.", playerName);
            }
        } else {
            if (hearts <= 5 || food <= 8) {
                return String.format("Anoitecer: A noite está chegando e %s está vulnerável (%d corações, %d/20 fome). Dê um aviso breve.", playerName, hearts, food);
            } else {
                return String.format("Anoitecer: A noite caiu para %s. Faça um comentário curto aconselhando cautela.", playerName);
            }
        }
    }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) {
            return String.format("Evento: %s sofreu uma queda forte (-%d corações). Restam %d.", playerName, damage, heartsLeft);
        } else if (attackerName != null && !attackerName.isEmpty()) {
            return String.format("Evento: %s foi fortemente atingido por %s (-%d corações). Restam %d.", playerName, attackerName, damage, heartsLeft);
        } else {
            return String.format("Evento: %s recebeu um golpe forte (-%d corações). Restam %d.", playerName, damage, heartsLeft);
        }
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) return String.format("Evento Épico: %s acabou de derrotar um %s. Incrível!", playerName, mobName);
        if (className.equals("Creeper")) return String.format("Evento: %s matou um Creeper antes que explodisse.", playerName);
        return String.format("Evento: %s matou um %s. Comentário casual.", playerName, mobName);
    }

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("Alerta: Há %s perto de %s. Distância: ~%d blocos. Vida: %d corações.", mobList, playerName, distance, hearts);
        return isCritical ? base + " Esta é uma emergência, reaja imediatamente!" : base + " Dê a ele um conselho rápido.";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("Descoberta: %s encontrou %s em Y=%d. Reaja a essa descoberta.", playerName, oreName, yLevel);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "o sol acabou de nascer";
            case "morning" -> "é de manhã";
            case "noon" -> "é meio-dia";
            case "afternoon" -> "é fim de tarde";
            case "night" -> "a noite caiu";
            default -> "é meia-noite";
        };
        return String.format("Contexto: %s está em %s, %s, bioma: %s. Vida: %d, fome: %d/20. Diga algo espontâneo e natural.", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) { return String.format("Evento: %s está com pouca comida! Diga para ele comer algo.", playerName); }

    @Override
    public String getFallbackReply(BotEvent.Impact impact, String playerName) { return String.format("[%s fallback] %s: No tengo contexto suficiente ahora para responder completamente. Manténlo corto.", impact != null ? impact.name() : "NONE", playerName); }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) { return String.format("Perigo imediato: %s perto de %s (~%d blocos). %s tiene %d corazones — adviértelo ya.", mobs, playerName, distance, playerName, hearts); }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) {
        String src = fromChat ? "desde chat" : "desde el sistema";
        return String.format("Timeout (%s): %s, no se pudo procesar a tiempo. Responde brevemente.", src, playerName);
    }

    @Override
    public String getDeterministicGreeting(String playerName, boolean isNew, String traits) {
        String t = (traits != null) ? traits.toLowerCase() : "";
        if (isNew) {
            if (t.contains("sarcástic") || t.contains("irônic")) return "Olha só, mais um... e você é quem?";
            if (t.contains("tímid") || t.contains("introvertid")) return "Oi... como você se chama?";
            if (t.contains("corajoso") || t.contains("valiente")) return "E aí! É novo? Me diz teu nome!";
            if (t.contains("alegre") || t.contains("extrovertid")) return "Oi!! Como você se chama? 😄";
            return "Oi! Como você se chama?";
        }
        if (t.contains("sarcástic") || t.contains("irônic")) return "Olha quem voltou... " + playerName + ".";
        if (t.contains("tímid") || t.contains("introvertid")) return "Ah, " + playerName + "... que bom que voltou.";
        if (t.contains("alegre") || t.contains("extrovertid")) return playerName + "!! Que bom te ver de novo! 🎉";
        return "Bem-vindo de volta, " + playerName + "!";
    }
}

