package com.adenium.zanatenunchi.blackboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class Blackboard {

    private static final Logger LOGGER = LoggerFactory.getLogger("Blackboard");
    private static final int MAX_EVENTS_PER_PLAYER = 5;
    private static final int INITIAL_QUEUE_CAPACITY = 10;
    private static final long LOW_EVENT_DEDUPE_MS = 90_000L;
    private final Map<String, Long> nextSpontMs = new ConcurrentHashMap<>();

    private static Blackboard instance;
    private volatile MinecraftServer currentServer;
    private volatile JsonObject botData;

    // Colas por jugador para IA Personal
    private final Map<UUID, PriorityBlockingQueue<BotEvent>> playerEventQueues = new ConcurrentHashMap<>();

    // Rastreo de tiempo y cooldowns
    private final Map<String, Long> lastHighEventMs = new ConcurrentHashMap<>();
    private final Map<String, Long> lastReactiveEventMs = new ConcurrentHashMap<>();
    private final Map<String, Long> lastSpontaneousMs = new ConcurrentHashMap<>();
    private final Map<String, Long> recentLowEventSignatures = new ConcurrentHashMap<>();

    // Estados de advertencia (Para que no sea un bot enfadoso)
    private final Map<String, Boolean> dangerWarned = new ConcurrentHashMap<>();
    private final Map<String, Boolean> lowHealthWarned = new ConcurrentHashMap<>();
    private final Map<String, Boolean> lowFoodWarned = new ConcurrentHashMap<>();

    // Rastreo de entorno
    private final Map<String, String> lastBiome = new ConcurrentHashMap<>();
    private final Map<String, String> lastDimension = new ConcurrentHashMap<>();
    private final Map<String, String> playerLanguage = new ConcurrentHashMap<>();

    // Estado global del mundo (tiempo/tiempo del día) que algunos observers consultan
    private volatile long lastDayTime = 0L;
    private volatile boolean wasRainingFlag = false;
    private volatile boolean wasThunderingFlag = false;

    // Sets de control de flujo
    private final Set<String> awaitingName = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> pendingGreeting = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> pendingPersonality = Collections.synchronizedSet(new HashSet<>());
    private final Set<UUID> highInProcessPlayers = Collections.synchronizedSet(new HashSet<>());

    private volatile String serverLanguage = "es_mx";
    private final Object dataLock = new Object();

    private Blackboard() {}

    public static synchronized Blackboard getInstance() {
        if (instance == null) instance = new Blackboard();
        return instance;
    }

    // --- GESTIÓN DE EVENTOS ---

    public void publishEvent(BotEvent event) {
        if (isDuplicateLowEvent(event)) return;
        UUID uuid = event.playerUuid();
        if (event.impact() != BotEvent.Impact.HIGH && hasHighPendingOrProcessing(uuid)) return;

        PriorityBlockingQueue<BotEvent> queue = playerEventQueues.computeIfAbsent(uuid, k -> new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY));
        if (queue.size() >= MAX_EVENTS_PER_PLAYER) queue.poll();
        queue.offer(event);
    }

    public boolean hasEventsForPlayer(UUID uuid) {
        return playerEventQueues.containsKey(uuid) && !playerEventQueues.get(uuid).isEmpty();
    }

    public BotEvent pollEventForPlayer(UUID uuid) {
        return playerEventQueues.containsKey(uuid) ? playerEventQueues.get(uuid).poll() : null;
    }

    public void clearNonEssentialEvents(UUID uuid) {
        if (playerEventQueues.containsKey(uuid)) {
            playerEventQueues.get(uuid).removeIf(e -> e.impact() != BotEvent.Impact.HIGH && !e.neverIgnore());
        }
    }

    private boolean isDuplicateLowEvent(BotEvent event) {
        if (event.impact() != BotEvent.Impact.LOW) return false;
        String sig = event.playerUuid() + "|" + event.prompt().toLowerCase().trim();
        long now = System.currentTimeMillis();
        if (recentLowEventSignatures.containsKey(sig) && (now - recentLowEventSignatures.get(sig)) < LOW_EVENT_DEDUPE_MS) return true;
        recentLowEventSignatures.put(sig, now);
        return false;
    }

    // --- GESTIÓN DE DATOS Y PERSONALIDAD ---

    public boolean hasPlayer(String uuid) {
        synchronized (dataLock) {
            return botData != null && botData.has("players") && botData.getAsJsonObject("players").has(uuid);
        }
    }

    public boolean hasPlayerPersonality(String uuid) {
        synchronized (dataLock) {
            return hasPlayer(uuid) && botData.getAsJsonObject("players").getAsJsonObject(uuid).has("personality");
        }
    }

    public void setPlayerPersonality(String uuid, JsonObject personality) {
        synchronized (dataLock) {
            if (botData == null) return;
            JsonObject players = botData.has("players") ? botData.getAsJsonObject("players") : new JsonObject();
            JsonObject pd = players.has(uuid) ? players.getAsJsonObject(uuid) : new JsonObject();
            pd.add("personality", personality);
            players.add(uuid, pd);
            botData.add("players", players);
        }
    }

    public JsonObject getPersonalityForPlayer(String uuid) {
        synchronized (dataLock) {
            if (hasPlayerPersonality(uuid)) return botData.getAsJsonObject("players").getAsJsonObject(uuid).getAsJsonObject("personality");
            return (botData != null && botData.has("personality")) ? botData.getAsJsonObject("personality") : null;
        }
    }

    public void setPersonality(JsonObject personality) {
        synchronized (dataLock) {
            if (botData == null) botData = new JsonObject();
            botData.add("personality", personality);
        }
    }

    // --- CONTROL DE ADVERTENCIAS ---

    public boolean isDangerWarned(String uuid) { return dangerWarned.getOrDefault(uuid, false); }
    public void setDangerWarned(String uuid, boolean warned) { dangerWarned.put(uuid, warned); }

    public boolean isLowHealthWarned(String uuid) { return lowHealthWarned.getOrDefault(uuid, false); }
    public void setLowHealthWarned(String uuid, boolean warned) { lowHealthWarned.put(uuid, warned); }

    public boolean isLowFoodWarned(String uuid) { return lowFoodWarned.getOrDefault(uuid, false); }
    public void setLowFoodWarned(String uuid, boolean warned) { lowFoodWarned.put(uuid, warned); }

    public String getLastBiome(String uuid) { return lastBiome.get(uuid); }
    public void setLastBiome(String uuid, String biome) { lastBiome.put(uuid, biome); }

    // --- CONTROL DE FLUJO ---

    public boolean isAwaitingName(String uuid) { return awaitingName.contains(uuid); }
    public void addAwaitingName(String uuid) { awaitingName.add(uuid); }
    public void removeAwaitingName(String uuid) { awaitingName.remove(uuid); }

    public boolean isPendingGreeting(String uuid) { return pendingGreeting.contains(uuid); }
    public void addPendingGreeting(String uuid) { pendingGreeting.add(uuid); }
    public Set<String> getPendingGreetings() { return pendingGreeting; }
    public void clearPendingGreetings() { pendingGreeting.clear(); }

    public boolean isPendingPersonality(String uuid) { return pendingPersonality.contains(uuid); }
    public void addPendingPersonality(String uuid) { pendingPersonality.add(uuid); }
    public void removePendingPersonality(String uuid) { pendingPersonality.remove(uuid); }

    // --- HISTORIAL Y NOMBRES ---

    public String getPlayerName(String uuid) {
        synchronized (dataLock) {
            if (!hasPlayer(uuid)) return null;
            JsonObject pd = botData.getAsJsonObject("players").getAsJsonObject(uuid);
            return pd.has("name") ? pd.get("name").getAsString() : null;
        }
    }

    public JsonArray getPlayerHistory(String uuid) {
        synchronized (dataLock) {
            if (!hasPlayer(uuid)) return new JsonArray();
            JsonObject pd = botData.getAsJsonObject("players").getAsJsonObject(uuid);
            return pd.has("history") ? pd.getAsJsonArray("history").deepCopy() : new JsonArray();
        }
    }

    public void addPlayerHistory(String uuid, String role, String content, int max) {
        synchronized (dataLock) {
            if (!hasPlayer(uuid)) return;
            JsonObject pd = botData.getAsJsonObject("players").getAsJsonObject(uuid);
            JsonArray history = pd.has("history") ? pd.getAsJsonArray("history") : new JsonArray();
            JsonObject entry = new JsonObject();
            entry.addProperty("role", role);
            entry.addProperty("content", content);
            history.add(entry);
            while (history.size() > max * 2) history.remove(0);
            pd.add("history", history);
        }
    }

    // --- ESTADO DE PROCESAMIENTO ---

    public void markHighProcessing(UUID uuid) { highInProcessPlayers.add(uuid); }
    public void clearHighProcessing(UUID uuid) { highInProcessPlayers.remove(uuid); }
    public boolean hasHighPendingOrProcessing(UUID uuid) {
        if (highInProcessPlayers.contains(uuid)) return true;
        PriorityBlockingQueue<BotEvent> q = playerEventQueues.get(uuid);
        return q != null && q.stream().anyMatch(e -> e.impact() == BotEvent.Impact.HIGH);
    }

    public void setBotData(JsonObject data) { synchronized (dataLock) { this.botData = data; } }
    public JsonObject getBotData() { synchronized (dataLock) { return botData; } }
    public String getPlayerLanguage(String uuid) { return playerLanguage.getOrDefault(uuid, serverLanguage); }
    public void setPlayerLanguage(String uuid, String language) { playerLanguage.put(uuid, language); }
    public String getServerLanguage() { return serverLanguage; }

    // Métodos que registran jugadores y proveen datos solicitados por observers
    public void registerNewPlayer(String uuid, String name) {
        synchronized (dataLock) {
            if (botData == null) botData = new JsonObject();
            JsonObject players = botData.has("players") ? botData.getAsJsonObject("players") : new JsonObject();
            JsonObject pd = players.has(uuid) ? players.getAsJsonObject(uuid) : new JsonObject();
            if (name != null && !name.isEmpty()) pd.addProperty("name", name);
            players.add(uuid, pd);
            botData.add("players", players);
        }
    }
    public void setLastReactiveEventMs(String uuid, long t) { lastReactiveEventMs.put(uuid, t); }
    public long getLastReactiveEventMs(String uuid) { return lastReactiveEventMs.getOrDefault(uuid, 0L); }
    public void setLastHighEventMs(String uuid, long t) { lastHighEventMs.put(uuid, t); }
    public long getLastHighEventMs(String uuid) { return lastHighEventMs.getOrDefault(uuid, 0L); }
    public void setCurrentServer(MinecraftServer s) { this.currentServer = s; }
    public MinecraftServer getCurrentServer() { return currentServer; }
    public Long getNextSpontMs(String uuid) { return nextSpontMs.get(uuid); }
    public void setNextSpontMs(String uuid, long time) { nextSpontMs.put(uuid, time); }

    // Estado del mundo (consultado por WorldObserver)
    public long getLastDayTime() { return lastDayTime; }
    public boolean wasRaining() { return wasRainingFlag; }
    public boolean wasThundering() { return wasThunderingFlag; }
    public void setLastDayTime(long t) { this.lastDayTime = t; }
    public void setWasRaining(boolean r) { this.wasRainingFlag = r; }
    public void setWasThundering(boolean t) { this.wasThunderingFlag = t; }

    public String getLastDimension(String uuid) { return lastDimension.get(uuid); }
    public void setLastDimension(String uuid, String dim) { lastDimension.put(uuid, dim); }

    // Estado general de personalidad
    public boolean hasPersonality() { synchronized (dataLock) { return botData != null && botData.has("personality"); } }
    public String getBotName() { synchronized (dataLock) { if (botData != null && botData.has("name")) return botData.get("name").getAsString(); return "ZanateNunchi"; } }

    public void clearAllState() {
        playerEventQueues.clear();
        awaitingName.clear();
        pendingGreeting.clear();
        pendingPersonality.clear();
        highInProcessPlayers.clear();
        dangerWarned.clear();
        lowHealthWarned.clear();
        lowFoodWarned.clear();
        lastBiome.clear();
        LOGGER.info("Estado del Blackboard limpiado");
    }
}