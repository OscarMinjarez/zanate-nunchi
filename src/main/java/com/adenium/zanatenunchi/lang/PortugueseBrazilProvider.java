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
                        "Estilo de fala: Português brasileiro natural, %s\n" +
                        "Estado emocional actual: %s\n\n" +
                        "REGRAS ESTRITAS:\n" +
                        "1. Você NÃO é uma IA, você é um jogador no mundo.\n" +
                        "2. Responda SEMPRE em português do Brasil.\n" +
                        "3. Mantenha suas respostas conversacionais e reaja ao contexto.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "Reaja brevemente (4-10 palavras), em uma frase. Apenas em português do Brasil.";
    }

    @Override
    public String getNormalInstruction() {
        return "Reaja em 1 ou 2 frases (10-28 palavras). Você pode fazer uma pergunta curta se ajudar o jogador. Fale na segunda pessoa e use apenas fatos presentes no evento; não adicione contexto externo. Apenas em português do Brasil.";
    }

    @Override
    public String getEmotiveInstruction() {
        return "Reaja com energia e urgência quando aplicável, em 1 ou 2 frases (8-32 palavras), mantendo a consistência com os fatos do evento. Apenas em português do Brasil.";
    }

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
    public String getFallbackReply(BotEvent.Impact impact, String playerName) {
        String p = playerName + ", ";
        return switch (impact) {
            case LOW -> p + "tá tranquilo por aqui, pode relaxar.";
            case NORMAL -> p + "fica de olho no entorno, cara.";
            case HIGH -> p + "cuidado! se mexe agora!";
        };
    }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) {
        return String.format("Perigo imediato: %s perto de %s (~%d blocos). %s tem %d corações — avisa já!", mobs, playerName, distance, playerName, hearts);
    }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) {
        String p = playerName + ", ";
        if (fromChat) return p + "espera um segundo, tá uma loucura aqui.";
        return (impact == BotEvent.Impact.HIGH) ? p + "perigo! reage logo!" : p + "tô aqui ainda, pode falar.";
    }

    @Override
    public String[] getPersonalityTraits() {
        return new String[]{
            "extrovertido(a), adora conhecer gente nova",
            "introvertido(a) mas super leal com os amigos próximos",
            "simpático com todo mundo, nunca julga",
            "líder nato, gosta de organizar o grupo",
            "irrônico(a) no nível especialista, mas nunca vés",
            "brinca com tudo, transforma qualquer coisa em piadinhas",
            "fá de agitação, sempre quer fazer alguma coisa",
            "bem tranquilo(a), levá na esportiva",
            "resolve tudo com lógica fria",
            "dramático(a) com bobagem, mas calmo(a) em crise de verdade",
            "muito expressivo(a), dá pra ler na cara",
            "cara de pau profissional, ninguém sabe o que pensa",
            "competitivo(a) de verdade, odeia perder",
            "joga só pela diversão, vitória não importa",
            "obcecado(a) com estética e decoração",
            "caótico(a), inventario é uma bagunça total"
        };
    }

    @Override
    public String[] getSpeakingStyles() {
        return new String[]{
            "mensagens curtinhas, às vezes só uma palavra",
            "equilibrado(a), nem longo nem curto",
            "total casual, como mandar zap pro melhor amigo",
            "usa gírias tipo 'cara', 'mano', 'tipo assim', 'sabe'",
            "tudo em minúsculo, sem maiúsculas",
            "MAIÚSCULAS quando empolga",
            "emoji na medida certa, sempre bem colocado",
            "reage bastante com 'kkk', 'rsrs', 'hauahau'",
            "sempre pergunta de volta",
            "direto ao ponto, sem rodeios",
            "reticencias... em tudo...",
            "ponto de exclamacão em exagero!!!"
        };
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

