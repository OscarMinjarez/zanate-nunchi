package com.adenium.zanatenunchi.lang;

import com.adenium.zanatenunchi.blackboard.BotEvent;

public class KoreanProvider implements IBotLanguageProvider {

    @Override
    public String buildSystemPrompt(String botName, String gender, String age, String traits, String style, String emotionalBias) {
        return String.format(
                "당신은 Minecraft 내부의 가상 동료입니다.\n" +
                        "이름: %s\n" +
                        "성별: %s\n" +
                        "나이: %s\n" +
                        "특성: %s\n" +
                        "화법: %s\n" +
                        "현재 감정 상태: %s\n\n" +
                        "규칙: 한국어로 자연스럽게 응답하세요.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() { return "매우 짧고 자연스러운 문장으로 응답하세요."; }

    @Override
    public String getNormalInstruction() { return "캐주얼하게 응답하세요."; }

    @Override
    public String getDeathEvent(String cause, String attackerName, String playerName) {
        if (attackerName != null && !attackerName.isEmpty()) return String.format("이벤트: %s가 %s(%s) 때문에 사망했습니다. 반응하세요.", playerName, attackerName, cause);
        return String.format("이벤트: %s가 %s로 사망했습니다. 반응하세요.", playerName, cause);
    }

    @Override
    public String getLowHealthEvent(String playerName, int hearts) { return String.format("이벤트: %s의 체력이 매우 낮습니다(단 %d 하트). 주의하세요.", playerName, hearts); }

    @Override
    public String getBiomeChangeEvent(String playerName, String biomeName) { return String.format("이벤트: %s가 %s 바이옴에 들어왔습니다.", playerName, biomeName); }

    @Override
    public String getChatEvent(String playerName, String message) { return String.format("%s says: \"%s\"", playerName, message); }

    @Override
    public String getDimensionChangeEvent(String playerName, String dimensionName, String previousDimension) {
        if ("the_nether".equals(dimensionName)) {
            return String.format("%s 가 Nether에 들어갔습니다! 이 위험한 장소에 반응하세요.", playerName);
        } else if ("the_end".equals(dimensionName)) {
            return String.format("%s 가 End에 들어갔습니다! 강하게 반응하세요.", playerName);
        } else {
            return String.format("%s 가 %s 에서 돌아왔습니다. 이에 대해 코멘트하세요.", playerName, previousDimension.replace("the_", "").replace("_", " "));
        }
    }

    @Override
    public String getWeatherEvent(String weatherType, boolean isStarting) {
        if ("rain".equals(weatherType)) {
            return isStarting ? "비가 오기 시작했습니다. 날씨에 대해 말하세요." : "비가 그쳤습니다. 짧게 한마디 하세요.";
        } else if ("thunder".equals(weatherType)) {
            return "천둥번개가 치고 있습니다! 반응하세요.";
        }
        return "날씨가 변했습니다.";
    }

    @Override
    public String getTimeEvent(String timeOfDay, String playerName, int hearts, int food, int nearbyHostiles) {
        if ("sunrise".equals(timeOfDay)) {
            if (nearbyHostiles >= 2) {
                return String.format("일출: 해가 떠올랐지만 %s 은(는) 하트 %d 개와 %d 개의 적이 근처에 있습니다. 살아남으라고 말하세요.", playerName, hearts, nearbyHostiles);
            } else if (hearts <= 5 || food <= 8) {
                return String.format("일출: %s 은(는) 밤을 견뎌냈지만 약합니다（%d 하트, %d/20 배고픔）。회복하라고 말하세요.", playerName, hearts, food);
            } else {
                return String.format("일출: 해가 떴고 %s 은(는) 괜찮아 보입니다. 짧고 자연스러운 코멘트를 하세요.", playerName);
            }
        } else {
            if (hearts <= 5 || food <= 8) {
                return String.format("일몰: 밤이 오고 있고 %s 은(는) 취약합니다（%d 하트, %d/20 배고픔）。짧은 경고를 주세요.", playerName, hearts, food);
            } else {
                return String.format("일몰: %s 에게 밤이 찾아왔습니다. 짧은 주의 코멘트를 하세요.", playerName);
            }
        }
    }

    @Override
    public String getDamageEvent(String playerName, String cause, String attackerName, int damage, int heartsLeft) {
        if ("fall".equals(cause)) return String.format("이벤트: %s가 강한 추락 피해를 입었습니다(-%d 하트). 남은 하트: %d.", playerName, damage, heartsLeft);
        if (attackerName != null && !attackerName.isEmpty()) return String.format("이벤트: %s가 %s에게 강하게 맞았습니다(-%d 하트). 남은 하트: %d.", playerName, attackerName, damage, heartsLeft);
        return String.format("이벤트: %s가 큰 피해를 받았습니다(-%d 하트). 남은 하트: %d.", playerName, damage, heartsLeft);
    }

    @Override
    public String getMobKillEvent(String playerName, String mobName, String className) {
        boolean isBoss = className.equals("EnderDragon") || className.equals("WitherBoss") || className.equals("ElderGuardian") || className.equals("Warden");
        if (isBoss) return String.format("에픽 이벤트: %s가 %s를 물리쳤습니다. 대단해요!", playerName, mobName);
        if (className.equals("Creeper")) return String.format("이벤트: %s가 Creeper를 폭발 전에 처치했습니다.", playerName);
        return String.format("이벤트: %s가 %s를 처치했습니다. 간단한 코멘트.", playerName, mobName);
    }

    @Override
    public String getDangerAlertEvent(String playerName, String mobList, int hearts, int distance, boolean isCritical) {
        String base = String.format("경보: %s 근처에 %s 가 있습니다. 거리: 약 %d 블록. 체력: %d 하트.", playerName, mobList, distance, hearts);
        return isCritical ? base + " 긴급 상황입니다. 즉시 반응하세요!" : base + " 빠른 조언을 해주세요.";
    }

    @Override
    public String getOreFoundEvent(String playerName, String oreName, int yLevel) {
        return String.format("발견: %s 가 Y=%d 에서 %s 를 발견했습니다. 이 발견에 반응하세요.", playerName, yLevel, oreName);
    }

    @Override
    public String getSpontaneousEvent(String playerName, String dimension, String timeKey, String biome, int hearts, int food) {
        String timeDesc = switch (timeKey) {
            case "sunrise" -> "해가 막 떠올랐습니다";
            case "morning" -> "아침입니다";
            case "noon" -> "정오입니다";
            case "afternoon" -> "오후입니다";
            case "night" -> "밤이 되었습니다";
            default -> "한밤중입니다";
        };
        return String.format("상황: %s 는 %s, %s, 바이옴: %s 에 있습니다. 체력: %d, 배고픔: %d/20. 자연스럽고 즉흥적인 한마디를 해주세요.", playerName, dimension, timeDesc, biome, hearts, food);
    }

    @Override
    public String getLowFoodEvent(String playerName) { return String.format("이벤트: %s 이(가) 배고픕니다! 먹으라고 알려주세요.", playerName); }

    @Override
    public String getFallbackReply(BotEvent.Impact impact, String playerName) { return String.format("[%s 폴백] %s: 지금은 충분한 컨텍스트가 없습니다. 간단히 답하세요.", impact != null ? impact.name() : "NONE", playerName); }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) { return String.format("즉시 위험: %s 가 %s 근처에 있습니다 (~%d 블록). %s 는 %d 하트입니다 — 즉시 대피하세요.", mobs, playerName, distance, playerName, hearts); }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) { String src = fromChat ? "채팅에서" : "시스템에서"; return String.format("타임아웃 (%s): %s, 처리하지 못했습니다. 간단히 답하세요.", src, playerName); }

    @Override
    public String getDeterministicGreeting(String playerName, boolean isNew, String traits) {
        String t = (traits != null) ? traits.toLowerCase() : "";
        if (isNew) {
            if (t.contains("비꼬") || t.contains("sarcástic")) return "오, 또 새로운 사람이네... 넌 누구야?";
            if (t.contains("수줍") || t.contains("내향") || t.contains("tímid")) return "음... 안녕하세요. 이름이 뭐예요?";
            if (t.contains("용감") || t.contains("valiente")) return "야! 새로 왔어? 이름이 뭐야!";
            if (t.contains("밝") || t.contains("외향") || t.contains("alegre")) return "안녕하세요!! 이름이 뭐예요? 😄";
            return "안녕하세요! 이름이 뭐예요?";
        }
        if (t.contains("비꼬") || t.contains("sarcástic")) return "누가 왔나 했더니... " + playerName + "이구나.";
        if (t.contains("수줍") || t.contains("내향") || t.contains("tímid")) return "아, " + playerName + "... 돌아와서 다행이에요.";
        if (t.contains("밝") || t.contains("외향") || t.contains("alegre")) return playerName + "!! 다시 만나서 너무 반가워요! 🎉";
        return "돌아와서 반가워요, " + playerName + "!";
    }
}

