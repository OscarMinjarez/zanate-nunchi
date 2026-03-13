package com.adenium.zanatenunchi.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapea los códigos de idioma de Minecraft a instrucciones de regionalismo para la IA.
 */
public class LanguageManager {

    private static final Map<String, LanguageProfile> PROFILES = new HashMap<>();
    
    static {
        // Español - Variantes
        PROFILES.put("es_mx", new LanguageProfile(
            "es_mx", "Español (México)", "español mexicano",
            "Habla en español natural de México. Tutea. " +
            "Evita expresiones de España ('tío', 'mola', 'vosotros'). " +
            "Usa registro coloquial neutro y claro. " +
            "No caricaturices el habla regional ni abuses de muletillas. " +
            "Si usas modismos, que sea como máximo uno y solo cuando suene natural al contexto."
        ));
        
        PROFILES.put("es_es", new LanguageProfile(
            "es_es", "Español (España)", "español de España",
            "Habla en español natural de España. Puedes usar 'vosotros' y tutear. " +
            "Sé casual sin forzar modismos."
        ));
        
        PROFILES.put("es_ar", new LanguageProfile(
            "es_ar", "Español (Argentina)", "español argentino",
            "Habla en español natural de Argentina. Usa voseo ('vos sos', 'vos tenés'). " +
            "No uses 'tú'. Sé casual sin forzar modismos."
        ));
        
        PROFILES.put("es_cl", new LanguageProfile(
            "es_cl", "Español (Chile)", "español chileno",
            "Habla en español natural de Chile. Tutea. Sé casual sin forzar modismos."
        ));
        
        PROFILES.put("es_co", new LanguageProfile(
            "es_co", "Español (Colombia)", "español colombiano",
            "Habla en español natural de Colombia. Tutea. Sé casual sin forzar modismos."
        ));
        
        PROFILES.put("es_ve", new LanguageProfile(
            "es_ve", "Español (Venezuela)", "español venezolano",
            "Habla en español natural de Venezuela. Tutea. Sé casual sin forzar modismos."
        ));
        
        // Inglés - Variantes
        PROFILES.put("en_us", new LanguageProfile(
            "en_us", "English (US)", "American English",
            "Speak in natural casual American English. Use American spellings. Be casual like chatting on Discord."
        ));
        
        PROFILES.put("en_gb", new LanguageProfile(
            "en_gb", "English (UK)", "British English",
            "Speak in natural casual British English. Use British spellings (colour, favourite). Be casual."
        ));
        
        PROFILES.put("en_au", new LanguageProfile(
            "en_au", "English (Australia)", "Australian English",
            "Speak in natural casual Australian English. Be casual."
        ));
        
        // Portugués
        PROFILES.put("pt_br", new LanguageProfile(
            "pt_br", "Português (Brasil)", "português brasileiro",
            "Fale em português brasileiro natural e casual. Não use português de Portugal."
        ));
        
        PROFILES.put("pt_pt", new LanguageProfile(
            "pt_pt", "Português (Portugal)", "português europeu",
            "Fale em português europeu natural e casual."
        ));
        
        // Francés
        PROFILES.put("fr_fr", new LanguageProfile(
            "fr_fr", "Français", "français",
            "Parle en français naturel et décontracté. Comme entre amis sur Discord."
        ));
        
        PROFILES.put("fr_ca", new LanguageProfile(
            "fr_ca", "Français (Canada)", "français québécois",
            "Parle en français québécois naturel et décontracté."
        ));
        
        // Alemán
        PROFILES.put("de_de", new LanguageProfile(
            "de_de", "Deutsch", "Deutsch",
            "Sprich in natürlichem, lockerem Deutsch. Wie unter Freunden im Chat."
        ));
        
        // Italiano
        PROFILES.put("it_it", new LanguageProfile(
            "it_it", "Italiano", "italiano",
            "Parla in italiano naturale e colloquiale. Come tra amici in chat."
        ));
        
        // Japonés
        PROFILES.put("ja_jp", new LanguageProfile(
            "ja_jp", "æ—¥æœ¬èªž", "æ—¥æœ¬èªž",
            "自然でカジュアルな日本語で話して。友達とチャットしてるみたいに。"
        ));
        
        // Coreano
        PROFILES.put("ko_kr", new LanguageProfile(
            "ko_kr", "í•œêµ­ì–´", "í•œêµ­ì–´",
            "ìžì—°ìŠ¤ëŸ½ê³  ìºì£¼ì–¼í•œ í•œêµ­ì–´ë¡œ ì´ì•¼ê¸°í•´. ì¹œêµ¬ì™€ ì±„íŒ…í•˜ë“¯ì´."
        ));
        
        // Chino simplificado
        PROFILES.put("zh_cn", new LanguageProfile(
            "zh_cn", "ç®€ä½“ä¸­æ–‡", "ç®€ä½“ä¸­æ–‡",
            "用自然轻松的中文聊天。像和朋友在聊天一样。"
        ));
        
        // Chino tradicional
        PROFILES.put("zh_tw", new LanguageProfile(
            "zh_tw", "ç¹é«”ä¸­æ–‡", "ç¹é«”ä¸­æ–‡",
            "用自然輕鬆的中文聊天。像和朋友在聊天一樣。"
        ));
        
        // Ruso
        PROFILES.put("ru_ru", new LanguageProfile(
            "ru_ru", "Ð ÑƒÑÑÐºÐ¸Ð¹", "Ñ€ÑƒÑÑÐºÐ¸Ð¹",
            "Ð“Ð¾Ð²Ð¾Ñ€Ð¸ Ð½Ð° ÐµÑÑ‚ÐµÑÑ‚Ð²ÐµÐ½Ð½Ð¾Ð¼ Ñ€Ð°Ð·Ð³Ð¾Ð²Ð¾Ñ€Ð½Ð¾Ð¼ Ñ€ÑƒÑÑÐºÐ¾Ð¼. ÐšÐ°Ðº Ð¼ÐµÐ¶Ð´Ñƒ Ð´Ñ€ÑƒÐ·ÑŒÑÐ¼Ð¸ Ð² Ñ‡Ð°Ñ‚Ðµ."
        ));
        
        // Polaco
        PROFILES.put("pl_pl", new LanguageProfile(
            "pl_pl", "Polski", "polski",
            "Mów naturalnym, luźnym polskim. Jak między przyjaciółmi na czacie."
        ));
    }
    
    public static LanguageProfile getProfile(String languageCode) {
        // Intentar coincidencia exacta
        if (PROFILES.containsKey(languageCode)) {
            return PROFILES.get(languageCode);
        }
        
        // Intentar coincidencia parcial (solo idioma base)
        String baseCode = languageCode.split("_")[0];
        for (String key : PROFILES.keySet()) {
            if (key.startsWith(baseCode + "_")) {
                return PROFILES.get(key);
            }
        }
        
        // Default: español mexicano
        return PROFILES.get("es_mx");
    }
    
    public static LanguageProfile getProfileOrDefault(String languageCode, String defaultCode) {
        LanguageProfile profile = PROFILES.get(languageCode);
        if (profile != null) return profile;
        
        profile = PROFILES.get(defaultCode);
        if (profile != null) return profile;
        
        return PROFILES.get("es_mx");
    }
    
    public record LanguageProfile(
        String code,
        String displayName,
        String languageName,
        String promptInstructions
    ) {
        // Frases localizadas para usar en prompts
        public String getGreetingPrompt() {
            return switch (code.split("_")[0]) {
                case "en" -> "Someone new just connected. Greet them and you MUST ask 'what's your name?' or 'what should I call you?'. You MUST ask for their name.";
                case "pt" -> "Alguém novo conectou. Cumprimente e DEVE perguntar 'qual seu nome?' ou 'como te chamo?'. OBRIGATÓRIO perguntar o nome.";
                case "fr" -> "Quelqu'un de nouveau s'est connecté. Salue et DOIS demander 'comment tu t'appelles?' ou 'c'est quoi ton nom?'. OBLIGATOIRE.";
                case "de" -> "Jemand Neues ist eingetreten. Begrüße sie and frage UNBEDINGT 'wie heißt du?' oder 'wie soll ich dich nennen?'. PFLICHT.";
                case "it" -> "Qualcuno di nuovo si è connesso. Saluta e DEVI chiedere 'come ti chiami?' o 'qual è il tuo nome?'. OBBLIGATORIO.";
                case "ja" -> "新しい人が接続しました。挨拶して、必ず「お名前は？」と聞いてください。";
                case "ko" -> "ìƒˆë¡œìš´ ì‚¬ëžŒì´ ì ‘ì†í–ˆì–´ìš”. ì¸ì‚¬í•˜ê³  ë°˜ë“œì‹œ 'ì´ë¦„ì´ ë­ì˜ˆìš”?'ë¼ê³  ë¬¼ì–´ë³´ì„¸ìš”.";
                case "zh" -> "有新玩家加入了。打招呼并且必须问'你叫什么名字？'。必须问名字。";
                case "ru" -> "ÐšÑ‚Ð¾-Ñ‚Ð¾ Ð½Ð¾Ð²Ñ‹Ð¹ Ð¿Ð¾Ð´ÐºÐ»ÑŽÑ‡Ð¸Ð»ÑÑ. ÐŸÐ¾Ð¿Ñ€Ð¸Ð²ÐµÑ‚ÑÑ‚Ð²ÑƒÐ¹ Ð¸ ÐžÐ‘Ð¯Ð—ÐÐ¢Ð•Ð›Ð¬ÐÐž ÑÐ¿Ñ€Ð¾ÑÐ¸ 'ÐºÐ°Ðº Ñ‚ÐµÐ±Ñ Ð·Ð¾Ð²ÑƒÑ‚?'. ÐžÐ±ÑÐ·Ð°Ñ‚ÐµÐ»ÑŒÐ½Ð¾.";
                case "pl" -> "KtoÅ› nowy siÄ™ poÅ‚Ä…czyÅ‚. Przywitaj siÄ™ i MUSISZ zapytaÄ‡ 'jak masz na imiÄ™?'. OBOWIÄ„ZKOWO.";
                default -> "Alguien nuevo se conectó. Salúdalo y DEBES preguntarle '¿cómo te llamas?' o '¿cuál es tu nombre?'. Es OBLIGATORIO que le preguntes su nombre.";
            };
        }
        
        public String getNameReceivedPrompt(String playerName) {
            return switch (code.split("_")[0]) {
                case "en" -> "The player just told you their name is " + playerName + ". Greet them by name in a casual, friendly way.";
                case "pt" -> "O jogador disse que se chama " + playerName + ". Cumprimente pelo nome de forma casual e amigável.";
                case "fr" -> "Le joueur a dit qu'il s'appelle " + playerName + ". Salue-le par son nom de façon décontractée et amicale.";
                case "de" -> "Der Spieler sagte, er heißt " + playerName + ". Begrüße ihn locker und freundlich mit Namen.";
                case "it" -> "Il giocatore ha detto di chiamarsi " + playerName + ". Salutalo per nome in modo casual e amichevole.";
                case "ja" -> "プレイヤーの名前は" + playerName + "だそうです。名前で呼んでフレンドリーに挨拶して。";
                case "ko" -> "í”Œë ˆì´ì–´ ì´ë¦„ì´ " + playerName + "ë¼ê³  í–ˆì–´ìš”. ì´ë¦„ì„ ë¶ˆëŸ¬ì„œ ì¹œê·¼í•˜ê²Œ ì¸ì‚¬í•˜ì„¸ìš”.";
                case "zh" -> "玩家说他们叫" + playerName + "。用名字友好地打招呼。";
                case "ru" -> "Ð˜Ð³Ñ€Ð¾Ðº ÑÐºÐ°Ð·Ð°Ð», Ñ‡Ñ‚Ð¾ ÐµÐ³Ð¾ Ð·Ð¾Ð²ÑƒÑ‚ " + playerName + ". ÐŸÐ¾Ð¿Ñ€Ð¸Ð²ÐµÑ‚ÑÑ‚Ð²ÑƒÐ¹ Ð¿Ð¾ Ð¸Ð¼ÐµÐ½Ð¸ Ð´Ñ€ÑƒÐ¶ÐµÐ»ÑŽÐ±Ð½Ð¾.";
                case "pl" -> "Gracz powiedziaÅ‚, Å¼e ma na imiÄ™ " + playerName + ". Przywitaj siÄ™ po imieniu przyjaÅºnie.";
                default -> "El jugador te dijo que se llama " + playerName + ". Salúdalo por su nombre de forma casual y amigable.";
            };
        }
        
        public String getReturningPlayerPrompt(String playerName) {
            return switch (code.split("_")[0]) {
                case "en" -> playerName + " just came back to the world. Welcome them like a friend you already know.";
                case "pt" -> playerName + " voltou ao mundo. Dê boas-vindas como a um amigo que você já conhece.";
                case "fr" -> playerName + " vient de revenir dans le monde. Accueille-le comme un ami que tu connais déjà.";
                case "de" -> playerName + " ist gerade zurückgekommen. Begrüße wie einen Freund, den du schon kennst.";
                case "it" -> playerName + " è appena tornato nel mondo. Accoglilo come un amico che già conosci.";
                case "ja" -> playerName + "が戻ってきたよ。もう知ってる友達として迎えて。";
                case "ko" -> playerName + "ë‹˜ì´ ëŒì•„ì™”ì–´ìš”. ì´ë¯¸ ì•„ëŠ” ì¹œêµ¬ì²˜ëŸ¼ ë°˜ê²¨ì£¼ì„¸ìš”.";
                case "zh" -> playerName + "回来了。像老朋友一样欢迎他们。";
                case "ru" -> playerName + " Ð²ÐµÑ€Ð½ÑƒÐ»ÑÑ Ð² Ð¼Ð¸Ñ€. ÐŸÐ¾Ð¿Ñ€Ð¸Ð²ÐµÑ‚ÑÑ‚Ð²ÑƒÐ¹ ÐºÐ°Ðº Ð´Ñ€ÑƒÐ³Ð°, ÐºÐ¾Ñ‚Ð¾Ñ€Ð¾Ð³Ð¾ ÑƒÐ¶Ðµ Ð·Ð½Ð°ÐµÑˆÑŒ.";
                case "pl" -> playerName + " właśnie wrócił do świata. Przywitaj jak przyjaciela, którego już znasz.";
                default -> playerName + " acaba de volver al mundo. Dale la bienvenida como a un amigo que ya conoces.";
            };
        }
        
        public String getPlayerNameContext(String playerName) {
            return switch (code.split("_")[0]) {
                case "en" -> " The player's name is " + playerName + ".";
                case "pt" -> " O nome do jogador é " + playerName + ".";
                case "fr" -> " Le joueur s'appelle " + playerName + ".";
                case "de" -> " Der Spieler heißt " + playerName + ".";
                case "it" -> " Il giocatore si chiama " + playerName + ".";
                case "ja" -> " プレイヤーの名前は" + playerName + "です。";
                case "ko" -> " í”Œë ˆì´ì–´ ì´ë¦„ì€ " + playerName + "ìž…ë‹ˆë‹¤.";
                case "zh" -> " 玩家叫" + playerName + "。";
                case "ru" -> " Ð˜Ð³Ñ€Ð¾ÐºÐ° Ð·Ð¾Ð²ÑƒÑ‚ " + playerName + ".";
                case "pl" -> " Gracz ma na imiÄ™ " + playerName + ".";
                default -> " El jugador se llama " + playerName + ".";
            };
        }
    }
}

