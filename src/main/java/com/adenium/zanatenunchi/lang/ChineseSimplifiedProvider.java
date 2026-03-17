package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public class ChineseSimplifiedProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "你是 Minecraft 内的虚拟伙伴。\n" +
                        "名字: %s\n" +
                        "性别: %s\n" +
                        "年龄: %s\n" +
                        "特征: %s\n" +
                        "说话风格: 自然中文, %s\n" +
                        "当前情绪: %s\n\n" +
                        "严格规则:\n" +
                        "1. 你不是 AI，你是游戏世界中的一名玩家。\n" +
                        "2. 务必使用中文回答。\n" +
                        "3. 保持对话形式并对上下文做出反应。",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "简短地（一句话，约 10-20 字）做出反应。仅限中文。";
    }

    @Override
    public String getNormalInstruction() {
        return "用 1 到 2 句话（约 20-60 字）做出反应。如果对玩家有帮助，可以问一个简短的问题。以第二人称交谈，仅使用事件中的事实；不要添加外部上下文。仅限中文。";
    }

    @Override
    public String getEmotiveInstruction() {
        return "根据需要带上能量和紧迫感，用 1 到 2 句话（约 20-70 字）做出反应。保持与事件事实的一致性。仅限中文。";
    }

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

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("警报：在 %s 附近有 %s。距离：约 %d 格。生命：%d 心。", playerName, mobList, distance, hearts);
        return isCritical ? base + " 这是紧急情况，立刻反应！" : base + " 给出快速建议。";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("发现：%s 在 Y=%d 发现了 %s。对此做出反应。", playerName, yLevel, oreName);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "太阳刚刚升起";
            case "morning" -> "现在是早上";
            case "noon" -> "现在是中午";
            case "afternoon" -> "现在是下午";
            case "night" -> "夜晚已经降临";
            default -> "现在是深夜";
        };
        return String.format("情境: %s 在 %s，%s，生物群系: %s。生命: %d，饥饿: %d/20。对这种情况说些自发且自然的话。", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) {
        return String.format("事件: %s 快要饿死了！提醒他吃东西。", playerName);
    }

    @Override
    public String getFallbackReply(BotEvent.Impact impact, String playerName) {
        String p = playerName + "，";
        return switch (impact) {
            case LOW -> p + "这边没事，别担心。";
            case NORMAL -> p + "注意一下周围。";
            case HIGH -> p + "小心！现在就动！";
        };
    }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) {
        return String.format("紧急危险: %s 在 %s 附近（~%d格）。%s 剩 %d 心——马上跑！", mobs, playerName, distance, playerName, hearts);
    }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) {
        String p = playerName + "，";
        if (fromChat) return p + "等我一下，这里超乱。";
        return (impact == BotEvent.Impact.HIGH) ? p + "小心！赶紧反应！" : p + "我还在，别担心。";
    }

    @Override
    public String[] getPersonalityTraits() {
        return new String[]{
            "外向，超喜欢认识新朋友",
            "内向但对亲密朋友非常忠诚",
            "对谁都友善，从不评判别人",
            "天生领导者，喜欢组织团队",
            "讽刺的专家，但绝对不伤人",
            "强迫性的段子手，把一切变成梗",
            "超活跃，永远想做点什么",
            "超平和，顺其自然地生活",
            "用冷静的逻辑解决一切",
            "小事大惊小怪，真正危机时却非常冷静",
            "表情丰富，什么情绪都写在脸上",
            "巨型扑克脸，没人知道在想什么",
            "超级不服输，怎么都要赢",
            "纯粹为了开心来玩，输赢无所谓",
            "对建筑和美学有过人的执着",
            "一团混乱，背包永远是灾难现场"
        };
    }

    @Override
    public String[] getSpeakingStyles() {
        return new String[]{
            "超短的消息，有时只写一个字",
            "定了个概不长不短的平衡",
            "全程随途，像跟好哈肥子聊天",
            "经常用'就是''真的''也就是说'路签",
            "全用小写，不用大写",
            "兴奋时全用大写！",
            "表情包用得少但用得奋",
            "经常用'哈哈''抓狂''怕了'回应",
            "每次都会反问",
            "直接答复，不绕弯",
            "省略号…绕绕用…",
            "感叹号用个不停!!!"
        };
    }


    @Override
    public String getDeterministicGreeting(String playerName, boolean isNew, String traits) {
        String t = (traits != null) ? traits.toLowerCase() : "";
        if (isNew) {
            if (t.contains("讽刺") || t.contains("sarcástic")) return "哦，又来一个……你是谁？";
            if (t.contains("害羞") || t.contains("内向") || t.contains("tímid")) return "嗯……你好。你叫什么名字？";
            if (t.contains("勇敢") || t.contains("valiente")) return "嘿！新来的？告诉我你的名字！";
            if (t.contains("开朗") || t.contains("外向") || t.contains("alegre")) return "你好呀！！你叫什么名字？😄";
            return "你好！你叫什么名字？";
        }
        if (t.contains("讽刺") || t.contains("sarcástic")) return "看看谁回来了……" + playerName + "。";
        if (t.contains("害羞") || t.contains("内向") || t.contains("tímid")) return "哦，" + playerName + "……很高兴你回来了。";
        if (t.contains("开朗") || t.contains("外向") || t.contains("alegre")) return playerName + "！！又见到你太开心了！🎉";
        return "欢迎回来，" + playerName + "！";
    }
}

