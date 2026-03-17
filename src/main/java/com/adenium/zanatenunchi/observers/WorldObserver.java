package com.adenium.zanatenunchi.observers;

import com.adenium.zanatenunchi.blackboard.Blackboard;
import com.adenium.zanatenunchi.blackboard.BotEvent;
import com.adenium.zanatenunchi.blackboard.BotEvent.Impact;
import com.adenium.zanatenunchi.lang.IBotLanguageProvider;
import com.adenium.zanatenunchi.util.LanguageManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class WorldObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger("WorldObserver");

    private static final Set<String> NOTABLE_BIOMES = new HashSet<>(Arrays.asList(
            "plains", "desert", "forest", "taiga", "swamp", "jungle", "savanna",
            "badlands", "ocean", "dark_forest", "snowy_plains", "mushroom_fields",
            "cherry_grove", "deep_dark", "nether_wastes", "soul_sand_valley",
            "crimson_forest", "warped_forest", "basalt_deltas", "the_end"
    ));

    private final Blackboard blackboard;
    private int tickCounter = 0;

    public WorldObserver(Blackboard blackboard) {
        this.blackboard = blackboard;
    }

    public void register() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
        LOGGER.info("WorldObserver registrado");
    }

    private void onServerTick(MinecraftServer server) {
        if (server.getPlayerList().getPlayers().isEmpty()) return;

        int tick = ++tickCounter;
        if (tick % 40 != 0) return;

        checkWeatherAndTime(server);

        if (tick % 100 == 0) {
            checkBiomeChanges(server);
            checkDimensionChanges(server);
        }

        if (tickCounter >= 144000) {
            tickCounter = 0;
        }
    }

    private void checkWeatherAndTime(MinecraftServer server) {
        long dayTime = server.overworld().getDayTime() % 24000;
        boolean isRaining = server.overworld().isRaining();
        boolean isThundering = server.overworld().isThundering();

        long lastDayTime = blackboard.getLastDayTime();
        boolean wasRaining = blackboard.wasRaining();
        boolean wasThundering = blackboard.wasThundering();

        if (lastDayTime >= 0) {
            if (lastDayTime < 12500 && dayTime >= 12500 && dayTime < 13500) {
                publishSunTransitionEvent(server, false);
            }

            if (lastDayTime >= 22500 && dayTime < 1000) {
                publishSunTransitionEvent(server, true);
            }

            if (!wasRaining && isRaining && !isThundering) {
                LOGGER.info("Detectado: empezó a llover");
                publishWeatherEvent(server, "rain", true, 100, Impact.NORMAL);
            }

            if (!wasThundering && isThundering) {
                LOGGER.info("Detectado: tormenta eléctrica");
                publishWeatherEvent(server, "thunder", true, 100, Impact.NORMAL);
            }

            if (wasRaining && !isRaining) {
                LOGGER.info("Detectado: dejó de llover");
                publishWeatherEvent(server, "rain", false, 40, Impact.LOW);
            }
        }

        blackboard.setLastDayTime(dayTime);
        blackboard.setWasRaining(isRaining);
        blackboard.setWasThundering(isThundering);
    }

    private void publishWeatherEvent(MinecraftServer server, String weatherType, boolean isStarting, int chancePercent, Impact impact) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();
            if (!blackboard.hasPlayer(uuid)) continue;

            if (ThreadLocalRandom.current().nextInt(100) < chancePercent) {
                String langCode = blackboard.getPlayerLanguage(uuid);
                IBotLanguageProvider langProvider = LanguageManager.getProvider(langCode);
                String promptText = langProvider.getWeatherEvent(weatherType, isStarting);

                BotEvent event = new BotEvent(
                        player.getUUID(),
                        promptText,
                        impact,
                        System.currentTimeMillis(),
                        isStarting
                );
                blackboard.publishEvent(event);
            }
        }
    }

    private void checkBiomeChanges(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();
            if (!blackboard.hasPlayer(uuid)) {
                continue;
            }
            String current = getBiomeName(player);
            String prev = blackboard.getLastBiome(uuid);

            if (prev != null && !current.equals(prev) && !current.equals("unknown") && NOTABLE_BIOMES.contains(current)) {
                String langCode = blackboard.getPlayerLanguage(uuid);
                IBotLanguageProvider langProvider = LanguageManager.getProvider(langCode);
                String playerName = player.getName().getString();
                String promptText = langProvider.getBiomeChangeEvent(playerName, current);

                BotEvent event = new BotEvent(
                        player.getUUID(),
                        promptText,
                        Impact.LOW,
                        System.currentTimeMillis(),
                        false
                );
                blackboard.publishEvent(event);
            }
            if (!current.equals("unknown")) {
                blackboard.setLastBiome(uuid, current);
            }
        }
    }

    private void checkDimensionChanges(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();

            if (!blackboard.hasPlayer(uuid)) continue;

            String current = getDimensionName(player);
            String prev = blackboard.getLastDimension(uuid);

            if (prev != null && !current.equals(prev)) {
                String langCode = blackboard.getPlayerLanguage(uuid);
                IBotLanguageProvider langProvider = LanguageManager.getProvider(langCode);
                String playerName = player.getName().getString();

                String promptText = langProvider.getDimensionChangeEvent(playerName, current, prev);

                if (promptText != null) {
                    BotEvent event = new BotEvent(
                            player.getUUID(),
                            promptText,
                            Impact.NORMAL,
                            System.currentTimeMillis(),
                            false
                    );
                    blackboard.publishEvent(event);
                }
            }

            blackboard.setLastDimension(uuid, current);
        }
    }

    private void publishSunTransitionEvent(MinecraftServer server, boolean sunrise) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();
            if (!blackboard.hasPlayer(uuid)) continue;

            String langCode = blackboard.getPlayerLanguage(uuid);
            IBotLanguageProvider langProvider = LanguageManager.getProvider(langCode);
            String playerName = player.getName().getString();

            int hearts = (int) Math.ceil(player.getHealth() / 2);
            int food = player.getFoodData().getFoodLevel();
            int nearbyHostiles = countNearbyHostiles(player, 10.0D);

            String timeOfDay = sunrise ? "sunrise" : "sunset";
            String promptText = langProvider.getTimeEvent(timeOfDay, playerName, hearts, food, nearbyHostiles);

            BotEvent event = new BotEvent(
                    player.getUUID(),
                    promptText,
                    Impact.NORMAL,
                    System.currentTimeMillis(),
                    false
            );
            blackboard.publishEvent(event);
        }
    }

    private int countNearbyHostiles(ServerPlayer player, double radius) {
        try {
            AABB box = new AABB(
                    player.getX() - radius, player.getY() - 5, player.getZ() - radius,
                    player.getX() + radius, player.getY() + 5, player.getZ() + radius
            );
            return ((ServerLevel) player.level()).getEntitiesOfClass(Monster.class, box).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private String getBiomeName(ServerPlayer player) {
        try {
            var keyOpt = player.level().getBiome(player.blockPosition()).unwrapKey();
            if (keyOpt.isEmpty()) return "unknown";
            String keyStr = keyOpt.get().toString();
            if (keyStr.contains(" / ")) {
                String path = keyStr.substring(keyStr.lastIndexOf(" / ") + 3).replace("]", "").trim();
                return path.contains(":") ? path.substring(path.indexOf(':') + 1) : path;
            }
            return "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getDimensionName(ServerPlayer player) {
        try {
            var dim = player.level().dimension();
            if (dim.equals(Level.NETHER)) return "the_nether";
            if (dim.equals(Level.END)) return "the_end";
            return "overworld";
        } catch (Exception e) {
            return "overworld";
        }
    }
}