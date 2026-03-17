package com.adenium.zanatenunchi.controller;

import com.adenium.zanatenunchi.ai.OllamaClient;
import com.adenium.zanatenunchi.ai.PromptManager;
import com.adenium.zanatenunchi.blackboard.Blackboard;
import com.adenium.zanatenunchi.blackboard.BotEvent;
import com.adenium.zanatenunchi.config.ModConfig;
import com.adenium.zanatenunchi.data.DataManager;
import com.adenium.zanatenunchi.lang.IBotLanguageProvider;
import com.adenium.zanatenunchi.util.LanguageManager;
import com.adenium.zanatenunchi.util.LanguageManager.LanguageProfile;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpTimeoutException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotController {

    private static final Logger LOGGER = LoggerFactory.getLogger("BotController");
    private static final int PROCESS_INTERVAL_TICKS = 5;

    // Cooldowns para evitar spam de la IA
    private static final long NORMAL_REACTIVE_COOLDOWN_MS = 6_000L;
    private static final long LOW_REACTIVE_COOLDOWN_MS = 12_000L;
    private static final long CRITICAL_HEALTH_REPLY_COOLDOWN_MS = 14_000L;

    // Regex para extraer datos de los eventos generados por los Observers
    private static final Pattern MULTIPLIER_COUNT_PATTERN = Pattern.compile("(\\d+)\\s*[x×]");
    private static final Pattern HEARTS_PATTERN = Pattern.compile("(\\d+)\\s+corazones");
    private static final Pattern DISTANCE_PATTERN = Pattern.compile("~(\\d+)\\s+bloques");
    private static final Pattern MOB_LIST_PAREN_PATTERN = Pattern.compile("\\(([^)]+)\\)");

    private final Blackboard blackboard;
    private final OllamaClient ollamaClient;
    private final PromptManager promptManager;
    private final DataManager dataManager;
    private final ModConfig config;

    private int tickCounter = 0;

    // Procesamiento independiente por jugador para evitar cuellos de botella
    private final Map<String, AtomicBoolean> processingMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastCriticalHealthReplyMs = new ConcurrentHashMap<>();

    public BotController(Blackboard blackboard, OllamaClient ollamaClient, PromptManager promptManager, DataManager dataManager) {
        this.blackboard = blackboard;
        this.ollamaClient = ollamaClient;
        this.promptManager = promptManager;
        this.dataManager = dataManager;
        this.config = ModConfig.getInstance();
    }

    public void register() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
        LOGGER.info("BotController registrado. Modo: Personal (Dueño: {})", config.getOwnerUUID());
    }

    private void onServerTick(MinecraftServer server) {
        if (++tickCounter % PROCESS_INTERVAL_TICKS != 0) {
            return;
        }
        if (tickCounter >= 144000) {
            tickCounter = 0;
        }
        processPendingGreetings(server);
        String owner = config.getOwnerUUID();
        boolean isOwnerFilterActive = owner != null && !owner.isEmpty();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();
            if (isOwnerFilterActive && !uuid.equals(owner)) {
                continue;
            }
            if (!blackboard.hasEventsForPlayer(player.getUUID())) {
                continue;
            }
            AtomicBoolean isProcessing = processingMap.computeIfAbsent(uuid, k -> new AtomicBoolean(false));
            if (isProcessing.compareAndSet(false, true)) {
                CompletableFuture.runAsync(() -> {
                    try {
                        processNextEventForPlayer(player);
                    } catch (Exception e) {
                        LOGGER.error("Error procesando evento para {}: {}", player.getName().getString(), e.getMessage());
                    } finally {
                        isProcessing.set(false);
                    }
                });
            }
        }
    }

    private void processNextEventForPlayer(ServerPlayer player) {
        BotEvent event = blackboard.pollEventForPlayer(player.getUUID());
        if (event == null) {
            return;
        }
        String uuid = player.getUUID().toString();
        String playerName = blackboard.getPlayerName(uuid);
        String langCode = blackboard.getPlayerLanguage(uuid);
        IBotLanguageProvider lang = LanguageManager.getProvider(langCode != null ? langCode : "es_mx");
        // Si hay una emergencia, borramos la paja de la cola.
        if (event.impact() == BotEvent.Impact.HIGH) {
            blackboard.clearNonEssentialEvents(player.getUUID());
            LOGGER.debug("Prioridad ALTA: Limpiando cola de eventos para {}", playerName);
        }

        if (!validateCooldowns(uuid, event, event.prompt())) return;

        JsonObject personality = blackboard.getPersonalityForPlayer(uuid);
        String botName = getBotNameForReply(personality);

        // REACCIÓN INMEDIATA (Sin pasar por Ollama para evitar latencia en combate)
        if (isImmediateDangerEvent(event.prompt(), event.impact())) {
            String mobs = extractMobSummary(event.prompt());
            int hearts = extractHearts(event.prompt());
            int dist = extractDistance(event.prompt());
            String immediateReply = lang.getImmediateDangerReply(mobs, hearts, dist, playerName);

            blackboard.addPlayerHistory(uuid, "assistant", immediateReply, ollamaClient.getMaxHistory());
            sendMessage(player, botName, immediateReply);
            return;
        }

        // PROCESAMIENTO CON LLM (Ollama)
        try {
            if (event.prompt().startsWith("GREETING_")) {
                handleGreeting(player, uuid, event.prompt(), lang, personality);
            } else if (event.prompt().startsWith("CHAT_NAME_RECEIVED:")) {
                handleNameAck(player, uuid, event.prompt(), lang, botName);
            } else if (event.prompt().startsWith("CHAT_MESSAGE:")) {
                handleChat(player, uuid, event.prompt().substring(13), lang, personality, botName);
            } else {
                handleGenericEvent(player, uuid, event, lang, personality, botName);
            }
        } catch (Exception e) {
            handleError(player, uuid, event, e, lang, botName, playerName);
        }
    }

    private void handleGreeting(ServerPlayer player, String uuid, String prompt, IBotLanguageProvider lang, JsonObject personality) {
        boolean isNew = prompt.contains("NEW");
        String playerName = blackboard.getPlayerName(uuid);
        // Si el jugador "existe" pero nunca dio su nombre, tratarlo como nuevo
        if (playerName == null || playerName.isBlank()) {
            isNew = true;
            playerName = null;
        }
        String traits = extractTraits(personality);
        String reply = lang.getDeterministicGreeting(playerName, isNew, traits);
        sendMessage(player, getBotNameForReply(personality), reply);
        if (isNew) blackboard.addAwaitingName(uuid);
    }

    private void handleNameAck(ServerPlayer player, String uuid, String prompt, IBotLanguageProvider lang, String botName) {
        String name = prompt.substring("CHAT_NAME_RECEIVED:".length());
        JsonObject personality = blackboard.getPersonalityForPlayer(uuid);
        String traits = extractTraits(personality);
        String reply = lang.getDeterministicGreeting(name, false, traits);
        blackboard.addPlayerHistory(uuid, "assistant", reply, ollamaClient.getMaxHistory());
        sendMessage(player, botName, reply);
        dataManager.saveData();
    }

    private void handleChat(ServerPlayer player, String uuid, String msg, IBotLanguageProvider lang, JsonObject personality, String botName) throws Exception {
        String playerName = blackboard.getPlayerName(uuid);
        String context = buildPlayerContext(player);
        LanguageProfile profile = LanguageManager.getProfile(blackboard.getPlayerLanguage(uuid));

        String system = promptManager.buildSystemPrompt(personality, blackboard.getPlayerLanguage(uuid))
                + profile.getPlayerNameContext(playerName) + context;

        blackboard.addPlayerHistory(uuid, "user", msg, ollamaClient.getMaxHistory());
        String userPrompt = "Responde al mensaje: \"" + msg + "\". Sé natural y muy breve.";
        BotEvent.Impact impact = isUrgentChatMessage(msg) ? BotEvent.Impact.HIGH : BotEvent.Impact.NORMAL;

        String reply = ollamaClient.callOllama(system, userPrompt, blackboard.getPlayerHistory(uuid), impact);
        reply = applyPostGuardrails(reply, impact, playerName, lang);

        blackboard.addPlayerHistory(uuid, "assistant", reply, ollamaClient.getMaxHistory());
        sendMessage(player, botName, reply);
        dataManager.saveData();
    }

    private void handleGenericEvent(ServerPlayer player, String uuid, BotEvent event, IBotLanguageProvider lang, JsonObject personality, String botName) throws Exception {
        String playerName = blackboard.getPlayerName(uuid);
        if (playerName == null) playerName = player.getName().getString();
        String context = buildPlayerContext(player);
        String prompt = event.prompt().replace("[nombre]", playerName);

        String system = promptManager.buildPromptWithPlayerName(personality, event.impact(), playerName, blackboard.getPlayerLanguage(uuid)) + context;

        JsonArray history = getHistoryForEvent(uuid, event);
        String reply = ollamaClient.callOllama(system, "Hecho actual: " + prompt, history, event.impact());
        reply = applyPostGuardrails(reply, event.impact(), playerName, lang);

        blackboard.addPlayerHistory(uuid, "assistant", reply, ollamaClient.getMaxHistory());
        sendMessage(player, botName, reply);
        dataManager.saveData();
    }

    private void handleError(ServerPlayer player, String uuid, BotEvent event, Exception e, IBotLanguageProvider lang, String botName, String playerName) {
        boolean isTimeout = e instanceof HttpTimeoutException || (e.getMessage() != null && e.getMessage().contains("timed out"));
        String fallback = lang.getTimeoutReply(event.impact(), playerName, event.prompt().contains("CHAT_MESSAGE"));

        blackboard.addPlayerHistory(uuid, "assistant", fallback, ollamaClient.getMaxHistory());
        sendMessage(player, botName, fallback);

        if (isTimeout) LOGGER.warn("Timeout para {}: Enviando fallback predefinido.", playerName);
        else LOGGER.error("Error en BotController: {}", e.getMessage());
    }

    private boolean validateCooldowns(String uuid, BotEvent event, String prompt) {
        if (event.neverIgnore()) return true;
        long now = System.currentTimeMillis();

        if (isCriticalHealthPrompt(prompt)) {
            if ((now - lastCriticalHealthReplyMs.getOrDefault(uuid, 0L)) < CRITICAL_HEALTH_REPLY_COOLDOWN_MS) return false;
            lastCriticalHealthReplyMs.put(uuid, now);
        }

        long lastEvent = (event.impact() == BotEvent.Impact.LOW) ? blackboard.getLastReactiveEventMs(uuid) : blackboard.getLastHighEventMs(uuid);
        long cooldown = (event.impact() == BotEvent.Impact.LOW) ? LOW_REACTIVE_COOLDOWN_MS : NORMAL_REACTIVE_COOLDOWN_MS;

        if ((now - lastEvent) < cooldown) return false;

        blackboard.setLastReactiveEventMs(uuid, now);
        return true;
    }

    private String applyPostGuardrails(String raw, BotEvent.Impact impact, String playerName, IBotLanguageProvider lang) {
        if (raw == null || raw.isBlank()) return lang.getFallbackReply(impact, playerName);
        String cleaned = raw.replaceAll("\\*[^*]*\\*", "").trim();
        if (cleaned.length() < 2) return lang.getFallbackReply(impact, playerName);
        if (playerName != null && !cleaned.toLowerCase().contains(playerName.toLowerCase())) {
            cleaned = playerName + ", " + cleaned;
        }
        // Si la frase termina bien, la dejamos
        if (cleaned.endsWith(".") || cleaned.endsWith("!") || cleaned.endsWith("?")) {
            return cleaned;
        }
        // Si la frase está cortada (por límite de tokens), buscamos el último signo de puntuación válido
        int lastPeriod = cleaned.lastIndexOf('.');
        int lastExclamation = cleaned.lastIndexOf('!');
        int lastQuestion = cleaned.lastIndexOf('?');
        int lastPunctuation = Math.max(lastPeriod, Math.max(lastExclamation, lastQuestion));
        if (lastPunctuation > 0) {
            // Cortamos hasta la última oración completa
            return cleaned.substring(0, lastPunctuation + 1).trim();
        }
        // Si no hay ninguna puntuación, simplemente le agregamos un punto final para que no se vea tan raro
        return cleaned + ".";
    }

    private boolean isImmediateDangerEvent(String prompt, BotEvent.Impact impact) {
        if (impact != BotEvent.Impact.HIGH) return false;
        String lower = prompt.toLowerCase();
        int h = extractHearts(lower);
        int d = extractDistance(lower);
        return h > 0 && (h <= 3 || (h <= 6 && d > 0 && d <= 3)) || extractMobCount(lower) >= 4;
    }

    private int extractHearts(String text) {
        Matcher m = HEARTS_PATTERN.matcher(text);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private int extractDistance(String text) {
        Matcher m = DISTANCE_PATTERN.matcher(text);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private int extractMobCount(String text) {
        int count = 0;
        Matcher m = MULTIPLIER_COUNT_PATTERN.matcher(text);
        while (m.find()) count += Integer.parseInt(m.group(1));
        return count > 0 ? count : (text.contains("hay un") ? 1 : 0);
    }

    private String extractMobSummary(String prompt) {
        Matcher m = MOB_LIST_PAREN_PATTERN.matcher(prompt);
        if (m.find()) return m.group(1);
        String l = prompt.toLowerCase();
        if (l.contains("zombi")) return "zombis";
        if (l.contains("esqueleto")) return "esqueletos";
        if (l.contains("creeper")) return "creepers";
        if (l.contains("guardián")) return "guardianes";
        return "monstruos";
    }

    private void sendMessage(ServerPlayer player, String botName, String message) {
        if (player.connection == null) return;
        String prefix = config.getBotChatPrefix();
        player.sendSystemMessage(Component.literal(prefix + botName + ": §f" + message));
    }

    private String getBotNameForReply(JsonObject personality) {
        return (personality != null && personality.has("name")) ? personality.get("name").getAsString() : "Bot";
    }

    private String extractTraits(JsonObject personality) {
        if (personality == null || !personality.has("traits")) return "";
        com.google.gson.JsonElement el = personality.get("traits");
        if (el.isJsonPrimitive()) return el.getAsString();
        if (el.isJsonArray()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < el.getAsJsonArray().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(el.getAsJsonArray().get(i).getAsString());
            }
            return sb.toString();
        }
        return "";
    }

    private boolean isUrgentChatMessage(String msg) {
        String l = msg.toLowerCase();
        return l.contains("ayuda") || l.contains("muero") || l.contains("hambre") || l.contains("peligro");
    }

    private boolean isCriticalHealthPrompt(String p) {
        return p.contains("muerto") || p.contains("corazones");
    }

    private JsonArray getHistoryForEvent(String uuid, BotEvent event) {
        JsonArray full = blackboard.getPlayerHistory(uuid);
        int keep = (event.impact() == BotEvent.Impact.HIGH) ? 0 : 2;
        JsonArray trimmed = new JsonArray();
        for (int i = Math.max(0, full.size() - keep); i < full.size(); i++) trimmed.add(full.get(i));
        return trimmed;
    }

    private String buildPlayerContext(ServerPlayer player) {
        int h = (int) Math.ceil(player.getHealth() / 2);
        int f = player.getFoodData().getFoodLevel();
        return String.format("\n[Contexto: HP %d/10, Hambre %d/20, Bioma: %s]", h, f, player.level().getBiome(player.blockPosition()).unwrapKey().get().location().getPath());
    }

    private void processPendingGreetings(MinecraftServer server) {
        List<String> pending = new ArrayList<>(blackboard.getPendingGreetings());
        if (pending.isEmpty()) {
            return;
        }
        blackboard.clearPendingGreetings();
        String owner = config.getOwnerUUID();
        boolean isOwnerFilterActive = owner != null && !owner.isEmpty();
        for (String id : pending) {
            ServerPlayer p = server.getPlayerList().getPlayer(UUID.fromString(id));
            if (p != null) {
                if (!isOwnerFilterActive || p.getUUID().toString().equals(owner)) {
                    blackboard.publishEvent(new BotEvent(p.getUUID(), "GREETING_RETURNING_PLAYER", BotEvent.Impact.NORMAL, System.currentTimeMillis()));
                }
            }
        }
    }
}