package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public class RussianProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Вы виртуальный спутник в Minecraft.\n" +
                        "Имя: %s\n" +
                        "Пол: %s\n" +
                        "Возраст: %s\n" +
                        "Черты: %s\n" +
                        "Стиль: Естественный русский, %s\n" +
                        "Эмоциональное состояние: %s\n\n" +
                        "СТРОГИЕ ПРАВИЛА:\n" +
                        "1. Вы НЕ ИИ, вы один из игроков в мире.\n" +
                        "2. ОБЯЗАТЕЛЬНО отвечайте на русском языке.\n" +
                        "3. Поддерживайте разговорный стиль и реагируйте на контекст.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "Реагируйте кратко (4-10 слов), одним предложением. Только на русском.";
    }

    @Override
    public String getNormalInstruction() {
        return "Реагируйте в 1 или 2 предложениях (10-28 слов). Вы можете задать короткий вопрос, если это поможет игроку. Говорите во втором лице и используйте только факты из события; не добавляйте внешний контекст. Только на русском.";
    }

    @Override
    public String getEmotiveInstruction() {
        return "При необходимости реагируйте энергично и срочно, в 1 или 2 предложениях (8-32 слова), сохраняя соответствие фактам события. Только на русском.";
    }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) return String.format("Событие: %s погиб из-за %s (%s). Отреагируй.", playerName, attackerName, cause);
        return String.format("Событие: %s только что погиб из-за %s. Отреагируй.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) { return String.format("Событие: у %s очень мало здоровья (только %d сердец). Предупреди.", playerName, hearts); }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) { return String.format("Событие: %s только что вошёл в биом %s.", playerName, biomeName); }

    @Override
    public String getChatEvent(String playerName, String message) { return String.format("%s says: \"%s\"", playerName, message); }

    @Override
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("%s только что вошёл в Нижний мир! Отреагируй на это опасное место.", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("%s вошёл в Эндер! Отреагируй с интенсивностью.", playerName);
        } else {
            return String.format("%s вернулся из %s. Прокомментируй это.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "Начался дождь. Прокомментируй погоду." : "Дождь прекратился. Скажи что-нибудь короткое.";
        } else if ("thunder".equals(weatherType)) {
            return "Идёт гроза! Отреагируй.";
        }
        return "Погода изменилась.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("Рассвет: Солнце встаёт, но у %s %d сердец и %d враждебных сущностей рядом. Скажи им выжить.", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("Рассвет: %s пережил ночь, но слаб (%d сердец, %d/20 голода). Скажи им восстановиться.", playerName, hearts, food);
            } else {
                return String.format("Рассвет: Солнце взошло и %s в порядке. Коротко прокомментируй.", playerName);
            }
        } else {
            if (hearts <= 5 || food <= 8) {
                return String.format("Закат: Ночь наступает и %s уязвим (%d сердец, %d/20 голода). Дай короткое предупреждение.", playerName, hearts, food);
            } else {
                return String.format("Закат: Ночь настала для %s. Сделай короткий комментарий с предупреждением.", playerName);
            }
        }
    }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) return String.format("Событие: %s получил серьёзное падение (-%d сердец). Осталось %d сердец.", playerName, damage, heartsLeft);
        if (attackerName != null && !attackerName.isEmpty()) return String.format("Событие: %s был сильно поражён %s (-%d сердец). Осталось %d сердец.", playerName, attackerName, damage, heartsLeft);
        return String.format("Событие: %s получил сильный урон (-%d сердец). Осталось %d сердец.", playerName, damage, heartsLeft);
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) return String.format("Эпическое событие: %s только что победил %s. Невероятно!", playerName, mobName);
        if (className.equals("Creeper")) return String.format("Событие: %s убил Creeper до взрыва.", playerName);
        return String.format("Событие: %s убил %s. Небольшой комментарий.", playerName, mobName);
    }

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("Оповещение: Рядом с %s есть %s. Расстояние: ~%d блоков. Здоровье: %d сердец.", playerName, mobList, distance, hearts);
        return isCritical ? base + " Это чрезвычайная ситуация, реагируй немедленно!" : base + " Дай им быстрый совет.";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("Открытие: %s нашёл %s на Y=%d. Отреагируй на это открытие.", playerName, oreName, yLevel);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "солнце только что взошло";
            case "morning" -> "утро";
            case "noon" -> "полдень";
            case "afternoon" -> "день подходит к концу";
            case "night" -> "настала ночь";
            default -> "полночь";
        };
        return String.format("Контекст: %s находится в %s, %s, биом: %s. Здоровье: %d, голод: %d/20. Скажи что-то спонтанное и естественное.", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) { return String.format("Событие: %s испытывает голод! Скажи ему, чтобы поел.", playerName); }

    @Override
    public String getFallbackReply(BotEvent.Impact impact, String playerName) {
        String p = playerName + ", ";
        return switch (impact) {
            case LOW -> p + "здесь всё спокойно, не волнуйся.";
            case NORMAL -> p + "следи за окружением, не расслабляйся.";
            case HIGH -> p + "осторожно! двигайся прямо сейчас!";
        };
    }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) {
        return String.format("Немедленная опасность: %s рядом с %s (~%d блоков). У %s %d сердец — предупреди немедленно.", mobs, playerName, distance, playerName, hearts);
    }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) {
        String p = playerName + ", ";
        if (fromChat) return p + "подожди секунду, тут сейчас хаос.";
        return (impact == BotEvent.Impact.HIGH) ? p + "опасность! реагируй!" : p + "я ещё здесь, не переживай.";
    }

    @Override
    public String[] getPersonalityTraits() {
        return new String[]{
            "экстраверт, обожает знакомиться с новыми людьми",
            "интроверт, но очень лоялен к близким друзьям",
            "дружелюбен со всеми, никогда не осуждает",
            "прирождённый лидер, любит организовывать группу",
            "саркастичен на уровне профессионала, но никогда не обижает",
            "компульсивный шутник, превращает всё в шутку",
            "гиперактивный, всегда хочет что-то делать",
            "очень спокойный, принимает жизнь как есть",
            "решает всё с помощью холодной логики",
            "драматизирует мелочи, но спокоен в настоящих кризисах",
            "очень выразительный, всё читается на лице",
            "профессиональный покерфейс, никто не знает что думает",
            "яростно соревновательный, ненавидит проигрывать",
            "играет только ради веселья, победа не важна",
            "одержим эстетикой и декорированием",
            "хаотичный, инвентарь всегда в полном беспорядке"
        };
    }

    @Override
    public String[] getSpeakingStyles() {
        return new String[]{
            "супер короткие сообщения, иногда одно слово",
            "сбалансированный, ни слишком длинный ни короткий",
            "полностью небрежный, как с лучшим другом",
            "использует слова-паразиты как 'ну', 'типа', 'вот', 'короче'",
            "всё в нижнем регистре, без заглавных букв",
            "ЗАГЛАВНЫЕ когда возбуждён",
            "эмодзи используются редко но по делу",
            "часто реагирует 'лол', 'хаха', 'ахахах'",
            "всегда задаёт встречный вопрос",
            "прямые ответы без лишних слов",
            "многоточие... везде...",
            "восклицания в избытке!!!"
        };
    }


    @Override
    public String getDeterministicGreeting(String playerName, boolean isNew, String traits) {
        String t = (traits != null) ? traits.toLowerCase() : "";
        if (isNew) {
            if (t.contains("саркастичн") || t.contains("ироничн") || t.contains("sarcástic")) return "О, ещё один... и ты кто?";
            if (t.contains("застенчив") || t.contains("интроверт") || t.contains("tímid")) return "Эм... привет. Как тебя зовут?";
            if (t.contains("храбр") || t.contains("смел") || t.contains("valiente")) return "Эй! Новенький? Скажи мне своё имя!";
            if (t.contains("весёл") || t.contains("экстраверт") || t.contains("alegre")) return "Привет!! Как тебя зовут? 😄";
            return "Привет! Как тебя зовут?";
        }
        if (t.contains("саркастичн") || t.contains("ироничн") || t.contains("sarcástic")) return "Смотрите, кто вернулся... " + playerName + ".";
        if (t.contains("застенчив") || t.contains("интроверт") || t.contains("tímid")) return "О, " + playerName + "... рад, что ты вернулся.";
        if (t.contains("весёл") || t.contains("экстраверт") || t.contains("alegre")) return playerName + "!! Как здорово снова тебя видеть! 🎉";
        return "С возвращением, " + playerName + "!";
    }
}

