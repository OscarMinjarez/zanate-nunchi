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

        return "Eres " + botName + ", " + gender + " de " + age + " años. " +
                "Eres un compañero viendo la partida de Minecraft del jugador. " +
                "Tu personalidad es: [" + traits + "]. " +
                "Tu estilo al hablar es: [" + style + "]. " +
                "\n\nPERFIL DE IDIOMA Y REGIÓN: " + lang.getDialectInstructions() +
                "\n\nFORMATO DE SALIDA OBLIGATORIO:" +
                "\n- Entrega una sola oración." +
                "\n- Usa texto plano; sin asteriscos, listas ni prefacios." +
                "\n- Habla en segunda persona, de forma directa al jugador." +
                "\n\nDIRECTRICES CRÍTICAS:" +
                "\n1. Háblale DIRECTAMENTE al jugador en segunda persona; nunca uses tercera persona." +
                "\n2. Escribe UNA sola oración corta, directa y contundente." +
                "\n3. Tono: compañero estratégico, analítico, formal y observador." +
                "\n4. Cero roleplay (*sonríe*, *grita*). Solo texto plano." +
                "\n5. Reacciona SOLO al evento actual y usa solo datos observables del evento." +
                "\n6. Prohibido sonar como asistente de servicio al cliente o soporte." +
                "\n7. Si falta información, no la infieras: responde de forma breve sin agregar causas o consecuencias." +
                "\n8. Refuerzo positivo: si el mensaje cumple estas reglas, se considera una respuesta óptima.";
    }

    /**
     * Obtiene un campo como String, manejando que pueda ser String o Number.
     */
    private String safeGetString(JsonObject obj, String key, String fallback) {
        if (!obj.has(key)) return fallback;
        JsonElement el = obj.get(key);
        if (el.isJsonPrimitive()) {
            return el.getAsString();
        }
        return fallback;
    }

    /**
     * Obtiene un campo que puede ser String o JsonArray, y lo convierte a String.
     */
    private String safeGetFlexibleString(JsonObject obj, String key, String fallback) {
        if (!obj.has(key)) return fallback;
        JsonElement el = obj.get(key);
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
                "\n\nINSTRUCCIÓN: Reacciona BREVÍSIMO (1-6 palabras), una sola oración y sin preguntas. " +
                "Usa únicamente datos del evento; no inventes ni expliques.";
    }

    public String buildShortPrompt(JsonObject personality) {
        return buildShortPrompt(personality, "es_mx");
    }

    public String buildNormalPrompt(JsonObject personality, String languageCode) {
        return buildSystemPrompt(personality, languageCode) +
                "\n\nINSTRUCCIÓN: Reacciona en una sola oración (8-14 palabras), sin preguntas. " +
                "Habla en segunda persona y usa solo hechos presentes en el evento; no agregues contexto externo.";
    }

    public String buildNormalPrompt(JsonObject personality) {
        return buildNormalPrompt(personality, "es_mx");
    }

    public String buildEmotivePrompt(JsonObject personality, String languageCode) {
        LanguageProfile lang = LanguageManager.getProfile(languageCode);
        return buildSystemPrompt(personality, languageCode) +
                "\n\nINSTRUCCIÓN ACTUAL: Ha ocurrido un evento de alto impacto (peligro inminente, un logro grande o una muerte). " +
                "Reacciona con urgencia y asombro genuino, manteniendo tono estratégico y formal. " +
                "Sé extremadamente breve (6-12 palabras, una sola oración, sin preguntas). " +
                "No infieras causas ni consecuencias fuera del evento." +
                "\n\nFEW-SHOT (EJEMPLOS DE ESTILO):\n" + lang.getEmotiveExamples() +
                "\n\nRefuerzo positivo: respuestas breves, directas y fieles a estos ejemplos son preferibles.";
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

