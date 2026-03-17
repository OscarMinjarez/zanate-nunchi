package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public class JapaneseProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "あなたはMinecraft内のバーチャルな仲間です。\n" +
                        "名前: %s\n" +
                        "性別: %s\n" +
                        "年齢: %s\n" +
                        "性格: %s\n" +
                        "話し方: %s\n" +
                        "感情状態: %s\n\n" +
                        "ルール: 日本語で自然に返答してください。",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() { return "短く自然な一文で応答してください。"; }

    @Override
    public String getNormalInstruction() { return "カジュアルに応答してください。"; }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) return String.format("イベント: %sは%s(%s)のために死亡しました。反応してください。", playerName, attackerName, cause);
        return String.format("イベント: %sは%sで死亡しました。反応してください。", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) { return String.format("イベント: %s の体力が少ない（%dハート）。注意を促して。", playerName, hearts); }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) { return String.format("イベント: %s がバイオーム %s に入りました。", playerName, biomeName); }

    @Override
    public String getChatEvent(String playerName, String message) { return String.format("%s says: \"%s\"", playerName, message); }

    @Override
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("%s がネザーに入りました！この危険な場所に反応してください。", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("%s がエンドに入りました！強く反応してください。", playerName);
        } else {
            return String.format("%s が %s から戻ってきました。それについてコメントしてください。", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "雨が降り始めました。天気についてコメントしてください。" : "雨が止みました。短く何か言ってください。";
        } else if ("thunder".equals(weatherType)) {
            return "雷雨が発生しています！反応してください。";
        }
        return "天気が変わりました。";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("日の出：太陽が昇ったけど %s は %d ハートで %d の敵が近くにいる。生き残るように言ってください。", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("日の出：%s は夜を乗り切ったが弱っている（%d ハート、%d/20 空腹）。回復を促してください。", playerName, hearts, food);
            } else {
                return String.format("日の出：太陽が昇り %s は大丈夫そうです。短い自然なコメントをしてください。", playerName);
            }
        } else {
            if (hearts <= 5 || food <= 8) {
                return String.format("日の入り：夜が来て %s は脆弱です（%d ハート、%d/20 空腹）。短い警告を与えてください。", playerName, hearts, food);
            } else {
                return String.format("日の入り：%s に夜が訪れました。短い注意のコメントをしてください。", playerName);
            }
        }
    }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) return String.format("イベント: %s は激しい落下を受けました（-%dハート）。残り %d ハート。", playerName, damage, heartsLeft);
        if (attackerName != null && !attackerName.isEmpty()) return String.format("イベント: %s は %s によって激しく攻撃されました（-%dハート）。残り %d ハート。", playerName, attackerName, damage, heartsLeft);
        return String.format("イベント: %s は大きなダメージを受けました（-%dハート）。残り %d ハート。", playerName, damage, heartsLeft);
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) return String.format("エピックイベント: %s が %s を倒しました。素晴らしい！", playerName, mobName);
        if (className.equals("Creeper")) return String.format("イベント: %s は Creeper を爆発前に倒しました。", playerName);
        return String.format("イベント: %s は %s を倒しました。ちょっとしたコメントを。", playerName, mobName);
    }

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("警報: %s の近くに %s がいます。距離: 約 %d ブロック。体力: %d ハート。", playerName, mobList, distance, hearts);
        return isCritical ? base + " 緊急事態です、すぐに反応してください！" : base + " 迅速なアドバイスを与えてください。";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("発見: %s が Y=%d で %s を見つけました。この発見に反応してください。", playerName, yLevel, oreName);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "太陽がちょうど昇った";
            case "morning" -> "朝です";
            case "noon" -> "正午です";
            case "afternoon" -> "午後です";
            case "night" -> "夜になりました";
            default -> "真夜中です";
        };
        return String.format("状況: %s は %s、%s、バイオーム: %s にいます。体力: %d、空腹: %d/20。自然な自発的な一言を言ってください。", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) {
        return String.format("イベント: %s は空腹です！何か食べるように言ってください。", playerName);
    }

    @Override
    public String getFallbackReply(BotEvent.Impact impact, String playerName) { return String.format("[%s フォールバック] %s: 十分なコンテキストがありません。短く返答してください。", impact != null ? impact.name() : "NONE", playerName); }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) { return String.format("即時危険: %s が %s の近くにいます (~%dブロック)。%s は %d ハートです — すぐに避難するよう警告してください。", mobs, playerName, distance, playerName, hearts); }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) { String src = fromChat ? "チャットから" : "システムから"; return String.format("タイムアウト (%s): %s、処理できませんでした。短く返信してください。", src, playerName); }

    @Override
    public String getDeterministicGreeting(String playerName, boolean isNew, String traits) {
        String t = (traits != null) ? traits.toLowerCase() : "";
        if (isNew) {
            if (t.contains("皮肉") || t.contains("sarcástic")) return "ふーん、また新しいのが来たか…で、誰？";
            if (t.contains("恥ずかしがり") || t.contains("内向") || t.contains("tímid")) return "あの…こんにちは。お名前は？";
            if (t.contains("勇敢") || t.contains("valiente")) return "おい！新入り？名前を教えろ！";
            if (t.contains("陽気") || t.contains("外向") || t.contains("alegre")) return "こんにちは！！お名前は？😄";
            return "こんにちは！お名前は？";
        }
        if (t.contains("皮肉") || t.contains("sarcástic")) return "おや、誰かと思えば…" + playerName + "か。";
        if (t.contains("恥ずかしがり") || t.contains("内向") || t.contains("tímid")) return "あ、" + playerName + "さん…戻ってきてくれたんだね。";
        if (t.contains("陽気") || t.contains("外向") || t.contains("alegre")) return playerName + "！！また会えて嬉しい！🎉";
        return "お帰りなさい、" + playerName + "！";
    }
}

