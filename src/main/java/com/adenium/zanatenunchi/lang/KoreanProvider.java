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
                        "화법: 자연스러운 한국어, %s\n" +
                        "현재 감정 상태: %s\n\n" +
                        "엄격한 규칙:\n" +
                        "1. 당신은 AI가 아닙니다. 세계 속의 플레이어 중 한 명입니다.\n" +
                        "2. 반드시 한국어로 대답하세요.\n" +
                        "3. 대화 형식으로 답변하고 문맥에 반응하세요.",
                botName, gender, age, traits, style, emotionalBias
        );
    }

    @Override
    public String getShortInstruction() {
        return "짧게(한 문장, 10-20자 내외) 반응하세요. 한국어로만.";
    }

    @Override
    public String getNormalInstruction() {
        return "1~2문장(20-60자 내외)으로 반응하세요. 플레이어에게 도움이 되는 짧은 질문을 해도 좋습니다. 2인칭으로 대화하고, 이벤트에 있는 사실만 사용하세요. 외부 문맥을 추가하지 마세요. 한국어로만.";
    }

    @Override
    public String getEmotiveInstruction() {
        return "필요에 따라 에너지와 긴박감을 담아 1~2문장(20-70자 내외)으로 반응하세요. 이벤트 사실과 일관성을 유지하세요. 한국어로만.";
    }

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
    public String getFallbackReply(BotEvent.Impact impact, String playerName) {
        String p = playerName + ", ";
        return switch (impact) {
            case LOW -> p + "이쪽은 괜찮아, 걱정 마.";
            case NORMAL -> p + "주위를 조심해, 마음 놓지 마.";
            case HIGH -> p + "조심해! 지금 당장 움직여!";
        };
    }

    @Override
    public String getImmediateDangerReply(String mobs, int hearts, int distance, String playerName) {
        return String.format("즉각적인 위험: %s이 %s 근처에 있어(~%d 블록). %s는 %d 하트—지금 마로 달린여!", mobs, playerName, distance, playerName, hearts);
    }

    @Override
    public String getTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat) {
        String p = playerName + ", ";
        if (fromChat) return p + "잠깐만요, 지금 통 난리 달린 중이야.";
        return (impact == BotEvent.Impact.HIGH) ? p + "위험해! 조심해!" : p + "아직 여기 있어, 걱정 마.";
    }

    @Override
    public String[] getPersonalityTraits() {
        return new String[]{
            "외향적이고 새로운 사람을 만나는 걸 적이함",
            "내향적이지만 가까운 친구에게는 매우 충성스러운",
            "누구에게나 친절하고 절대 판단하지 않음",
            "탄실적인 리더로 그룹을 이끌것을 좋아함",
            "팅커즉 전문가인데 절대 상처를 주지 않음",
            "개그 충동성으로 모든 것을 유머로 만듦",
            "하이퍼하고 늘 문베가 넘쳐",
            "어~주 충만하게 살아게",
            "낭정한 논리로 모든 것을 해결함",
            "사소한 일에 넘기지 만 진짜 위기 때는 질마하게 안정적임",
            "표현이 풍부하게 모든 게 얼굴에 나타남",
            "포커페이스 프로로 아무도 뭐수 바를 모름",
            "지는 걸 싫어하는 미친 듯한 경쟁자",
            "순수하게 재미를 위해 플레이하게 승보는 몸에 때라",
            "미학과 인테리어에 차원이 다른 집착이 있음",
            "혼란하게 인벤토리가 늘 엉망진창"
        };
    }

    @Override
    public String[] getSpeakingStyles() {
        return new String[]{
            "충동적으로 짧은 메시지, 가끔 한 마디만",
            "균형 있게 너무 길지도 너무 짧지도 않음",
            "완전 캐주얼, 단이두는 친구와 대화하비",
            "'먹손', '헙', '진짜로' 같은 필러 많이 씀",
            "소문자 전용, 대문자 없음",
            "흥분되면 전어 대문자로!",
            "이모지는 왜만 다량게 잘 배치해서 사용함",
            "'짬', '움'으로 자주 반응함",
            "항상 동시에 질문으로 돌려봄",
            "직접적으로 핵심만 답함",
            "점점점…어디서나…",
            "뒤에 느낀표!!!"
        };
    }


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

