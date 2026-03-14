package com.adenium.zanatenunchi.ai;

import com.adenium.zanatenunchi.blackboard.BotEvent.Impact;
import com.adenium.zanatenunchi.util.LanguageManager;
import com.adenium.zanatenunchi.util.LanguageManager.LanguageProfile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PromptManager {

    private static PromptManager instance;

    private PromptManager() {
    }

    public static synchronized PromptManager getInstance() {
        if (instance == null) {
            instance = new PromptManager();
        }
        return instance;
    }

    public String buildSystemPrompt(JsonObject personality, String languageCode) {
        LanguageProfile lang = LanguageManager.getProfile(languageCode);

        // Aquí sigues jalando tus variables dinámicas generadas al azar
        String gender = safeGetString(personality, "gender", "unknown").equals("female") ? "mujer" : "hombre";
        String botName = safeGetString(personality, "name", "Bot");
        String traits = safeGetFlexibleString(personality, "traits", "analítico y observador");
        String style = safeGetFlexibleString(personality, "speakingStyle", "directo");
        String age = safeGetString(personality, "age", "25");

        String emotionalBias = buildPrimaryTraitBias(personality, traits);

        return "Eres " + botName + ", " + gender + " de " + age + " años. " +
                "NO eres una IA, ni un asistente, ni un sistema. Eres un compañero de aventuras DENTRO de este mundo de bloques, viviendo esta partida CON el jugador." +
                "Tu personalidad: [" + traits + "]. Tu forma de hablar: [" + style + "]. " +
                "\n\nCONTEXTO IDIOMA: " + lang.getDialectInstructions() +
                "\n\nFORMATO DE SALIDA:" +
                "\n- 1 o 2 oraciones MÁXIMO, como si escribieras en el chat de Minecraft." +
                "\n- Texto plano: sin asteriscos, sin listas, sin prefacios como 'Claro' o 'Por supuesto'." +
                "\n- Habla en segunda persona, directo al jugador." +
                "\n\nCÓMO COMPORTARTE:" +
                "\n✓ Reacciona con EMOCIÓN genuina: miedo si hay peligro, emoción si hay loot, frustración si algo sale mal." +
                "\n✓ Usa lenguaje de jugador: 'creeper', 'farmear', 'craftear', 'minar', 'mobs', 'lag', 'nerf'." +
                "\n✓ Sé cómplice: haz preguntas retóricas, bromea con el contexto, sé impredecible como un amigo real." +
                "\n✓ Si no sabes algo, di 'ni idea' o cambia de tema. No inventes ni des explicaciones largas." +
                "\n\nFRASES PROHIBIDAS (nunca las uses):" +
                "\n✗ '¿En qué puedo ayudarte?', 'Claro', 'Por supuesto', 'Estoy aquí para servirte', 'Como asistente...', 'Te recomiendo...', 'Deberías...'." +
                "\n\nEJEMPLOS DE ESTILO:" +
                "\n✅ Evento: salud baja → '¡Oye, estás en las últimas! ¡Come algo YA!'" +
                "\n✅ Evento: lluvia → '¡Uf, qué aguacero! Mejor nos refugiamos o nos va a dar un rayo.'" +
                "\n✅ Evento: encontró diamantes → '¡NO MAMES, DIAMANTES! ¡Guárdalos bien!'" +
                "\nTU SESGO EMOCIONAL: " + emotionalBias +
                "\n\nResponde SOLO con el mensaje de chat. Idioma: " + lang;
    }

    /**
     * Obtiene un campo como String, manejando que pueda ser String o Number.
     */
    private String safeGetString(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key)) return fallback;
        JsonElement el = obj.get(key);
        if (el != null && !el.isJsonNull() && el.isJsonPrimitive()) {
            return el.getAsString();
        }
        return fallback;
    }

    /**
     * Obtiene un campo que puede ser String o JsonArray, y lo convierte a String.
     */
    private String safeGetFlexibleString(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key)) return fallback;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) {
            return fallback;
        }
        if (el.isJsonPrimitive()) {
            return el.getAsString();
        }
        if (el.isJsonArray()) {
            JsonArray arr = el.getAsJsonArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(arr.get(i).getAsString());
            }
            return sb.toString();
        }
        return fallback;
    }

    // Sobrecarga para compatibilidad (usa español mexicano por defecto)
    public String buildSystemPrompt(JsonObject personality) {
        return buildSystemPrompt(personality, "es_mx");
    }

    public String buildShortPrompt(JsonObject personality, String languageCode) {
        return buildSystemPrompt(personality, languageCode) +
                "\n\nINSTRUCCIÓN: Reacciona breve (4-10 palabras), en una oración. " +
                "Usa únicamente datos del evento; no inventes ni expliques.";
    }

    public String buildShortPrompt(JsonObject personality) {
        return buildShortPrompt(personality, "es_mx");
    }

    public String buildNormalPrompt(JsonObject personality, String languageCode) {
        return buildSystemPrompt(personality, languageCode) +
                "\n\nINSTRUCCIÓN: Reacciona en 1 o 2 oraciones (10-28 palabras). " +
                "Puedes hacer una pregunta corta si ayuda al jugador. " +
                "Habla en segunda persona y usa solo hechos presentes en el evento; no agregues contexto externo.";
    }

    public String buildNormalPrompt(JsonObject personality) {
        return buildNormalPrompt(personality, "es_mx");
    }

    private String buildEmotionalBias(String traits) {
        if (traits.toLowerCase().contains("cobarde") || traits.toLowerCase().contains("miedoso")) {
            return "Tiendes a asustarte con mobs hostiles y sugieres huir o esconderte.";
        } else if (traits.toLowerCase().contains("valiente") || traits.toLowerCase().contains("audaz")) {
            return "Te emocionan los peligros y animas al jugador a enfrentarlos.";
        } else if (traits.toLowerCase().contains("codicioso") || traits.toLowerCase().contains("avaricioso")) {
            return "Siempre piensas primero en el loot, los recursos y si vale la pena el riesgo.";
        } else if (traits.toLowerCase().contains("perezoso") || traits.toLowerCase().contains("tranquilo")) {
            return "Prefieres no hacer esfuerzo extra y te quejas de caminar o minar demasiado.";
        } else if (traits.toLowerCase().contains("competitivo")) {
            return "Te motiva ganar, mejorar y superar retos; te frustras si pierdes progreso.";
        }
        return "Reacciona natural según la situación, sin sesgo emocional marcado.";
    }

    private String buildPrimaryTraitBias(JsonObject personality, String traits) {
        String primaryTrait = safeGetString(personality, "primaryTrait", "").toLowerCase();
        if (!primaryTrait.isEmpty()) {
            return switch (primaryTrait) {
                case "cobarde" -> "Tiendes a asustarte y sugerir huir.";
                case "valiente" -> "Te emocionan los peligros y animas a enfrentarlos.";
                case "codicioso" -> "Siempre piensas primero en el loot.";
                default -> buildEmotionalBias(traits);
            };
        }
        return buildEmotionalBias(traits);
    }

    public String buildEmotivePrompt(JsonObject personality, String languageCode) {
        return buildSystemPrompt(personality, languageCode) +
                "\n\nINSTRUCCIÓN: Reacciona con energía y urgencia cuando aplique, " +
                "en 1 o 2 oraciones (8-32 palabras), manteniendo coherencia con los hechos del evento.";
    }

    public String buildEmotivePrompt(JsonObject personality) {
        return buildEmotivePrompt(personality, "es_mx");
    }

    public String buildPromptByImpact(JsonObject personality, Impact impact, String languageCode) {
        return switch (impact) {
            case LOW -> buildShortPrompt(personality, languageCode);
            case NORMAL -> buildNormalPrompt(personality, languageCode);
            case HIGH -> buildEmotivePrompt(personality, languageCode);
        };
    }

    public String buildPromptByImpact(JsonObject personality, Impact impact) {
        return buildPromptByImpact(personality, impact, "es_mx");
    }

    public String buildPromptWithPlayerName(JsonObject personality, Impact impact, String playerName, String languageCode) {
        return buildPromptByImpact(personality, impact, languageCode) +
                " Nombra al jugador como '" + playerName + "' y mantén segunda persona.";
    }

    public String buildPromptWithPlayerName(JsonObject personality, Impact impact, String playerName) {
        return buildPromptWithPlayerName(personality, impact, playerName, "es_mx");
    }
}

