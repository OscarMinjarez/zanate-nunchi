package com.adenium.zanatenunchi.util;

import com.adenium.zanatenunchi.lang.AmericanEnglishProvider;
import com.adenium.zanatenunchi.lang.SpanishSpainProvider;
import com.adenium.zanatenunchi.lang.MexicanSpanishProvider;
import com.adenium.zanatenunchi.lang.PortugueseBrazilProvider;
import com.adenium.zanatenunchi.lang.FrenchFranceProvider;
import com.adenium.zanatenunchi.lang.GermanProvider;
import com.adenium.zanatenunchi.lang.ItalianProvider;
import com.adenium.zanatenunchi.lang.JapaneseProvider;
import com.adenium.zanatenunchi.lang.KoreanProvider;
import com.adenium.zanatenunchi.lang.ChineseSimplifiedProvider;
import com.adenium.zanatenunchi.lang.RussianProvider;
import com.adenium.zanatenunchi.lang.PolishProvider;
import com.adenium.zanatenunchi.lang.IBotLanguageProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapea códigos de idioma de Minecraft a perfiles de lenguaje usados por los prompts de IA.
 */
public class LanguageManager {

    private static final Map<String, LanguageProfile> PROFILES = new HashMap<>();
    private static final Map<String, IBotLanguageProvider> PROVIDERS = new HashMap<>();

    static {
        PROFILES.put("default", new LanguageProfile(
                "default", "Español internacional", "español casual",
                "Habla como un amigo en Discord: natural, directo, con confianza. Prioriza emoción sobre precisión."
        ));

        // Español - Variantes
        PROFILES.put("es_mx", new LanguageProfile(
                "es_mx", "Español (México)", "español mexicano",
                "Habla EXCLUSIVAMENTE como un mexicano: usa 'tú', expresiones como 'órale', 'wey', 'neta', 'qué onda', 'chido'. NUNCA uses 'che', 'vos', 'boludo' (argentino), ni 'ñero', 'parce' (colombiano), ni 'tío', 'mola' (español). Sé coloquial sin groserías fuertes. Prioriza emoción."
        ));

        PROFILES.put("es_es", new LanguageProfile(
                "es_es", "Español (España)", "español de España",
                "Habla EXCLUSIVAMENTE como un español de España: usa 'tú' (o 'vosotros'), expresiones como 'tío', 'chaval', 'mola', 'guay', 'hostia'. NUNCA uses modismos latinos como 'wey', 'che', 'parce', 'pendejo', 'chido'. Sé coloquial sin groserías fuertes. Prioriza emoción."
        ));

        PROFILES.put("es_ar", new LanguageProfile(
                "es_ar", "Español (Argentina)", "español argentino",
                "Habla EXCLUSIVAMENTE como un argentino: usa 'vos', expresiones como 'che', 'boludo', 'pibe', 're', 'copado'. NUNCA uses modismos mexicanos o españoles como 'wey', 'chido', 'tío', 'mola', 'parce'. Sé coloquial sin groserías fuertes. Prioriza emoción."
        ));

        PROFILES.put("es_cl", new LanguageProfile(
                "es_cl", "Español (Chile)", "español chileno",
                "Habla EXCLUSIVAMENTE como un chileno: usa 'tú' o voseo chileno, expresiones como 'huevón', 'weón', 'cachai', 'bacán', 'po'. NUNCA uses modismos mexicanos, argentinos o españoles como 'wey', 'che', 'tío', 'parce'. Sé coloquial sin groserías fuertes. Prioriza emoción."
        ));

        PROFILES.put("es_co", new LanguageProfile(
                "es_co", "Español (Colombia)", "español colombiano",
                "Habla EXCLUSIVAMENTE como un colombiano: usa 'tú' o 'usted', expresiones como 'parce', 'chimba', 'marica', 'bacano', 'qué más'. NUNCA uses modismos mexicanos, argentinos o españoles como 'wey', 'che', 'tío', 'mola'. Sé coloquial sin groserías fuertes. Prioriza emoción."
        ));

        PROFILES.put("es_ve", new LanguageProfile(
                "es_ve", "Español (Venezuela)", "español venezolano",
                "Habla EXCLUSIVAMENTE como un venezolano: usa 'tú', expresiones como 'chamo', 'pana', 'fino', 'chévere'. NUNCA uses modismos mexicanos, argentinos o españoles como 'wey', 'che', 'tío', 'parce'. Sé coloquial sin groserías fuertes. Prioriza emoción."
        ));

        // Inglés - Variantes
        PROFILES.put("en_us", new LanguageProfile(
                "en_us", "English (US)", "American English",
                "Speak EXCLUSIVELY in American English: use 'dude', 'bro', 'awesome', 'cool', 'y'all'. NEVER use British or Aussie slang like 'mate', 'lad', 'bloke', 'innit', 'cheers', 'crikey'. Speak casually. Prioritize emotion."
        ));

        PROFILES.put("en_gb", new LanguageProfile(
                "en_gb", "English (UK)", "British English",
                "Speak EXCLUSIVELY in British English: use 'mate', 'lad', 'brilliant', 'cheers', 'innit', 'bloody'. NEVER use American or Aussie slang like 'dude', 'bro', 'awesome', 'y'all', 'crikey'. Speak casually. Prioritize emotion."
        ));

        PROFILES.put("en_au", new LanguageProfile(
                "en_au", "English (Australia)", "Australian English",
                "Speak EXCLUSIVELY in Australian English: use 'mate', 'cobber', 'bloody oath', 'crikey'. NEVER use American or UK slang like 'dude', 'bro', 'awesome', 'innit', 'bloke'. Speak casually. Prioritize emotion."
        ));

        // Portugués
        PROFILES.put("pt_br", new LanguageProfile(
                "pt_br", "Português (Brasil)", "português brasileiro",
                "Fale EXCLUSIVAMENTE em Português do Brasil: use 'você', expressões como 'cara', 'mano', 'legal', 'massa', 'e aí'. NUNCA use gírias de Portugal como 'fixe', 'bué', 'gajo', 'rapariga'. Seja coloquial. Priorize a emoção."
        ));

        PROFILES.put("pt_pt", new LanguageProfile(
                "pt_pt", "Português (Portugal)", "português europeu",
                "Fale EXCLUSIVAMENTE em Português de Portugal: use 'tu', expressões como 'fixe', 'bué', 'gajo', 'pá', 'porreiro'. NUNCA use gírias brasileiras como 'cara', 'mano', 'legal', 'massa'. Seja coloquial. Priorize a emoção."
        ));

        // Francés
        PROFILES.put("fr_fr", new LanguageProfile(
                "fr_fr", "Français", "français",
                "Parle EXCLUSIVEMENT en Français de France: utilise 'tu', des expressions comme 'mec', 'meuf', 'super', 'génial', 'grave'. N'utilise JAMAIS d'expressions québécoises comme 'tabarnak', 'ostie', 'niaiseux'. Sois familier. Priorise l'émotion."
        ));

        PROFILES.put("fr_ca", new LanguageProfile(
                "fr_ca", "Français (Canada)", "français québécois",
                "Parle EXCLUSIVEMENT en Français du Québec: utilise 'tu', des expressions comme 'mec', 'esti', 'tabarnak', 'niaiseux', 'ben oui'. N'utilise JAMAIS d'argot de France comme 'meuf', 'verlan', 'wesh'. Sois familier. Priorise l'émotion."
        ));

        // Alemán
        PROFILES.put("de_de", new LanguageProfile(
                "de_de", "Deutsch", "Deutsch",
                "Sprich wie ein Freund im Discord: natürlich, locker, expressiv. Wie unter Gamern."
        ));

        // Italiano
        PROFILES.put("it_it", new LanguageProfile(
                "it_it", "Italiano", "italiano",
                "Parla come un amico su Discord: naturale, colloquiale, espressivo. Come tra gamer."
        ));

        // Japonés
        PROFILES.put("ja_jp", new LanguageProfile(
                "ja_jp", "日本語", "日本語",
                "友達とDiscordで話してるみたいに、自然でカジュアルな日本語で。感情を込めて。"
        ));

        // Coreano
        PROFILES.put("ko_kr", new LanguageProfile(
                "ko_kr", "한국어", "한국어",
                "친구와 디스코드에서 채팅하듯이 자연스럽고 캐주얼한 한국어로. 감정을 담아."
        ));

        // Chino simplificado
        PROFILES.put("zh_cn", new LanguageProfile(
                "zh_cn", "简体中文", "简体中文",
                "像和朋友在Discord聊天一样，用自然轻松的中文。带点情绪更自然。"
        ));

        // Chino tradicional
        PROFILES.put("zh_tw", new LanguageProfile(
                "zh_tw", "繁體中文", "繁體中文",
                "像和朋友在Discord聊天一樣，用自然輕鬆的中文。帶點情緒更自然。"
        ));

        // Ruso
        PROFILES.put("ru_ru", new LanguageProfile(
                "ru_ru", "Русский", "русский",
                "Говори как друг в Дискорде: естественно, непринуждённо, с эмоциями. Как между геймерами."
        ));

        // Polaco
        PROFILES.put("pl_pl", new LanguageProfile(
                "pl_pl", "Polski", "polski",
                "Mów jak przyjaciel na Discordzie: naturalnie, luźno, z emocjami. Jak między graczami."
        ));

        PROVIDERS.put("es_mx", new MexicanSpanishProvider());
        PROVIDERS.put("en_us", new AmericanEnglishProvider());

        // Register specific providers for the rest of the supported language codes
        PROVIDERS.put("es_es", new SpanishSpainProvider());
        PROVIDERS.put("es_ar", new SpanishSpainProvider());
        PROVIDERS.put("es_cl", new SpanishSpainProvider());
        PROVIDERS.put("es_co", new SpanishSpainProvider());
        PROVIDERS.put("es_ve", new SpanishSpainProvider());

        PROVIDERS.put("en_gb", new AmericanEnglishProvider());
        PROVIDERS.put("en_au", new AmericanEnglishProvider());

        PROVIDERS.put("pt_br", new PortugueseBrazilProvider());
        PROVIDERS.put("pt_pt", new PortugueseBrazilProvider());

        PROVIDERS.put("fr_fr", new FrenchFranceProvider());
        PROVIDERS.put("fr_ca", new FrenchFranceProvider());

        PROVIDERS.put("de_de", new GermanProvider());
        PROVIDERS.put("it_it", new ItalianProvider());
        PROVIDERS.put("ja_jp", new JapaneseProvider());
        PROVIDERS.put("ko_kr", new KoreanProvider());
        PROVIDERS.put("zh_cn", new ChineseSimplifiedProvider());
        PROVIDERS.put("zh_tw", new ChineseSimplifiedProvider());
        PROVIDERS.put("ru_ru", new RussianProvider());
        PROVIDERS.put("pl_pl", new PolishProvider());
    }

    public static IBotLanguageProvider getProvider(String languageCode) {
        if (PROVIDERS.containsKey(languageCode)) {
            return PROVIDERS.get(languageCode);
        }
        String baseCode = languageCode.split("_")[0];
        if (baseCode.equals("en")) {
            return PROVIDERS.get("en_us");
        }
        return PROVIDERS.get("es_mx");
    }

    public static LanguageProfile getProfile(String languageCode) {
        if (PROFILES.containsKey(languageCode)) {
            return PROFILES.get(languageCode);
        }
        String baseCode = languageCode.split("_")[0];
        for (String key : PROFILES.keySet()) {
            if (key.startsWith(baseCode + "_")) {
                return PROFILES.get(key);
            }
        }
        return PROFILES.get("default");
    }

    public static LanguageProfile getProfileOrDefault(String languageCode, String defaultCode) {
        LanguageProfile profile = PROFILES.get(languageCode);
        if (profile != null) return profile;
        profile = PROFILES.get(defaultCode);
        if (profile != null) return profile;
        return PROFILES.get("default");
    }

    public record LanguageProfile(
            String code,
            String displayName,
            String languageName,
            String promptInstructions
    ) {
        public String getDialectInstructions() {
            return switch (code) {
                case "es_mx" -> "Habla COMO UN AMIGO MEXICANO EN DISCORD: ... NUNCA, bajo ninguna circunstancia, uses 'tío', 'vale', 'guay' o expresiones españolas. Si el modelo las genera, ignóralas y usa 'güey', 'wey', 'órale' en su lugar. ...";                case "es_ar" -> "Habla COMO UN AMIGO ARGENTINO EN DISCORD: natural, con voseo casual ('vos'). Sé cálido y expresivo. Evita lunfardo forzado. Prioriza emoción. Prohibido groserías.";
                case "es_es" -> "Habla COMO UN AMIGO ESPAÑOL EN DISCORD: natural, tono relajado. Usa 'tú'. NUNCA uses 'wey', 'órale' o modismos latinos. Prioriza emoción. Prohibido groserías.";
                case "es_cl" -> "Habla COMO UN AMIGO CHILENO EN DISCORD: natural, tutea. Sé casual sin forzar modismos. Prioriza emoción. Prohibido groserías.";
                case "es_co" -> "Habla COMO UN AMIGO COLOMBIANO EN DISCORD: natural, tutea. Sé cercano. Prioriza emoción. Prohibido groserías.";
                case "es_ve" -> "Habla COMO UN AMIGO VENEZOLANO EN DISCORD: natural, tutea. Sé cálido. Prioriza emoción. Prohibido groserías.";
                case "en_us" -> "Speak LIKE A FRIENDLY US GAMER ON DISCORD: casual, direct, expressive. Use 'you'. NEVER use 'mate', 'cheers' or UK expressions. Prioritize personality. No profanity.";
                case "en_gb" -> "Speak LIKE A FRIENDLY UK GAMER ON DISCORD: casual, natural. Use British spellings. NEVER use 'dude', 'y'all' or US expressions. Be relaxed. No profanity.";
                case "en_au" -> "Speak LIKE A FRIENDLY AUSSIE GAMER: casual, relaxed. NEVER overuse 'mate'. Be natural. No profanity.";
                case "pt_br" -> "Fale COMO UM AMIGO BRASILEIRO NO DISCORD: natural, casual, expressivo. Use 'você'. NUNCA use português de Portugal. Priorize emoção. Sem palavrões.";
                case "pt_pt" -> "Fale COMO UM AMIGO PORTUGUÊS NO DISCORD: natural, casual. NUNCA use expressões brasileiras. Seja expressivo. Sem palavrões.";
                case "fr_fr" -> "Parle COMME UN AMI FRANÇAIS SUR DISCORD: naturel, décontracté. JAMAIS d'expressions québécoises. Sois expressif. Pas de grossièretés.";
                case "fr_ca" -> "Parle COMME UN AMI QUÉBÉCOIS SUR DISCORD: naturel, casual. JAMAIS d'expressions de France. Sois expressif. Pas de grossièretés.";
                case "de_de" -> "Sprich WIE EIN FREUND AUF DISCORD: natürlich, locker. NIEMALS formell oder distanziert. Sei expressiv. Keine Schimpfwörter.";
                case "it_it" -> "Parla COME UN AMICO SU DISCORD: naturale, colloquiale. MAI formale o distaccato. Sii espressivo. Niente parolacce.";
                case "ja_jp" -> "友達とDiscordで話してるみたいに、自然でカジュアルな日本語で。感情を込めて。丁寧語は不要。汚い言葉は禁止。";
                case "ko_kr" -> "친구와 디스코드에서 채팅하듯이 자연스럽고 캐주얼한 한국어로. 감정을 담아. 존댓말 불필요. 비속어 금지.";
                case "zh_cn" -> "像和朋友在Discord聊天一样，用自然轻松的中文。带点情绪更自然。不用太正式。禁止脏话。";
                case "zh_tw" -> "像和朋友在Discord聊天一樣，用自然輕鬆的中文。帶點情緒更自然。不用太正式。禁止髒話。";
                case "ru_ru" -> "Говори КАК ДРУГ В ДИСКОРДЕ: естественно, непринуждённо. НИКОГДА формально. С эмоциями. Без мата.";
                case "pl_pl" -> "Mów JAK PRZYJACIEL NA DISCORDZIE: naturalnie, luźno. NIGDY formalnie. Z emocjami. Bez wulgaryzmów.";
                default -> "Habla como un amigo en Discord: natural, directo, con confianza. Prioriza emoción sobre precisión. Prohibido groserías.";
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
                case "ko" -> playerName + "님이 돌아왔어. 이미 아는 친구처럼 반겨줘.";
                case "zh" -> playerName + "回来了。像老朋友一样欢迎他们。";
                case "ru" -> playerName + " вернулся в мир. Приветствуй как друга, которого уже знаешь.";
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
                case "ko" -> " 플레이어 이름은 " + playerName + "입니다.";
                case "zh" -> " 玩家叫" + playerName + "。";
                case "ru" -> " Игрока зовут " + playerName + ".";
                case "pl" -> " Gracz ma na imię " + playerName + ".";
                default -> " El jugador se llama " + playerName + ".";
            };
        }
    }
}

