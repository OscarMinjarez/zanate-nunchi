package com.adenium.zanatenunchi.ai;

import com.adenium.zanatenunchi.blackboard.BotEvent;
import com.adenium.zanatenunchi.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class OllamaClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("OllamaClient");

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static OllamaClient instance;
    private final ModConfig config;

    // CAMBIO CLAVE: Guardamos la petición activa para poder matarla si hay emergencia
    private CompletableFuture<HttpResponse<String>> activeRequest;

    private OllamaClient() {
        this.config = ModConfig.getInstance();
    }

    public static synchronized OllamaClient getInstance() {
        if (instance == null) {
            instance = new OllamaClient();
        }
        return instance;
    }

    public String callOllama(String systemPrompt, String userMessage, JsonArray history) throws Exception {
        return callOllama(systemPrompt, userMessage, history, null);
    }

    public String callOllama(String systemPrompt, String userMessage, JsonArray history, BotEvent.Impact impact) throws Exception {
        // Si entra un evento HIGH y Ollama está ocupado, abortamos la misión
        if (impact == BotEvent.Impact.HIGH && activeRequest != null && !activeRequest.isDone()) {
            LOGGER.info("¡Abortando petición anterior de Ollama para procesar EMERGENCIA!");
            activeRequest.cancel(true);
        }
        StringBuilder ctx = new StringBuilder();
        int maxHistory = config.getMaxHistoryMessages();
        // Cero historial si es emergencia. No queremos que Llama procese plática cuando ocupas sobrevivir.
        if (impact == BotEvent.Impact.HIGH) {
            maxHistory = 0;
        } else if (impact == BotEvent.Impact.NORMAL) {
            maxHistory = Math.min(maxHistory, 2);
        }
        int start = Math.max(0, history.size() - maxHistory);
        for (int i = start; i < history.size(); i++) {
            JsonObject msg = history.get(i).getAsJsonObject();
            String role = msg.get("role").getAsString();
            String text = msg.get("content").getAsString();
            if (role.equals("user")) {
                ctx.append("Jugador: ").append(text).append("\n");
            } else if (role.equals("assistant")) {
                ctx.append("Tú: ").append(text).append("\n");
            }
        }
        String fullPrompt = systemPrompt + (maxHistory > 0 ? "\n\nConversación previa:\n" + ctx : "\n\n")
                + "Jugador: " + userMessage + "\nTú:";
        JsonObject body = new JsonObject();
        body.addProperty("model", config.getOllamaModel());
        body.addProperty("prompt", fullPrompt);
        body.addProperty("stream", false);

        JsonObject options = new JsonObject();
        options.addProperty("temperature", impact == BotEvent.Impact.HIGH ? 0.2 : 0.45);
        options.addProperty("top_p", impact == BotEvent.Impact.HIGH ? 0.8 : 0.9);
        options.addProperty("repeat_penalty", 1.15);
        if (impact != null) {
            options.addProperty("num_predict", switch (impact) {
                case LOW -> 40;
                case NORMAL -> 60;
                case HIGH -> 18; // Super corto para que responda instantáneo
            });
        }
        body.add("options", options);
        int requestTimeoutSeconds = config.getOllamaTimeoutSeconds();
        if (impact != null) {
            requestTimeoutSeconds = switch (impact) {
                case LOW -> 12;
                case NORMAL -> 18;
                case HIGH -> 7; // Si no responde rápido, cortamos y tiramos Fallback
            };
        }
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(config.getOllamaApiUrl()))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)))
                .build();
        activeRequest = HTTP_CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString());
        try {
            HttpResponse<String> resp = activeRequest.join(); // Esperamos el resultado

            if (resp.statusCode() != 200) {
                LOGGER.error("Ollama respondió HTTP {}: {}", resp.statusCode(), resp.body());
                return "...";
            }
            JsonObject parsed = JsonParser.parseString(resp.body()).getAsJsonObject();
            return parsed.has("response") ? parsed.get("response").getAsString().trim() : "...";
        } catch (CancellationException e) {
            LOGGER.warn("Petición de Ollama cancelada exitosamente por un evento de mayor prioridad.");
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public String callOllamaForPersonality(String prompt) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("model", config.getOllamaModel());
        body.addProperty("prompt", prompt);
        body.addProperty("stream", false);
        body.addProperty("format", "json");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(config.getOllamaApiUrl()))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(config.getOllamaPersonalityTimeoutSeconds()))
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)))
                .build();
        HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            LOGGER.error("Ollama respondió HTTP {}: {}", resp.statusCode(), resp.body());
            return null;
        }
        JsonObject parsed = JsonParser.parseString(resp.body()).getAsJsonObject();
        return parsed.has("response") ? parsed.get("response").getAsString() : null;
    }

    public int getMaxHistory() {
        return config.getMaxHistoryMessages();
    }
}