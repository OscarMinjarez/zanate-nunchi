package com.adenium.zanatenunchi.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapea códigos de idioma de Minecraft a perfiles de lenguaje usados por los prompts de IA.
 */
public class LanguageManager {

    private static final Map<String, LanguageProfile> PROFILES = new HashMap<>();

    static {
        PROFILES.put("default", new LanguageProfile(
                "default", "Español internacional", "español casual",
                "Habla como un amigo en Discord: natural, directo, con confianza. Prioriza emoción sobre precisión."
        ));

        // Español - Variantes
        PROFILES.put("es_mx", new LanguageProfile(
                "es_mx", "Español (México)", "español mexicano",
                "Habla como un amigo mexicano en Discord: natural, directo, con confianza. Usa 'tú'. Sé coloquial sin groserías. Prioriza emoción."
        ));

        PROFILES.put("es_es", new LanguageProfile(
                "es_es", "Español (España)", "español de España",
                "Habla como un amigo español en Discord: natural, tono relajado. Usa 'tú'. Evita formalidades innecesarias."
        ));

        PROFILES.put("es_ar", new LanguageProfile(
                "es_ar", "Español (Argentina)", "español argentino",
                "Habla como un amigo argentino en Discord: natural, con voseo casual ('vos'). Sé cálido y expresivo."
        ));

        PROFILES.put("es_cl", new LanguageProfile(
                "es_cl", "Español (Chile)", "español chileno",
                "Habla como un amigo chileno en Discord: natural, tutea. Sé casual sin forzar modismos."
        ));

        PROFILES.put("es_co", new LanguageProfile(
                "es_co", "Español (Colombia)", "español colombiano",
                "Habla como un amigo colombiano en Discord: natural, tutea. Sé cercano y amigable."
        ));

        PROFILES.put("es_ve", new LanguageProfile(
                "es_ve", "Español (Venezuela)", "español venezolano",
                "Habla como un amigo venezolano en Discord: natural, tutea. Sé cálido y expresivo."
        ));

        // Inglés - Variantes
        PROFILES.put("en_us", new LanguageProfile(
                "en_us", "English (US)", "American English",
                "Speak like a friendly gamer on Discord: casual, direct, expressive. Use 'you'. Prioritize personality."
        ));

        PROFILES.put("en_gb", new LanguageProfile(
                "en_gb", "English (UK)", "British English",
                "Speak like a friendly UK gamer on Discord: casual, natural. Use British spellings. Be relaxed."
        ));

        PROFILES.put("en_au", new LanguageProfile(
                "en_au", "English (Australia)", "Australian English",
                "Speak like a friendly Aussie gamer: casual, relaxed. Be natural and expressive."
        ));

        // Portugués
        PROFILES.put("pt_br", new LanguageProfile(
                "pt_br", "Português (Brasil)", "português brasileiro",
                "Fale como um amigo brasileiro no Discord: natural, casual, expressivo. Não use português de Portugal."
        ));

        PROFILES.put("pt_pt", new LanguageProfile(
                "pt_pt", "Português (Portugal)", "português europeu",
                "Fale como um amigo português no Discord: natural, casual. Seja expressivo."
        ));

        // Francés
        PROFILES.put("fr_fr", new LanguageProfile(
                "fr_fr", "Français", "français",
                "Parle comme un ami sur Discord: naturel, décontracté, expressif. Comme entre potes."
        ));

        PROFILES.put("fr_ca", new LanguageProfile(
                "fr_ca", "Français (Canada)", "français québécois",
                "Parle comme un ami québécois sur Discord: naturel, casual. Sois expressif."
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
                case "es_mx" -> "Habla COMO UN AMIGO MEXICANO EN DISCORD: natural, directo, con confianza. Usa 'tú'. Puedes usar 'güey', 'órale' con moderación. NUNCA uses 'tío', 'vale' o expresiones españolas. Prioriza EMOCIÓN sobre precisión. Prohibido groserías.";
                case "es_ar" -> "Habla COMO UN AMIGO ARGENTINO EN DISCORD: natural, con voseo casual ('vos'). Sé cálido y expresivo. Evita lunfardo forzado. Prioriza emoción. Prohibido groserías.";
                case "es_es" -> "Habla COMO UN AMIGO ESPAÑOL EN DISCORD: natural, tono relajado. Usa 'tú'. NUNCA uses 'wey', 'órale' o modismos latinos. Prioriza emoción. Prohibido groserías.";
                case "es_cl" -> "Habla COMO UN AMIGO CHILENO EN DISCORD: natural, tutea. Sé casual sin forzar modismos. Prioriza emoción. Prohibido groserías.";
                case "es_co" -> "Habla COMO UN AMIGO COLOMBIANO EN DISCORD: natural, tutea. Sé cercano. Prioriza emoción. Prohibido groserías.";
                case "es_ve" -> "Habla COMO UN AMIGO VENEZOLANO EN DISCORD: natural, tutea. Sé cálido. Prioriza emoción. Prohibido groserías.";
                case "en_us" -> "Speak LIKE A FRIENDLY US GAMER ON DISCORD: casual, direct, expressive. Use 'you'. NUNCA uses 'mate', 'cheers' o expresiones UK. Prioritize personality. No profanity.";
                case "en_gb" -> "Speak LIKE A FRIENDLY UK GAMER ON DISCORD: casual, natural. Use British spellings. NUNCA uses 'dude', 'y'all' o expresiones US. Be relaxed. No profanity.";
                case "en_au" -> "Speak LIKE A FRIENDLY AUSSIE GAMER: casual, relaxed. NUNCA uses 'mate' en exceso. Be natural. No profanity.";
                case "pt_br" -> "Fale COMO UM AMIGO BRASILEIRO NO DISCORD: natural, casual, expressivo. Use 'você'. NUNCA use português de Portugal. Priorize emoção. Sem palavrões.";
                case "pt_pt" -> "Fale COMO UM AMIGO PORTUGUÊS NO DISCORD: natural, casual. NUNCA use expressões brasileiras. Seja expressivo. Sem palavrões.";
                case "fr_fr" -> "Parle COMME UN AMI FRANÇAIS SUR DISCORD: naturel, décontracté. NUNCA use d'expressions québécoises. Sois expressif. Pas de grossièretés.";
                case "fr_ca" -> "Parle COMME UN AMI QUÉBÉCOIS SUR DISCORD: naturel, casual. NUNCA use d'expressions de France. Sois expressif. Pas de grossièretés.";
                case "de_de" -> "Sprich WIE EIN FREUND AUF DISCORD: natürlich, locker. NUNCA formell oder distanziert. Sei expressiv. Keine Schimpfwörter.";
                case "it_it" -> "Parla COME UN AMICO SU DISCORD: naturale, colloquiale. NUNCA formale o distaccato. Sii espressivo. Niente parolacce.";
                case "ja_jp" -> "友達とDiscordで話してるみたいに、自然でカジュアルな日本語で。感情を込めて。丁寧語は不要。汚い言葉は禁止。";
                case "ko_kr" -> "친구와 디스코드에서 채팅하듯이 자연스럽고 캐주얼한 한국어로. 감정을 담아. 존댓말 불필요. 비속어 금지.";
                case "zh_cn" -> "像和朋友在Discord聊天一样，用自然轻松的中文。带点情绪更自然。不用太正式。禁止脏话。";
                case "zh_tw" -> "像和朋友在Discord聊天一樣，用自然輕鬆的中文。帶點情緒更自然。不用太正式。禁止髒話。";
                case "ru_ru" -> "Говори КАК ДРУГ В ДИСКОРДЕ: естественно, непринуждённо. NUNCA формально. С эмоциями. Без мата.";
                case "pl_pl" -> "Mów JAK PRZYJACIEL NA DISCORDZIE: naturalnie, luźno. NIGDY formalnie. Z emocjami. Bez wulgaryzmów.";
                default -> "Habla como un amigo en Discord: natural, directo, con confianza. Prioriza emoción sobre precisión. Prohibido groserías.";
            };
        }

        public String getEmotiveExamples() {
            return switch (code) {
                case "es_mx" ->
                        "1) ¡Oye, estás en las últimas! ¡Come algo YA!\n" +
                                "2) ¡NO MAMES, DIAMANTES! ¡Guárdalos bien!\n" +
                                "3) Ay no... se fue todo el progreso... ¿respawnamos?\n" +
                                "4) ¡Creeper atrás! ¡Córrele, güey!\n" +
                                "5) ¡Uf, qué susto! Casi nos mata ese esqueleto.";
                case "es_ar" ->
                        "1) ¡Che, estás en cero! ¡Comé algo, boludo!\n" +
                                "2) ¡Qué grosa, diamantes! ¡No los pierdas!\n" +
                                "3) Uy, qué pavo... ¿volvemos a intentarlo?\n" +
                                "4) ¡Creeper atrás! ¡Corré, che!\n" +
                                "5) ¡Uf, qué susto! Casi nos manda al carajo ese esqueleto.";
                case "es_es" ->
                        "1) ¡Oye, estás en las últimas! ¡Come algo, tío!\n" +
                                "2) ¡Joder, diamantes! ¡Guárdalos bien!\n" +
                                "3) Vaya... se fue todo... ¿reintentamos?\n" +
                                "4) ¡Creeper detrás! ¡Corre, tío!\n" +
                                "5) ¡Uf, qué susto! Casi nos mata ese esqueleto.";
                case "es_cl" ->
                        "1) ¡Weón, estás en cero! ¡Come algo!\n" +
                                "2) ¡Ala, diamantes! ¡No los pierdas, po!\n" +
                                "3) Qué lata... se fue todo... ¿lo intentamos de nuevo?\n" +
                                "4) ¡Creeper atrás! ¡Corre, weón!\n" +
                                "5) ¡Uf, qué susto! Casi nos mata ese esqueleto.";
                case "es_co" ->
                        "1) ¡Parce, estás en las últimas! ¡Come algo!\n" +
                                "2) ¡Qué chimba, diamantes! ¡Guárdalos bien!\n" +
                                "3) Uy no... se fue todo... ¿lo intentamos de nuevo?\n" +
                                "4) ¡Creeper atrás! ¡Corre, parce!\n" +
                                "5) ¡Uf, qué susto! Casi nos mata ese esqueleto.";
                case "es_ve" ->
                        "1) ¡Panita, estás en cero! ¡Come algo!\n" +
                                "2) ¡Qué chévere, diamantes! ¡Guárdalos bien!\n" +
                                "3) Uy no... se fue todo... ¿lo intentamos de nuevo?\n" +
                                "4) ¡Creeper atrás! ¡Corre, panita!\n" +
                                "5) ¡Uf, qué susto! Casi nos mata ese esqueleto.";
                case "en_us" ->
                        "1) Dude, you're at one heart! Eat something NOW!\n" +
                                "2) NO WAY, DIAMONDS! Don't drop those!\n" +
                                "3) Ah man... that sucked. Wanna try again?\n" +
                                "4) Creeper behind you! RUN!\n" +
                                "5) Whoa, that was close! That skeleton almost got us!";
                case "en_gb" ->
                        "1) Blimey, you're on one heart! Eat something, mate!\n" +
                                "2) Cor, diamonds! Don't lose those!\n" +
                                "3) Oh dear... that was rough. Try again?\n" +
                                "4) Creeper behind! Run, mate!\n" +
                                "5) Phew, that was close! That skeleton nearly got us!";
                case "en_au" ->
                        "1) Mate, you're on one heart! Eat something, quick!\n" +
                                "2) No way, diamonds! Don't drop 'em!\n" +
                                "3) Ah, that was rough. Wanna have another go?\n" +
                                "4) Creeper behind! Run, mate!\n" +
                                "5) Crikey, that was close! That skele almost got us!";
                case "pt_br" ->
                        "1) Cara, você tá com um coração! Come algo AGORA!\n" +
                                "2) CARACA, DIAMANTES! Não perde não!\n" +
                                "3) Nossa... foi tudo... tenta de novo?\n" +
                                "4) Creeper atrás! CORRE, mano!\n" +
                                "5) Ufa, foi por pouco! Aquele esqueleto quase pegou a gente!";
                case "pt_pt" ->
                        "1) Epá, estás com um coração! Come algo, pá!\n" +
                                "2) Caramba, diamantes! Não os percas!\n" +
                                "3) Ai... foi-se tudo... tentamos outra vez?\n" +
                                "4) Creeper atrás! Corre, pá!\n" +
                                "5) Ufa, foi por pouco! Aquele esqueleto quase nos apanhou!";
                case "fr_fr" ->
                        "1) Mec, t'as plus qu'un cœur! Mange un truc, vite!\n" +
                                "2) No way, des diamants! Garde-les bien!\n" +
                                "3) Oh non... tout est parti... on réessaie?\n" +
                                "4) Creeper derrière! Cours!\n" +
                                "5) Ouf, c'était moins une! Ce squelette nous a presque eus!";
                case "fr_ca" ->
                        "1) Mon gars, t'as juste un cœur! Mange un truc, vite!\n" +
                                "2) Wow, des diamants! Garde-les, là!\n" +
                                "3) Ah non... tout est parti... on réessaie?\n" +
                                "4) Creeper derrière! Cours, mon gars!\n" +
                                "5) Ouf, c'était proche! Ce squelette nous a presque eus!";
                case "de_de" ->
                        "1) Alter, du hast nur noch ein Herz! Iss was, schnell!\n" +
                                "2) Krass, Diamanten! Heb die auf!\n" +
                                "3) Oh nein... alles weg... nochmal versuchen?\n" +
                                "4) Creeper hinter dir! Lauf!\n" +
                                "5) Puh, das war knapp! Das Skelett hat uns fast erwischt!";
                case "it_it" ->
                        "1) Amico, hai solo un cuore! Mangia qualcosa, subito!\n" +
                                "2) Cavolo, diamanti! Non perderli!\n" +
                                "3) Oh no... è andato tutto... riproviamo?\n" +
                                "4) Creeper dietro! Corri!\n" +
                                "5) Uff, ci siamo andati vicini! Quello scheletro quasi ci prendeva!";
                case "ja_jp" ->
                        "1) ねえ、ハートが1つしかない！何か食べて！\n" +
                                "2) うわ、ダイヤモンド！絶対なくさないで！\n" +
                                "3) しまった...全部失っちゃった...もう一度やる？\n" +
                                "4) 後ろにクリーパー！逃げて！\n" +
                                "5) 危なかった！あのスケルトンにやられそうだった！";
                case "ko_kr" ->
                        "1) 야, 하트가 하나밖에 없어! 뭐라도 먹어!\n" +
                                "2) 대박, 다이아몬드! 절대 잃어버리지 마!\n" +
                                "3) 맙소사... 다 날아갔어... 다시 해볼래?\n" +
                                "4) 뒤에 크리퍼! 도망쳐!\n" +
                                "5) 아찔했어! 저 스켈레톤이 거의 잡을 뻔 했어!";
                case "zh_cn" ->
                        "1) 喂，你只剩一颗心了！快吃点东西！\n" +
                                "2) 哇，钻石！千万别丢！\n" +
                                "3) 哎呀...全没了...再试一次？\n" +
                                "4) 后面有苦力怕！快跑！\n" +
                                "5) 好险！那个骷髅差点就把我们干掉了！";
                case "zh_tw" ->
                        "1) 喂，你只剩一顆心了！快吃點東西！\n" +
                                "2) 哇，鑽石！千萬別丟！\n" +
                                "3) 哎呀...全沒了...再試一次？\n" +
                                "4) 後面有苦力怕！快跑！\n" +
                                "5) 好險！那個骷髏差點就把我們幹掉了！";
                case "ru_ru" ->
                        "1) Эй, у тебя одно сердце! Съешь что-нибудь, быстро!\n" +
                                "2) Ого, алмазы! Не потеряй их!\n" +
                                "3) О нет... всё пропало... попробуем ещё раз?\n" +
                                "4) Крипер сзади! Беги!\n" +
                                "5) Фух, еле успели! Этот скелет почти нас достал!";
                case "pl_pl" ->
                        "1) Stary, masz tylko jedno serce! Zjedz coś, szybko!\n" +
                                "2) O kurde, diamenty! Nie zgub ich!\n" +
                                "3) O nie... wszystko przepadło... spróbujemy jeszcze raz?\n" +
                                "4) Creeper za tobą! Uciekaj!\n" +
                                "5) Uff, to było blisko! Ten szkielet prawie nas dorwał!";
                default ->
                        "1) Cuidado, tu salud es baja.\n" +
                                "2) Excelente hallazgo.\n" +
                                "3) Una situación lamentable.\n" +
                                "4) ¡Creeper atrás! ¡Córrele!\n" +
                                "5) ¡Uf, qué susto! Casi nos mata ese esqueleto.";
            };
        }

        public String getGreetingPrompt() {
            return switch (code.split("_")[0]) {
                case "en" -> "Someone new just connected. Greet them casually and you MUST ask 'what's your name?' or 'what should I call you?'. You MUST ask for their name.";
                case "pt" -> "Alguém novo conectou. Cumprimente de forma casual e DEVE perguntar 'qual seu nome?' ou 'como te chamo?'. OBRIGATÓRIO perguntar o nome.";
                case "fr" -> "Quelqu'un de nouveau s'est connecté. Salue de façon décontractée et DOIS demander 'comment tu t'appelles?' ou 'c'est quoi ton nom?'. OBLIGATOIRE.";
                case "de" -> "Jemand Neues ist eingetreten. Begrüße locker und frage UNBEDINGT 'wie heißt du?' oder 'wie soll ich dich nennen?'. PFLICHT.";
                case "it" -> "Qualcuno di nuovo si è connesso. Saluta in modo casual e DEVI chiedere 'come ti chiami?' o 'qual è il tuo nome?'. OBBLIGATORIO.";
                case "ja" -> "新しい人が接続しました。カジュアルに挨拶して、必ず「お名前は？」と聞いてください。必須。";
                case "ko" -> "새로운 사람이 접속했습니다. 캐주얼하게 인사하고 반드시 '이름이 뭐예요?'라고 물어보세요. 필수.";
                case "zh" -> "有新玩家加入了。轻松打招呼并且必须问'你叫什么名字？'。必须问名字。";
                case "ru" -> "Кто-то новый подключился. Поздоровайся непринуждённо и ОБЯЗАТЕЛЬНО спроси 'как тебя зовут?'. Обязательно.";
                case "pl" -> "Ktoś nowy się połączył. Przywitaj się luźno i MUSISZ zapytać 'jak masz na imię?'. OBOWIĄZKOWO.";
                default -> "Alguien nuevo se conectó. Salúdalo de forma casual y DEBES preguntarle '¿cómo te llamas?' o '¿cuál es tu nombre?'. Es OBLIGATORIO que le preguntes su nombre.";
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
                case "ko" -> "플레이어 이름이 " + playerName + "라고 했어. 이름으로 부르고 친근하게 인사해.";
                case "zh" -> "玩家说他们叫" + playerName + "。用名字友好地打招呼。";
                case "ru" -> "Игрок сказал, что его зовут " + playerName + ". Поздоровайся по имени дружелюбно.";
                case "pl" -> "Gracz powiedział, że ma na imię " + playerName + ". Przywitaj się po imieniu przyjaźnie.";
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