package com.adenium.zanatenunchi.lang;

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
}

