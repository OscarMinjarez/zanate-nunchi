package com.adenium.zanatenunchi.controller;

import com.adenium.zanatenunchi.ai.OllamaClient;
import com.adenium.zanatenunchi.ai.PromptManager;
import com.adenium.zanatenunchi.blackboard.Blackboard;
import com.adenium.zanatenunchi.blackboard.BotEvent;
import com.adenium.zanatenunchi.config.ModConfig;
import com.adenium.zanatenunchi.data.DataManager;
import com.adenium.zanatenunchi.util.LanguageManager;
import com.adenium.zanatenunchi.util.LanguageManager.LanguageProfile;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotController {

    private static final Logger LOGGER = LoggerFactory.getLogger("BotController");
    private static final int PROCESS_INTERVAL_TICKS = 5;
    private static final long NORMAL_REACTIVE_COOLDOWN_MS = 6_000L;
    private static final long LOW_REACTIVE_COOLDOWN_MS = 12_000L;
    private static final Set<String> EVENT_FACT_TERMS = new HashSet<>(Arrays.asList(
            "creeper", "zombi", "zombie", "esqueleto", "skeleton", "araña", "arana", "spider",
            "warden", "wither", "dragon", "dragón", "ender dragon", "guardian", "elder guardian",
            "villager", "aldeano", "golem", "gólem", "drown", "ahogo", "ahogo", "ahogo", "ahogo",
            "ahog", "lava", "fuego", "quem", "caida", "cayo", "caer", "vacio", "vacío",
            "explosion", "explosión", "flecha", "arrow", "hambre", "starve", "rayo", "lightning",
            "cactus", "magma", "congel", "freeze", "wither", "magic", "magia"
    ));
    private static final List<String> SPECULATION_MARKERS = Arrays.asList(
            "o algo", "me parece que", "parece que", "seguro que", "quizá", "quizas", "tal vez", "a lo mejor"
    );
    private static final List<String> ASSISTANT_TONE_MARKERS = Arrays.asList(
            "como ia", "como asistente", "en que puedo ayudarte", "servicio al cliente", "estoy aqui para ayudarte"
    );
    private static final List<String> URGENT_CHAT_TERMS = Arrays.asList(
            "ayuda", "me muero", "casi muerto", "socorro", "creeper", "esqueleto", "zombi", "zombie",
            "araña", "arana", "me pegan", "sin vida", "1 corazon", "un corazon", "hambre"
    );
    private static final Pattern MULTIPLIER_COUNT_PATTERN = Pattern.compile("(\\d+)\\s*[x×]");
    private static final Pattern HEARTS_PATTERN = Pattern.compile("tiene\\s+(\\d+)\\s+corazones");
    private static final Pattern DISTANCE_PATTERN = Pattern.compile("~(\\d+)\\s+bloques");
    private static final Pattern MOB_LIST_PAREN_PATTERN = Pattern.compile("\\(([^)]+)\\)");
    private static final Pattern MOB_SINGULAR_PATTERN = Pattern.compile("hay un\\s+([a-záéíóúñ]+)|hay una\\s+([a-záéíóúñ]+)");
    private static final int RECENT_REPLY_WINDOW = 3;
    private static final long CRITICAL_HEALTH_REPLY_COOLDOWN_MS = 14_000L;

    private final Blackboard blackboard;
    private final OllamaClient ollamaClient;
    private final PromptManager promptManager;
    private final DataManager dataManager;
    private final ModConfig config;

    private int tickCounter = 0;
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final Map<String, Deque<String>> recentRepliesByPlayer = new ConcurrentHashMap<>();
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
        LOGGER.info("BotController registrado");
    }

    private void onServerTick(MinecraftServer server) {
        int tick = ++tickCounter;
        if (tick % PROCESS_INTERVAL_TICKS != 0) return;

        if (tickCounter >= 144000) {
            tickCounter = 0;
        }

        // Procesar saludos pendientes que quedaron esperando a la personalidad
        processPendingGreetings(server);

        if (!blackboard.hasEvents()) return;

        if (processing.compareAndSet(false, true)) {
            CompletableFuture.runAsync(() -> {
                try {
                    processNextEvent();
                } catch (Exception e) {
                    LOGGER.error("Error no manejado procesando evento: {}", e.getMessage(), e);
                } finally {
                    processing.set(false);
                }
            });
        } else {
            LOGGER.debug("Ya hay un procesamiento en curso, esperando...");
        }
    }

    private void processPendingGreetings(MinecraftServer server) {
        var pending = blackboard.getPendingGreetings();
        if (pending.isEmpty()) return;
        
        blackboard.clearPendingGreetings();
        
        for (String uuid : pending) {
            try {
                ServerPlayer player = server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
                if (player != null && player.connection != null) {
                    var event = new BotEvent(
                            player.getUUID(),
                            "GREETING_NEW_PLAYER",
                            BotEvent.Impact.HIGH,
                            System.currentTimeMillis(),
                            true
                    );
                    blackboard.publishEvent(event);
                    LOGGER.info("Saludo pendiente enviado a cola para: {}", player.getName().getString());
                } else {
                    // El jugador aún no está listo, volver a intentar
                    blackboard.addPendingGreeting(uuid);
                }
            } catch (Exception e) {
                LOGGER.debug("Error procesando saludo pendiente: {}", e.getMessage());
            }
        }
    }

    private void processNextEvent() {
        BotEvent event = blackboard.pollEvent();
        if (event == null) {
            LOGGER.debug("No hay eventos en la cola");
            return;
        }

        String uuid = event.playerUuid().toString();
        LOGGER.info("Procesando evento: [{}] para jugador {}", event.prompt(), uuid);

        MinecraftServer server = blackboard.getCurrentServer();
        if (server == null) {
            LOGGER.warn("Server es null, reponiendo evento");
            blackboard.publishEvent(event);
            return;
        }

        ServerPlayer player = server.getPlayerList().getPlayer(event.playerUuid());
        if (player == null) {
            LOGGER.warn("Player es null para UUID {}, descartando evento", uuid);
            return;
        }

        String prompt = event.prompt();
        boolean highEvent = event.impact() == BotEvent.Impact.HIGH;
        if (!highEvent && !prompt.startsWith("GREETING_") && !prompt.startsWith("CHAT_NAME_RECEIVED:")
                && blackboard.hasHighPendingOrProcessing(event.playerUuid())) {
            LOGGER.debug("Evento {} descartado para {} porque hay HIGH pendiente/procesando", event.impact(), uuid);
            return;
        }

        // Eventos del flujo de registro no requieren personalidad
        boolean isRegistrationFlow = prompt.startsWith("GREETING_") || prompt.startsWith("CHAT_NAME_RECEIVED:");
        if (!isRegistrationFlow && blackboard.getPersonalityForPlayer(uuid) == null) {
            LOGGER.info("Evento diferido para {}: sin personalidad disponible aún", uuid);
            blackboard.publishEvent(new BotEvent(
                    event.playerUuid(),
                    event.prompt(),
                    event.impact(),
                    System.currentTimeMillis(),
                    event.neverIgnore()
            ));
            return;
        }

        if (!validateCooldowns(uuid, event, isRegistrationFlow)) {
            LOGGER.debug("Evento ignorado por cooldown: {}", prompt);
            return;
        }

        LOGGER.info("Ejecutando handler para: {}", prompt);

        if (highEvent) {
            blackboard.markHighProcessing(event.playerUuid());
        }
        try {
            if (prompt.startsWith("GREETING_NEW_PLAYER")) {
                handleNewPlayerGreeting(player, uuid);
            } else if (prompt.startsWith("GREETING_RETURNING_PLAYER")) {
                handleReturningPlayerGreeting(player, uuid);
            } else if (prompt.startsWith("CHAT_NAME_RECEIVED:")) {
                String playerName = prompt.substring("CHAT_NAME_RECEIVED:".length());
                handleNameReceived(player, uuid, playerName);
            } else if (prompt.startsWith("CHAT_MESSAGE:")) {
                String message = prompt.substring("CHAT_MESSAGE:".length());
                handleChatMessage(player, uuid, message);
            } else {
                handleGenericEvent(player, uuid, event);
            }
        } finally {
            if (highEvent) {
                blackboard.clearHighProcessing(event.playerUuid());
            }
        }
    }

    private boolean validateCooldowns(String uuid, BotEvent event, boolean isRegistrationFlow) {
        long now = System.currentTimeMillis();
        String prompt = event.prompt();

        if (isRegistrationFlow) {
            return true;
        }

        if (event.neverIgnore()) {
            if (isCriticalHealthPrompt(prompt)) {
                long lastCritical = lastCriticalHealthReplyMs.getOrDefault(uuid, 0L);
                if ((now - lastCritical) < CRITICAL_HEALTH_REPLY_COOLDOWN_MS) {
                    return false;
                }
                lastCriticalHealthReplyMs.put(uuid, now);
            }
            long lastHigh = blackboard.getLastHighEventMs(uuid);
            if ((now - lastHigh) < config.getHighEventCooldownMs()) {
                return false;
            }
            blackboard.setLastHighEventMs(uuid, now);
            blackboard.setLastReactiveEventMs(uuid, now);
            blackboard.setLastSpontaneousMs(uuid, now);
            return true;
        }

        if (isSpontaneousEvent(prompt)) {
            long lastSpont = blackboard.getLastSpontaneousMs(uuid);
            long spontaneousCooldown = Math.min(config.getSpontaneousCooldownMs(), 12_000L);
            if ((now - lastSpont) < spontaneousCooldown) {
                return false;
            }
            int silenceChance = switch (event.impact()) {
                case LOW -> 0;
                case NORMAL -> 0;
                case HIGH -> 0;
            };
            if (ThreadLocalRandom.current().nextInt(100) < silenceChance) {
                return false;
            }
            blackboard.setLastSpontaneousMs(uuid, now);
            return true;
        }

        long reactiveCooldown = event.impact() == BotEvent.Impact.LOW
                ? LOW_REACTIVE_COOLDOWN_MS
                : NORMAL_REACTIVE_COOLDOWN_MS;

        long lastReactive = blackboard.getLastReactiveEventMs(uuid);
        if ((now - lastReactive) < reactiveCooldown) {
            return false;
        }

        int silenceChance = switch (event.impact()) {
            case LOW -> 0;
            case NORMAL -> 0;
            case HIGH -> 0;
        };

        if (isUrgentReactiveEvent(prompt)) {
            silenceChance = 0;
        }

        if (ThreadLocalRandom.current().nextInt(100) < silenceChance) {
            return false;
        }

        blackboard.setLastReactiveEventMs(uuid, now);
        return true;
    }

    private boolean isSpontaneousEvent(String prompt) {
        String lower = prompt.toLowerCase();
        return lower.contains("espontáneo")
                || lower.contains("espontaneo")
                || lower.contains("di algo espont")
                || lower.contains("situación.");
    }

    private boolean isUrgentReactiveEvent(String prompt) {
        String lower = prompt.toLowerCase();
        return lower.contains("casi muerto")
                || lower.contains("se está muriendo de hambre")
                || lower.contains("se ahogó")
                || lower.contains("murió")
                || lower.contains("acaba de matar")
                || lower.contains("hay un ")
                || lower.contains("hay una ")
                || lower.contains("hay ");
    }

    private void handleNewPlayerGreeting(ServerPlayer player, String uuid) {
        JsonObject personality = blackboard.getPersonalityForPlayer(uuid);
        String botName = getBotNameForReply(personality);
        String language = blackboard.getPlayerLanguage(uuid);
        String reply = buildDeterministicAskName(language);

        try {
            reply = applyPostGuardrails(reply, BotEvent.Impact.HIGH, null);
            sendMessage(player, botName, reply);
            blackboard.addAwaitingName(uuid);
            LOGGER.info("[Nuevo] {}: {}", botName, reply);
        } catch (Exception e) {
            LOGGER.error("Error en saludo a nuevo jugador: {}", e.getMessage());
        }
    }

    private void handleReturningPlayerGreeting(ServerPlayer player, String uuid) {
        JsonObject personality = blackboard.getPersonalityForPlayer(uuid);
        if (personality == null) return;
        String playerName = blackboard.getPlayerName(uuid);
        if (playerName == null) {
            // Jugador que regresa pero sin nombre registrado — pedirle nombre de nuevo
            LOGGER.info("[Regreso sin nombre] Pidiendo nombre al jugador {}", uuid);
            handleNewPlayerGreeting(player, uuid);
            return;
        }
        String botName = personality.get("name").getAsString();
        String language = blackboard.getPlayerLanguage(uuid);
        LanguageProfile langProfile = LanguageManager.getProfile(language);
        String systemPrompt = promptManager.buildNormalPrompt(personality, language) + langProfile.getPlayerNameContext(playerName);
        String userPrompt = langProfile.getReturningPlayerPrompt(playerName);
        JsonArray history = blackboard.getPlayerHistory(uuid);
        try {
            String reply = ollamaClient.callOllama(systemPrompt, userPrompt, history, BotEvent.Impact.NORMAL);
            reply = applyPostGuardrails(reply, BotEvent.Impact.NORMAL, playerName);
            blackboard.addPlayerHistory(uuid, "assistant", reply, ollamaClient.getMaxHistory());
            dataManager.saveData();
            sendMessage(player, botName, reply);
            LOGGER.info("[Regreso: {}] {}: {}", playerName, botName, reply);
        } catch (Exception e) {
            LOGGER.error("Error en saludo a jugador que regresa: {}", e.getMessage());
        }
    }

    private void handleNameReceived(ServerPlayer player, String uuid, String playerName) {
        JsonObject personality = blackboard.getPersonalityForPlayer(uuid);
        String botName = getBotNameForReply(personality);
        String language = blackboard.getPlayerLanguage(uuid);
        String reply = buildDeterministicNameAck(language, playerName);

        try {
            reply = applyPostGuardrails(reply, BotEvent.Impact.HIGH, playerName);
            blackboard.addPlayerHistory(uuid, "assistant", reply, ollamaClient.getMaxHistory());
            sendMessage(player, botName, reply);
            LOGGER.info("[{}] {}: {}", playerName, botName, reply);
        } catch (Exception e) {
            LOGGER.error("Error al saludar con nombre: {}", e.getMessage());
        } finally {
            // Guardar siempre: el nombre ya fue registrado en memoria por ChatObserver
            dataManager.saveData();
        }
    }

    private void handleChatMessage(ServerPlayer player, String uuid, String message) {
        JsonObject personality = blackboard.getPersonalityForPlayer(uuid);
        if (personality == null) return;
        String playerName = blackboard.getPlayerName(uuid);
        if (playerName == null) return;
        String botName = personality.get("name").getAsString();
        String language = blackboard.getPlayerLanguage(uuid);
        LanguageProfile langProfile = LanguageManager.getProfile(language);
        String playerContext = buildPlayerContext(player);
        String systemPrompt = promptManager.buildSystemPrompt(personality, language)
                + langProfile.getPlayerNameContext(playerName)
                + playerContext;
        blackboard.addPlayerHistory(uuid, "user", message, ollamaClient.getMaxHistory());
        JsonArray history = blackboard.getPlayerHistory(uuid);
        try {
            String userPrompt = "MENSAJE LITERAL DEL JUGADOR: \"" + message + "\". " +
                    "Responde a ese mensaje. Usa el contexto solo como apoyo. No asumas metas, planes ni intenciones que el jugador no dijo.";
            BotEvent.Impact chatImpact = isUrgentChatMessage(message) ? BotEvent.Impact.HIGH : BotEvent.Impact.NORMAL;
            String reply = ollamaClient.callOllama(systemPrompt, userPrompt, history, chatImpact);
            reply = applyPostGuardrails(reply, chatImpact, playerName);
            blackboard.addPlayerHistory(uuid, "assistant", reply, ollamaClient.getMaxHistory());
            dataManager.saveData();
            sendMessage(player, botName, reply);
            LOGGER.info("[{}] {}: {}", playerName, botName, reply);
        } catch (Exception e) {
            String fallback = buildTimeoutReply(BotEvent.Impact.NORMAL, playerName, true, message);
            blackboard.addPlayerHistory(uuid, "assistant", fallback, ollamaClient.getMaxHistory());
            dataManager.saveData();
            sendMessage(player, botName, fallback);
            if (isTimeoutError(e)) {
                LOGGER.warn("Timeout procesando mensaje de chat para {}. Enviado fallback.", uuid);
                return;
            }
            LOGGER.error("Error procesando mensaje de chat: {}", e.getMessage());
        }
    }

    private void handleGenericEvent(ServerPlayer player, String uuid, BotEvent event) {
        JsonObject personality = blackboard.getPersonalityForPlayer(uuid);
        if (personality == null) return;
        String playerName = blackboard.getPlayerName(uuid);
        if (playerName == null) return;
        String botName = personality.get("name").getAsString();
        String language = blackboard.getPlayerLanguage(uuid);
        String playerContext = buildPlayerContext(player);
        String prompt = event.prompt().replace("[nombre]", playerName);
        String systemPrompt = buildStrictEventSystemPrompt(personality, event, playerName, language, playerContext);
        JsonArray history = getHistoryForEvent(uuid, event);
        LOGGER.debug("Prompt con nombre reemplazado: {}", prompt);
        try {
            if (isImmediateDangerEvent(prompt, event.impact())) {
                String immediateReply = buildImmediateDangerReply(playerName, prompt);
                blackboard.addPlayerHistory(uuid, "assistant", immediateReply, ollamaClient.getMaxHistory());
                dataManager.saveData();
                sendMessage(player, botName, immediateReply);
                LOGGER.info("[{}] {}: {}", playerName, botName, immediateReply);
                return;
            }
            if (event.impact() != BotEvent.Impact.HIGH && blackboard.hasHighPendingOrProcessing(event.playerUuid())) {
                LOGGER.debug("Se descarta respuesta {} por llegada de HIGH para {}", event.impact(), uuid);
                return;
            }
            String reply = generateStrictEventReply(systemPrompt, prompt, history, event.impact(), playerName);
            if (event.impact() != BotEvent.Impact.HIGH && blackboard.hasHighPendingOrProcessing(event.playerUuid())) {
                LOGGER.debug("Se descarta envío {} por HIGH pendiente para {}", event.impact(), uuid);
                return;
            }
            blackboard.addPlayerHistory(uuid, "assistant", reply, ollamaClient.getMaxHistory());
            dataManager.saveData();
            sendMessage(player, botName, reply);
            LOGGER.info("[{}] {}: {}", playerName, botName, reply);
        } catch (Exception e) {
            String fallback = buildTimeoutReply(event.impact(), playerName, false, prompt);
            blackboard.addPlayerHistory(uuid, "assistant", fallback, ollamaClient.getMaxHistory());
            dataManager.saveData();
            sendMessage(player, botName, fallback);
            if (isTimeoutError(e)) {
                LOGGER.warn("Timeout en reacción de evento {} para {}. Enviado fallback.", event.impact(), uuid);
                return;
            }
            LOGGER.error("Error en reacción a evento: {}", e.getMessage());
        }
    }

    /**
     * Construye un resumen del estado actual del jugador para dar contexto a la IA.
     */
    private String buildPlayerContext(ServerPlayer player) {
        try {
            int hearts = (int) Math.ceil(player.getHealth() / 2);
            int maxHearts = (int) Math.ceil(player.getMaxHealth() / 2);
            int food = player.getFoodData().getFoodLevel();

            long dayTime = ((net.minecraft.server.level.ServerLevel) player.level()).getDayTime() % 24000;
            String timeDesc = dayTime < 1000 ? "amanecer" : dayTime < 6000 ? "mañana" :
                    dayTime < 12000 ? "mediodía" : dayTime < 13500 ? "atardecer" :
                            dayTime < 18000 ? "noche" : "medianoche";

            String biome = "desconocido";
            try {
                var keyOpt = player.level().getBiome(player.blockPosition()).unwrapKey();
                if (keyOpt.isPresent()) {
                    String keyStr = keyOpt.get().toString();
                    if (keyStr.contains(" / ")) {
                        String path = keyStr.substring(keyStr.lastIndexOf(" / ") + 3).replace("]", "").trim();
                        biome = (path.contains(":") ? path.substring(path.indexOf(':') + 1) : path).replace("_", " ");
                    }
                }
            } catch (Exception ignored) {}

            String dim = "overworld";
            try {
                var d = player.level().dimension();
                if (d.equals(net.minecraft.world.level.Level.NETHER)) dim = "nether";
                else if (d.equals(net.minecraft.world.level.Level.END)) dim = "end";
            } catch (Exception ignored) {}

            String relevantItems = buildRelevantInventorySummary(player);

            String mainHand = player.getMainHandItem().isEmpty() ? "vacía" : player.getMainHandItem().getHoverName().getString();
            String offHand = player.getOffhandItem().isEmpty() ? "vacía" : player.getOffhandItem().getHoverName().getString();
            String lookedAtEntity = getLookedAtEntitySummary(player);

            // Entidades cercanas para que la IA sepa contra qué está reaccionando
            String nearbySummary = "sin entidades relevantes";
            String villageSignal = "no detectada";
            try {
                AABB box = new AABB(
                        player.getX() - 14, player.getY() - 6, player.getZ() - 14,
                        player.getX() + 14, player.getY() + 6, player.getZ() + 14
                );
                var entities = ((net.minecraft.server.level.ServerLevel) player.level())
                        .getEntitiesOfClass(LivingEntity.class, box, e -> e != player);

                int hostiles = 0;
                Map<String, Integer> nearbyTypes = new HashMap<>();
                for (LivingEntity entity : entities) {
                    if (entity instanceof Monster) {
                        hostiles++;
                    }
                    if (entity instanceof Villager || entity instanceof IronGolem) {
                        villageSignal = "sí (aldeanos/gólem detectados)";
                    }
                    String name = entity.getName().getString();
                    nearbyTypes.merge(name, 1, Integer::sum);
                }

                if (!nearbyTypes.isEmpty()) {
                    String topNearby = nearbyTypes.entrySet()
                            .stream()
                            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                            .limit(4)
                            .map(e -> e.getKey() + " x" + e.getValue())
                            .collect(Collectors.joining(", "));
                    nearbySummary = topNearby + " | hostiles=" + hostiles;
                }
            } catch (Exception ignored) {
            }

            return "\n[Estado del jugador: " + hearts + "/" + maxHearts + " corazones, " +
                    "hambre " + food + "/20, " +
                    "hora: " + timeDesc + ", " +
                    "bioma: " + biome + ", " +
                    "dimensión: " + dim + "]" +
                    "\n[Inventario relevante: " + relevantItems + "]" +
                    "\n[Mano principal: " + mainHand + " | Mano secundaria: " + offHand + "]" +
                    "\n[Entidad mirada: " + lookedAtEntity + "]" +
                    "\n[Cerca del jugador: " + nearbySummary + "]" +
                    "\n[Señales de aldea: " + villageSignal + "]";
        } catch (Exception e) {
            return "";
        }
    }

    private JsonArray getHistoryForEvent(String uuid, BotEvent event) {
        JsonArray fullHistory = blackboard.getPlayerHistory(uuid);
        int entriesToKeep = switch (event.impact()) {
            case HIGH -> 0;
            case NORMAL -> 2;
            case LOW -> 1;
        };

        if (entriesToKeep <= 0 || fullHistory.isEmpty()) {
            return new JsonArray();
        }

        JsonArray trimmed = new JsonArray();
        int start = Math.max(0, fullHistory.size() - entriesToKeep);
        for (int i = start; i < fullHistory.size(); i++) {
            trimmed.add(fullHistory.get(i));
        }
        return trimmed;
    }

    private String buildStrictEventSystemPrompt(JsonObject personality, BotEvent event, String playerName, String language, String playerContext) {
        return promptManager.buildPromptWithPlayerName(personality, event.impact(), playerName, language)
                + playerContext
                + "\n\nPROTOCOLO DE EVENTO ACTUAL:" 
                + "\n- El HECHO ACTUAL tiene prioridad total sobre historial y contexto." 
                + "\n- Si el HECHO dice Creeper, NO menciones zombi, caída, hacha ni otra causa." 
                + "\n- Si el HECHO no menciona arma, no inventes arma." 
                + "\n- Si el HECHO no menciona plan del jugador, no inventes plan." 
                + "\n- Reacciona a lo observado, no escribas fanfic.";
    }

    private String generateStrictEventReply(String systemPrompt, String eventPrompt, JsonArray history, BotEvent.Impact impact, String playerName) throws Exception {
        String userPrompt = buildStrictEventUserPrompt(eventPrompt);
        String reply = ollamaClient.callOllama(systemPrompt, userPrompt, history, impact);

        boolean shouldRetry = impact == BotEvent.Impact.LOW
                || (impact == BotEvent.Impact.NORMAL && !isImmediateDangerEvent(eventPrompt, impact));
        if (shouldRetry && isContradictoryEventReply(eventPrompt, reply)) {
            LOGGER.warn("Respuesta contradictoria detectada. Reintentando con guardrail fuerte. Evento='{}' Reply='{}'", eventPrompt, reply);
            String retrySystemPrompt = systemPrompt
                    + "\n\nCORRECCIÓN OBLIGATORIA: Tu respuesta anterior contradijo el hecho actual."
                    + "\nDebes corregirte. No menciones ninguna causa, mob, arma, distancia o situación que no esté en el hecho actual o en el contexto explícito.";
            String retryUserPrompt = userPrompt
                    + "\n\nREINTENTO ÚNICO: Responde de nuevo en 1 oración, totalmente fiel al hecho actual.";
            reply = ollamaClient.callOllama(retrySystemPrompt, retryUserPrompt, new JsonArray(), impact);
        }

        return applyPostGuardrails(reply, impact, playerName);
    }

    private String applyPostGuardrails(String rawReply, BotEvent.Impact impact, String playerName) {
        if (rawReply == null || rawReply.isBlank()) {
            return buildFallbackReply(impact, playerName);
        }

        String cleaned = rawReply.replaceAll("\\*[^*]*\\*", "").replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) {
            return buildFallbackReply(impact, playerName);
        }

        String normalized = ensureFinalPunctuation(cleaned);

        String normalizedForChecks = normalizeForComparison(normalized);
        if (containsSpeculationMarker(normalizedForChecks) || containsAssistantTone(normalizedForChecks)) {
            return buildFallbackReply(impact, playerName);
        }

        if (playerName != null && !playerName.isBlank() && !normalized.toLowerCase(Locale.ROOT).contains(playerName.toLowerCase(Locale.ROOT))) {
            normalized = playerName + ", " + normalized;
            normalized = ensureFinalPunctuation(normalized);
        }

        if (isLikelyTruncated(normalized) && normalized.length() < 24) {
            return buildFallbackReply(impact, playerName);
        }

        return normalized;
    }

    private boolean containsAssistantTone(String normalizedReply) {
        for (String marker : ASSISTANT_TONE_MARKERS) {
            if (normalizedReply.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLikelyTruncated(String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        return normalized.matches(".*\\b(de|del|a|con|en|por|para|que|y|o|el|la|los|las|un|una)\\.$");
    }

    private String ensureFinalPunctuation(String text) {
        if (text.isEmpty()) return text;
        char last = text.charAt(text.length() - 1);
        if (last == '.' || last == '!' || last == '?') {
            return text;
        }
        return text + ".";
    }

    private String buildFallbackReply(BotEvent.Impact impact, String playerName) {
        String prefix = (playerName != null && !playerName.isBlank()) ? playerName + ", " : "";
        if (impact == null) {
            return prefix + "mantente alerta.";
        }
        return switch (impact) {
            case LOW -> prefix + chooseVariant("todo en orden por ahora, pero ojo.", "vas bien; no bajes la guardia.");
            case NORMAL -> prefix + chooseVariant("sigue con cabeza fria, lo tienes.", "ojo con el entorno, pero sin panico.");
            case HIGH -> prefix + chooseVariant("peligro inmediato, reacciona ya.", "esto se puso feo, muévete ahora.");
        };
    }

    private boolean isUrgentHighEvent(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT);
        return lower.contains("muerte verificada")
                || lower.contains("murió")
                || lower.contains("mato")
                || lower.contains("mató")
                || lower.contains("casi muerto")
                || lower.contains("se está muriendo de hambre")
                || lower.contains("peligro")
                || lower.contains("alerta verificada");
    }

    private boolean isImmediateDangerEvent(String prompt, BotEvent.Impact impact) {
        if (impact == BotEvent.Impact.HIGH && isUrgentHighEvent(prompt)) {
            return true;
        }
        String lower = prompt.toLowerCase(Locale.ROOT);
        if (!lower.contains("alerta verificada")) {
            return false;
        }
        int mobCount = extractMobCount(lower);
        int hearts = extractHearts(lower);
        int distance = extractDistance(lower);
        return mobCount >= 4
                || (distance > 0 && distance <= 2)
                || (hearts > 0 && hearts <= 2)
                || (hearts > 0 && hearts <= 3 && distance > 0 && distance <= 6);
    }

    private String buildImmediateDangerReply(String playerName, String prompt) {
        String prefix = (playerName != null && !playerName.isBlank()) ? playerName + ", " : "";
        String lower = prompt.toLowerCase(Locale.ROOT);
        String mobs = extractMobSummary(prompt);
        if (lower.contains("muerte verificada") || lower.contains("murió") || lower.contains("mato") || lower.contains("mató")) {
            return prefix + "te bajaron; reaparece, respira y vuelve con plan corto.";
        }
        int hearts = extractHearts(lower);
        int mobCount = extractMobCount(lower);
        int distance = extractDistance(lower);

        if (lower.contains("casi muerto") || (hearts > 0 && hearts <= 2)) {
            if (hearts > 0 && hearts <= 2) {
                return prefix + chooseVariant(
                        "estas a un golpe, cubrete ya y curate en cuanto puedas.",
                        "te soplan y te vas al lobby, cura primero.",
                        "ni un intercambio mas: cobertura y curacion inmediata."
                );
            }
            return prefix + "salud critica, corta pelea y curate ahora.";
        }
        if (lower.contains("se está muriendo de hambre") || lower.contains("hambre")) {
            return prefix + "hambre critica, come ya y no te expongas.";
        }
        if (mobCount >= 4) {
            return prefix + chooseVariant(
                    "son demasiados hostiles encima (" + mobs + "), retrocede y rompe linea de vision.",
                    "estas rodeado por " + mobs + ", kitea y busca cobertura ya.",
                    "horda encima: " + mobs + "; retrocede en diagonal y gana espacio.",
                    "si esto fuera speedrun al respawn, irias primero; sal de ahi ya."
            );
        }
        if (distance > 0 && distance <= 2) {
            return prefix + chooseVariant(
                    "los tienes pegados (" + mobs + "), esquiva lateral y busca cobertura inmediata.",
                    "estan encima (" + mobs + "), no intercambies golpes de frente.",
                    "distancia critica con " + mobs + ", muévete ya y cubrete."
            );
        }
        if (lower.contains("alerta verificada") || lower.contains("hay ")) {
            return prefix + chooseVariant(
                    "tienes cerca a " + mobs + "; muévete, cubrete y pelea por espacio.",
                    "ojo, vienen " + mobs + "; prioriza posicion y salida segura.",
                    "atento: " + mobs + " cerca; no te quedes quieto.",
                    "te estan cerrando el paso (" + mobs + "), gira y abre hueco."
            );
        }
        return buildFallbackReply(BotEvent.Impact.HIGH, playerName);
    }

    private String buildTimeoutReply(BotEvent.Impact impact, String playerName, boolean fromChat, String context) {
        String prefix = (playerName != null && !playerName.isBlank()) ? playerName + ", " : "";
        if (fromChat) {
            return prefix + chooseVariant(
                    "te leo, dame un segundo y te respondo bien.",
                    "te contesto en nada, ando esquivando el caos.",
                    "te escucho, deja que ordene idea rapido."
            );
        }
        
        // Intentar dar una respuesta específica basada en el contexto si falla la IA
        if (context != null) {
            String lower = context.toLowerCase(Locale.ROOT);
            int mobCount = extractMobCount(lower);
            if (mobCount >= 4) return prefix + "muchos hostiles encima, retrocede y curarte primero.";
            if (lower.contains("creeper")) return prefix + "creeper cerca, no te quedes quieto.";
            if (lower.contains("zombi")) return prefix + "zombis cerca, retrocede y controla distancia.";
            if (lower.contains("esqueleto")) return prefix + "esqueletos al frente, usa cobertura.";
            if (lower.contains("araña")) return prefix + "arana encima, pegale y reposiciona.";
            if (lower.contains("enderman")) return prefix + "enderman cerca, evita mirarlo y cubrete.";
            if (lower.contains("bruja")) return prefix + "bruja cerca, evita intercambio largo.";
            if (lower.contains("phantom") || lower.contains("fantasma")) return prefix + "fantasma encima, mueve y cubrete.";
            
            if (lower.contains("hambre")) return prefix + "come algo.";
            if (lower.contains("salud") || lower.contains("corazones")) return prefix + "cuidado con la vida.";
            if (lower.contains("lava")) return prefix + "¡Cuidado con la lava!";
            if (lower.contains("caida") || lower.contains("caída")) return prefix + "¡Cuidado al bajar!";
        }

        if (impact == null) {
            return prefix + "mantente alerta.";
        }
        return switch (impact) {
            case LOW -> prefix + "sigo observando.";
            case NORMAL -> prefix + chooseVariant("te cubro, dame un segundo.", "sigo contigo, responde con calma.");
            case HIGH -> prefix + chooseVariant("peligro inmediato!", "esto esta picante, muévete ya!");
        };
    }

    private boolean isUrgentChatMessage(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String lower = normalizeForComparison(message);
        for (String term : URGENT_CHAT_TERMS) {
            if (lower.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private int extractMobCount(String text) {
        int count = 0;
        Matcher matcher = MULTIPLIER_COUNT_PATTERN.matcher(text);
        while (matcher.find()) {
            count += Integer.parseInt(matcher.group(1));
        }
        if (count > 0) {
            return count;
        }
        if (text.contains("hay un ") || text.contains("hay una ")) {
            return 1;
        }
        return 0;
    }

    private int extractHearts(String text) {
        Matcher matcher = HEARTS_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    private int extractDistance(String text) {
        Matcher matcher = DISTANCE_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    private boolean isTimeoutError(Exception e) {
        if (e instanceof HttpTimeoutException) {
            return true;
        }
        String msg = e.getMessage();
        return msg != null && msg.toLowerCase(Locale.ROOT).contains("timed out");
    }

    private String getBotNameForReply(JsonObject personality) {
        if (personality != null && personality.has("name")) {
            return personality.get("name").getAsString();
        }
        String fallback = blackboard.getBotName();
        return (fallback == null || fallback.isBlank()) ? "Bot" : fallback;
    }

    private String buildDeterministicAskName(String language) {
        String base = language == null ? "es" : language.split("_")[0];
        return switch (base) {
            case "en" -> "Hi, what should I call you?";
            case "pt" -> "Oi, como devo te chamar?";
            default -> "Hola, ¿cómo te llamas?";
        };
    }

    private String buildDeterministicNameAck(String language, String playerName) {
        String base = language == null ? "es" : language.split("_")[0];
        String safeName = (playerName == null || playerName.isBlank()) ? "jugador" : playerName;
        return switch (base) {
            case "en" -> "Nice to meet you, " + safeName + ".";
            case "pt" -> "Prazer, " + safeName + ".";
            default -> "Un gusto, " + safeName + ".";
        };
    }

    private String buildStrictEventUserPrompt(String eventPrompt) {
        return "HECHO ACTUAL PRIORITARIO: " + eventPrompt
                + "\nTAREA: Reacciona SOLO a este hecho."
                + "\nPERMITIDO: emoción, advertencia breve, comentario corto."
                + "\nPROHIBIDO: inventar causas, armas, planes, mobs o detalles no mencionados."
                + "\nSi dudas, mantente literal.";
    }

    private boolean isContradictoryEventReply(String eventPrompt, String reply) {
        String normalizedEvent = normalizeForComparison(eventPrompt);
        String normalizedReply = normalizeForComparison(reply);

        if (containsSpeculationMarker(normalizedReply) && normalizedEvent.contains("verificada")) {
            return true;
        }

        Set<String> eventTerms = extractFactTerms(normalizedEvent);
        Set<String> replyTerms = extractFactTerms(normalizedReply);

        if (!eventTerms.isEmpty()) {
            for (String replyTerm : replyTerms) {
                if (!eventTerms.contains(replyTerm)) {
                    return true;
                }
            }
        }

        List<Integer> eventNumbers = extractNumbers(normalizedEvent);
        List<Integer> replyNumbers = extractNumbers(normalizedReply);
        if (!eventNumbers.isEmpty() && !replyNumbers.isEmpty()) {
            for (Integer number : replyNumbers) {
                if (!eventNumbers.contains(number)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsSpeculationMarker(String normalizedReply) {
        for (String marker : SPECULATION_MARKERS) {
            if (normalizedReply.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> extractFactTerms(String text) {
        Set<String> found = new HashSet<>();
        for (String term : EVENT_FACT_TERMS) {
            if (text.contains(term)) {
                found.add(term);
            }
        }
        return found;
    }

    private List<Integer> extractNumbers(String text) {
        List<Integer> numbers = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c)) {
                current.append(c);
            } else if (current.length() > 0) {
                numbers.add(Integer.parseInt(current.toString()));
                current.setLength(0);
            }
        }
        if (current.length() > 0) {
            numbers.add(Integer.parseInt(current.toString()));
        }
        return numbers;
    }

    private String normalizeForComparison(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return normalized.replaceAll("\\s+", " ").trim();
    }

    private String buildRelevantInventorySummary(ServerPlayer player) {
        try {
            Map<String, Integer> counts = new HashMap<>();
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;

                String itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
                if (!isRelevantItemKey(itemKey)) continue;

                String itemName = stack.getHoverName().getString();
                counts.merge(itemName, stack.getCount(), Integer::sum);
            }

            if (counts.isEmpty()) {
                return "sin objetos especialmente relevantes";
            }

            return counts.entrySet()
                    .stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(6)
                    .map(e -> e.getKey() + " x" + e.getValue())
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "desconocido";
        }
    }

    private boolean isRelevantItemKey(String itemKey) {
        return itemKey.contains("sword")
                || itemKey.contains("axe")
                || itemKey.contains("pickaxe")
                || itemKey.contains("shovel")
                || itemKey.contains("hoe")
                || itemKey.contains("shield")
                || itemKey.contains("bow")
                || itemKey.contains("crossbow")
                || itemKey.contains("arrow")
                || itemKey.contains("torch")
                || itemKey.contains("bread")
                || itemKey.contains("beef")
                || itemKey.contains("porkchop")
                || itemKey.contains("chicken")
                || itemKey.contains("mutton")
                || itemKey.contains("apple")
                || itemKey.contains("golden_apple")
                || itemKey.contains("bucket")
                || itemKey.contains("diamond")
                || itemKey.contains("iron_ingot")
                || itemKey.contains("coal")
                || itemKey.contains("log")
                || itemKey.contains("planks")
                || itemKey.contains("cobblestone")
                || itemKey.contains("crafting_table")
                || itemKey.contains("furnace")
                || itemKey.contains("bed");
    }

    private String getLookedAtEntitySummary(ServerPlayer player) {
        try {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            AABB box = new AABB(
                    player.getX() - 16, player.getY() - 8, player.getZ() - 16,
                    player.getX() + 16, player.getY() + 8, player.getZ() + 16
            );

            LivingEntity best = null;
            double bestDot = 0.94D;
            double bestDistance = Double.MAX_VALUE;

            for (LivingEntity entity : ((net.minecraft.server.level.ServerLevel) player.level()).getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                Vec3 toEntity = entity.getEyePosition().subtract(eyePos);
                double distance = toEntity.length();
                if (distance <= 0.001D || distance > 16.0D) continue;

                double dot = look.dot(toEntity.normalize());
                if (dot > bestDot || (Math.abs(dot - bestDot) < 0.01D && distance < bestDistance)) {
                    best = entity;
                    bestDot = dot;
                    bestDistance = distance;
                }
            }

            if (best == null) {
                return "ninguna entidad clara";
            }

            String type = best instanceof Monster ? "hostil" : "no hostil";
            return best.getName().getString() + " a ~" + Math.max(1, (int) Math.round(bestDistance)) + " bloques (" + type + ")";
        } catch (Exception e) {
            return "desconocida";
        }
    }

    private void sendMessage(ServerPlayer player, String botName, String message) {
        if (player != null && player.connection != null) {
            String finalMessage = diversifyRepeatedReply(player.getUUID().toString(), message);
            String prefix = config.getBotChatPrefix();
            String suffix = config.getBotChatSuffix();
            player.sendSystemMessage(Component.literal(prefix + botName + ": " + suffix + finalMessage));
        }
    }

    private String diversifyRepeatedReply(String playerUuid, String message) {
        if (message == null || message.isBlank()) {
            return message;
        }
        String normalized = normalizeForComparison(message);
        Deque<String> history = recentRepliesByPlayer.computeIfAbsent(playerUuid, ignored -> new ArrayDeque<>());

        if (!isRecentlyRepeated(history, normalized)) {
            rememberReply(history, normalized);
            return message;
        }

        String lower = normalized;
        if (lower.contains("salud critica")) {
            String variant = chooseVariant(
                    "aguanta, curate primero y luego retomamos combate.",
                    "prioriza vida: cubrete, come o pocion y recien peleas.",
                    "no te regales, sana primero y reentra con mejor angulo."
            );
            rememberReply(history, normalizeForComparison(variant));
            return variant;
        }
        if (lower.contains("amenaza") || lower.contains("hostiles") || lower.contains("zombi")
                || lower.contains("creeper") || lower.contains("esqueleto") || lower.contains("arana")) {
            String variant = chooseVariant(
                    "te siguen presionando, gira, cubrete y corta linea de vision.",
                    "manten movilidad y no pelees rodeado.",
                    "respira, reposiciona y limpia uno por uno."
            );
            rememberReply(history, normalizeForComparison(variant));
            return variant;
        }
        rememberReply(history, normalized);
        return message;
    }

    private boolean isRecentlyRepeated(Deque<String> history, String normalizedMessage) {
        synchronized (history) {
            for (String recent : history) {
                if (recent.equals(normalizedMessage)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void rememberReply(Deque<String> history, String normalizedMessage) {
        synchronized (history) {
            if (history.size() >= RECENT_REPLY_WINDOW) {
                history.removeFirst();
            }
            history.addLast(normalizedMessage);
        }
    }

    private boolean isCriticalHealthPrompt(String prompt) {
        String normalized = normalizeForComparison(prompt);
        return normalized.contains("casi muerto")
                || normalized.contains("le quedan solo 1 corazones")
                || normalized.contains("le quedan solo 2 corazones")
                || normalized.contains("le quedan solo 3 corazones")
                || normalized.contains("tiene 1 corazones")
                || normalized.contains("tiene 2 corazones");
    }

    private String chooseVariant(String... options) {
        if (options == null || options.length == 0) {
            return "mantente alerta.";
        }
        return options[ThreadLocalRandom.current().nextInt(options.length)];
    }

    private String extractMobSummary(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT);
        Matcher grouped = MOB_LIST_PAREN_PATTERN.matcher(prompt);
        if (grouped.find()) {
            return grouped.group(1).trim();
        }
        if (lower.contains("creeper")) return "creeper";
        if (lower.contains("esqueleto")) return "esqueleto";
        if (lower.contains("zombi")) return "zombi";
        if (lower.contains("araña") || lower.contains("arana")) return "arana";
        Matcher singular = MOB_SINGULAR_PATTERN.matcher(lower);
        if (singular.find()) {
            String mob = singular.group(1) != null ? singular.group(1) : singular.group(2);
            if (mob != null && !mob.isBlank()) {
                return mob;
            }
        }
        return "mobs hostiles";
    }
}




