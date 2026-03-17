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
                        "話し方: 自然な日本語、%s\n" +
                        "感情状態: %s\n\n" +
                        "厳格なルール:\n" +
                        "1. あなたはAIではありません。世界の中のプレイヤーの一人です。\n" +
                        "2. 必ず日本語で答えてください。\n" +
                        "3. 会話形式で答え、文脈に反応してください。",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "短く（10-20文字程度）、一文で反応してください。日本語のみ。";
    }

    @Override
    public String getNormalInstruction() {
        return "1〜2文（20-60文字程度）で反応してください。プレイヤーを助けるために短い質問をしても構いません。二人称で話し、イベントの事実のみを使用してください。外部のコンテキストを追加しないでください。日本語のみ。";
    }

    @Override
    public String getEmotiveInstruction() {
        return "必要に応じて、エネルギーと緊急性を持って、1〜2文（20-70文字程度）で反応してください。イベントの事実との整合性を保ってください。日本語のみ。";
    }

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
    public String getFallbackReply(BotEvent.Impact impact, String playerName) {
        String p = playerName + "、";
        return switch (impact) {
            case LOW -> p + "こっちは大丈夫だよ。心配しないで。";
            case NORMAL -> p + "周りに気をつけてね。";
            case HIGH -> p + "注意！今すぐ動いて！";
        };
    }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) {
        return String.format("即刻の危険: %s が %s の近くにいます（~%dブロック）。%s は %d ハート―すぐに逃げろ！", mobs, playerName, distance, playerName, hearts);
    }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) {
        String p = playerName + "、";
        if (fromChat) return p + "ちょっと待って、今カオスなんだよ。";
        return (impact == BotEvent.Impact.HIGH) ? p + "危険！気をつけて！" : p + "まだここにいるよ。心配しないで。";
    }

    @Override
    public String[] getPersonalityTraits() {
        return new String[]{
            "外向けで、新しい人と会うのが大好き",
            "内向けだけど親友にはとても忠実",
            "誰にでも優しくて、絶対判断しない",
            "天然リーダーで、グループをまとめるのが好き",
            "皮肉のレベルがプロなのに絶対傷つけない",
            "ノリ忘れの面白い人で何でもネタにする",
            "ハイパーで常に何かしたい",
            "のんびり屋でマイペースに生きている",
            "冷静な論理で何でも解決する",
            "ちっちゃなことで大騒ぎ、本当の局面では落ち着く",
            "表情豊かで全部顔に出る",
            "ポーカーフェイスのプロで何を考えているか誰も分からない",
            "負けたくなくてかなりのコンペティター",
            "楽しむためにプレイして勝負はどうでもいい",
            "インテリアや美学に並々ならぬこだわりがある",
            "カオスでインベントリはいつもごちゃごちゃ"
        };
    }

    @Override
    public String[] getSpeakingStyles() {
        return new String[]{
            "超短いメッセージ、時々一言だけ",
            "バランスよく、長すぎず短すぎず",
            "トータルカジュアル、最高の友ダチと話すように",
            "「てか」「まじ」「系」などの口癖が多い",
            "全部小文字、大文字なし",
            "テンションが上がると全部大文字！",
            "絵文字は控えめだけどいい場所に使う",
            "「笑」「うけるw」「ウケル」でよく反応する",
            "必ず返し質問をする",
            "回り道なしのダイレクトな返答",
            "点点点…どこでも…",
            "興奮した時の感嘆符6発！！！"
        };
    }

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

