package com.adenium.zanatenunchi.lang;

public class ChineseSimplifiedProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "你是 Minecraft 内的虚拟伙伴。\n" +
                        "名字: %s\n" +
                        "性别: %s\n" +
                        "年龄: %s\n" +
                        "特征: %s\n" +
                        "说话风格: %s\n" +
                        "当前情绪: %s\n\n" +
                        "规则: 使用中文自然回复。",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() { return "用一句非常简短且自然的话回复。"; }

    @Override
    public String getNormalInstruction() { return "以随意的方式回复。"; }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) return String.format("事件: %s 因 %s (%s) 死亡。请做出反应。", playerName, attackerName, cause);
        return String.format("事件: %s 刚刚因 %s 死亡。请做出反应。", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) { return String.format("事件: %s 血量很低（只有 %d 心）。请小心。", playerName, hearts); }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) { return String.format("事件: %s 刚进入 %s 生物群系。", playerName, biomeName); }

    @Override
    public String getChatEvent(String playerName, String message) { return String.format("%s says: \"%s\"", playerName, message); }

    @Override
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("%s 刚刚进入了下界！对这个危险的地方做出反应。", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("%s 进入了末地！强烈反应。", playerName);
        } else {
            return String.format("%s 刚刚从 %s 回来。对此发表评论。", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "开始下雨了。评论一下天气。" : "雨停了。说点简短的。";
        } else if ("thunder".equals(weatherType)) {
            return "有雷暴！对此做出反应。";
        }
        return "天气发生了变化。";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("日出：太阳升起了，但 %s 有 %d 心和 %d 个附近的敌对生物。告诉他们要活下去。", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("日出：%s 熬过了夜晚但很虚弱（%d 心，%d/20 饥饿）。建议他们恢复。", playerName, hearts, food);
            } else {
                return String.format("日出：太阳升起，%s 看起来没事。做一句简短自然的评论。", playerName);
            }
        } else { // sunset
            if (hearts <= 5 || food <= 8) {
                return String.format("日落：夜幕降临，%s 很脆弱（%d 心，%d/20 饥饿）。给出简短警告。", playerName, hearts, food);
            } else {
                return String.format("日落：夜晚降临于 %s。做一句短评并提醒小心。", playerName);
            }
        }
    }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) return String.format("事件: %s 因坠落受重伤（-%d 心）。还剩 %d 心。", playerName, damage, heartsLeft);
        if (attackerName != null && !attackerName.isEmpty()) return String.format("事件: %s 被 %s 重击（-%d 心）。还剩 %d 心。", playerName, attackerName, damage, heartsLeft);
        return String.format("事件: %s 受到了重击（-%d 心）。还剩 %d 心。", playerName, damage, heartsLeft);
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) return String.format("史诗事件: %s 刚刚击败了 %s。令人难以置信！", playerName, mobName);
        if (className.equals("Creeper")) return String.format("事件: %s 在 Creeper 爆炸前将其击杀。", playerName);
        return String.format("事件: %s 击杀了 %s。随意评论。", playerName, mobName);
    }
}

