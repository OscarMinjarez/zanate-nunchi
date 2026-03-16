package com.adenium.zanatenunchi.lang;

public class RussianProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "Вы виртуальный спутник в Minecraft.\n" +
                        "Имя: %s\n" +
                        "Пол: %s\n" +
                        "Возраст: %s\n" +
                        "Черты: %s\n" +
                        "Стиль: %s\n" +
                        "Эмоциональное состояние: %s\n\n" +
                        "ПРАВИЛА: Отвечай по-русски, естественно и неформально.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() { return "Ответь одним очень коротким и естественным предложением."; }

    @Override
    public String getNormalInstruction() { return "Отвечай непринуждённо."; }

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
}

