package com.adenium.zanatenunchi.observers;

import com.adenium.zanatenunchi.blackboard.Blackboard;
import com.adenium.zanatenunchi.blackboard.BotEvent;
import com.adenium.zanatenunchi.blackboard.BotEvent.Impact;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.monster.Monster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerStatusObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerStatusObserver");

    private final Blackboard blackboard;
    private final Map<String, DangerSnapshot> lastDangerSnapshotByPlayer = new HashMap<>();
    private final Map<String, Long> lastDangerEventMsByPlayer = new HashMap<>();
    private int tickCounter = 0;

    private static final long NORMAL_DANGER_MIN_INTERVAL_MS = 9_000L;
    private static final long CRITICAL_DANGER_MIN_INTERVAL_MS = 8_000L;
    private static final long SAME_SIGNATURE_SUPPRESS_MS = 24_000L;

    private record DangerSnapshot(int hostiles, int hearts, int nearestDistance, String composition, boolean critical) {}

    public PlayerStatusObserver(Blackboard blackboard) {
        this.blackboard = blackboard;
    }

    public void register() {
        registerBlockBreak();
        registerTickChecks();
        LOGGER.info("PlayerStatusObserver registrado");
    }

    private void registerBlockBreak() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return;

            String reaction = null;
            Impact impact = Impact.NORMAL;
            int y = pos.getY();

            if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
                reaction = "Hallazgo verificado: [nombre] minó diamantes en Y=" + y + ". Reacciona con emoción sin inventar.";
                impact = Impact.HIGH;
            } else if (state.is(Blocks.ANCIENT_DEBRIS)) {
                reaction = "Hallazgo verificado: [nombre] encontró ancient debris en Y=" + y + " en el Nether. Reacciona.";
                impact = Impact.HIGH;
            } else if (state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE)) {
                if (ThreadLocalRandom.current().nextInt(100) < 40) {
                    reaction = "Hallazgo verificado: [nombre] encontró esmeraldas en Y=" + y + ". Comenta algo breve.";
                }
            } else if (state.is(Blocks.SPAWNER)) {
                reaction = "Hallazgo verificado: [nombre] encontró un spawner en Y=" + y + ". Reacciona.";
            }

            if (reaction != null) {
                BotEvent event = new BotEvent(
                        serverPlayer.getUUID(),
                        reaction,
                        impact,
                        System.currentTimeMillis()
                );
                blackboard.publishEvent(event);
            }
        });
    }

    private void registerTickChecks() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server) {
        if (server.getPlayerList().getPlayers().isEmpty()) return;

        int tick = ++tickCounter;
        if (tick % 40 != 0) return;

        checkLowHealth(server);
        checkLowFood(server);
        checkNearbyDanger(server);

        if (tick % 200 == 0) {
            checkSpontaneousChat(server);
        }

        if (tickCounter >= 144000) {
            tickCounter = 0;
        }
    }

    private void checkLowHealth(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();

            if (!blackboard.hasPlayer(uuid)) continue;
            if (player.isDeadOrDying()) continue;

            float health = player.getHealth();
            boolean isLow = health <= 6.0f && health > 0;
            boolean wasLow = blackboard.isLowHealthWarned(uuid);

            if (isLow && !wasLow) {
                blackboard.setLowHealthWarned(uuid, true);
                int hearts = (int) Math.ceil(health / 2);

                BotEvent event = new BotEvent(
                        player.getUUID(),
                        "¡[nombre] está casi muerto, le quedan solo " + hearts + " corazones! Reacciona ya.",
                        Impact.HIGH,
                        System.currentTimeMillis(),
                        true
                );
                blackboard.publishEvent(event);
            } else if (!isLow) {
                blackboard.setLowHealthWarned(uuid, false);
            }
        }
    }

    private void checkLowFood(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();

            if (!blackboard.hasPlayer(uuid)) continue;

            int food = player.getFoodData().getFoodLevel();
            boolean isLow = food <= 6;
            boolean wasLow = blackboard.isLowFoodWarned(uuid);

            if (isLow && !wasLow) {
                blackboard.setLowFoodWarned(uuid, true);

                BotEvent event = new BotEvent(
                        player.getUUID(),
                        "¡[nombre] se está muriendo de hambre! Reacciona.",
                        Impact.HIGH,
                        System.currentTimeMillis(),
                        true
                );
                blackboard.publishEvent(event);
            } else if (!isLow) {
                blackboard.setLowFoodWarned(uuid, false);
            }
        }
    }

    private void checkNearbyDanger(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();

            if (!blackboard.hasPlayer(uuid)) continue;
            if (player.isDeadOrDying()) continue;

            try {
                AABB box = new AABB(
                        player.getX() - 12, player.getY() - 5, player.getZ() - 12,
                        player.getX() + 12, player.getY() + 5, player.getZ() + 12
                );

                var hostiles = ((ServerLevel) player.level()).getEntitiesOfClass(Monster.class, box);

                boolean danger = !hostiles.isEmpty();
                boolean wasInDanger = blackboard.isDangerWarned(uuid);

                if (danger) {
                    blackboard.setDangerWarned(uuid, true);
                    int hearts = (int) Math.ceil(player.getHealth() / 2);
                    double nearestDistance = hostiles.stream()
                            .mapToDouble(mob -> mob.distanceTo(player))
                            .min()
                            .orElse(0.0D);
                    int nearestDistanceRounded = Math.max(1, (int) Math.round(nearestDistance));

                    java.util.LinkedHashMap<String, Integer> counts = new java.util.LinkedHashMap<>();
                    for (var mob : hostiles) {
                        counts.merge(mob.getName().getString(), 1, Integer::sum);
                    }

                    boolean criticalDanger = hostiles.size() >= 4
                            || nearestDistance <= 2.0D
                            || hearts <= 2
                            || (hearts <= 3 && nearestDistance <= 6.0D);
                    DangerSnapshot currentSnapshot = new DangerSnapshot(
                            hostiles.size(),
                            hearts,
                            nearestDistanceRounded,
                            buildCompositionSignature(counts),
                            criticalDanger
                    );

                    long now = System.currentTimeMillis();
                    if (wasInDanger) {
                        DangerSnapshot previousSnapshot = lastDangerSnapshotByPlayer.get(uuid);
                        long lastEventMs = lastDangerEventMsByPlayer.getOrDefault(uuid, 0L);
                        if (!shouldPublishDangerUpdate(previousSnapshot, currentSnapshot, now - lastEventMs)) {
                            continue;
                        }
                    }

                    // Si ya estaba marcado en peligro, solo reenviar cuando hay escalada critica.
                    // (ahora se decide con snapshot + ventanas de tiempo para evitar spam)

                    String prompt;
                    if (hostiles.size() > 3) {
                        // Muchos mobs: listar tipos únicos
                        java.util.LinkedHashSet<String> types = new java.util.LinkedHashSet<>();
                        for (var mob : hostiles) {
                            types.add(mob.getName().getString());
                            if (types.size() >= 3) break;
                        }
                        prompt = "Alerta verificada: hay " + hostiles.size() + " mobs hostiles cerca de [nombre] (" +
                                String.join(", ", types) + "). Tiene " + hearts + " corazones. El más cercano está a ~" +
                                nearestDistanceRounded + " bloques. Reacciona de inmediato.";
                    } else if (hostiles.size() > 1) {
                        String mobList = counts.entrySet().stream()
                                .map(entry -> entry.getValue() + "× " + entry.getKey())
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("mobs hostiles");
                        prompt = "Alerta verificada: hay " + mobList + " cerca de [nombre]. Tiene " + hearts +
                                " corazones. El más cercano está a ~" + nearestDistanceRounded + " bloques." +
                                (criticalDanger ? " Reacciona de inmediato." : "");
                    } else {
                        String mobName = hostiles.get(0).getName().getString();
                        prompt = "Alerta verificada: hay un " + mobName + " cerca de [nombre]. Tiene " + hearts +
                                " corazones. Está a ~" + nearestDistanceRounded + " bloques." +
                                (criticalDanger ? " Reacciona de inmediato." : "");
                    }

                    BotEvent event = new BotEvent(
                            player.getUUID(),
                            prompt,
                            criticalDanger ? Impact.HIGH : Impact.NORMAL,
                            now,
                            criticalDanger
                    );
                    blackboard.publishEvent(event);
                    lastDangerSnapshotByPlayer.put(uuid, currentSnapshot);
                    lastDangerEventMsByPlayer.put(uuid, now);
                } else if (!danger) {
                    blackboard.setDangerWarned(uuid, false);
                    lastDangerSnapshotByPlayer.remove(uuid);
                    lastDangerEventMsByPlayer.remove(uuid);
                }
            } catch (Exception e) {
                LOGGER.debug("Error detectando mobs cercanos: {}", e.getMessage());
            }
        }
    }

    private boolean shouldPublishDangerUpdate(DangerSnapshot previous, DangerSnapshot current, long elapsedMs) {
        if (previous == null) {
            return true;
        }
        if (isDangerEscalation(previous, current)) {
            return true;
        }

        long minInterval = current.critical ? CRITICAL_DANGER_MIN_INTERVAL_MS : NORMAL_DANGER_MIN_INTERVAL_MS;
        if (elapsedMs < minInterval) {
            return false;
        }

        if (buildDangerSignature(previous).equals(buildDangerSignature(current))
                && elapsedMs < SAME_SIGNATURE_SUPPRESS_MS) {
            return false;
        }
        return true;
    }

    private boolean isDangerEscalation(DangerSnapshot previous, DangerSnapshot current) {
        if (!previous.critical && current.critical) {
            return true;
        }
        if (current.hostiles >= previous.hostiles + 1) {
            return true;
        }
        if (current.nearestDistance <= previous.nearestDistance - 2) {
            return true;
        }
        if (current.hearts <= previous.hearts - 2) {
            return true;
        }
        return false;
    }

    private String buildDangerSignature(DangerSnapshot snapshot) {
        int hostileBucket = snapshot.hostiles >= 4 ? 4 : snapshot.hostiles;
        String distanceBucket = snapshot.nearestDistance <= 2 ? "near" : snapshot.nearestDistance <= 5 ? "mid" : "far";
        String heartsBucket = snapshot.hearts <= 3 ? "critical" : snapshot.hearts <= 6 ? "low" : "ok";
        return hostileBucket + "|" + distanceBucket + "|" + heartsBucket;
    }

    private String buildCompositionSignature(Map<String, Integer> counts) {
        if (counts.isEmpty()) {
            return "none";
        }
        return counts.entrySet().stream()
                .sorted((a, b) -> {
                    int countCompare = Integer.compare(b.getValue(), a.getValue());
                    if (countCompare != 0) {
                        return countCompare;
                    }
                    return a.getKey().compareToIgnoreCase(b.getKey());
                })
                .limit(3)
                .map(e -> e.getKey() + ":" + e.getValue())
                .reduce((a, b) -> a + "|" + b)
                .orElse("none");
    }

    private void checkSpontaneousChat(MinecraftServer server) {
        long now = System.currentTimeMillis();
        long spontMinMs = 7 * 60_000L;
        long spontMaxMs = 16 * 60_000L;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();

            if (!blackboard.hasPlayer(uuid)) continue;
            if (player.isDeadOrDying()) continue;

            Long next = blackboard.getNextSpontMs(uuid);
            if (next == null) {
                blackboard.setNextSpontMs(uuid, now + spontMinMs + ThreadLocalRandom.current().nextLong(spontMaxMs - spontMinMs));
                continue;
            }

            if (now < next) continue;

            blackboard.setNextSpontMs(uuid, now + spontMinMs + ThreadLocalRandom.current().nextLong(spontMaxMs - spontMinMs));

            long dayTime = server.overworld().getDayTime() % 24000;
            String timeDesc = dayTime < 1000 ? "acaba de amanecer" : dayTime < 6000 ? "es de mañana" :
                    dayTime < 12000 ? "es mediodía" : dayTime < 13500 ? "está atardeciendo" :
                            dayTime < 18000 ? "anocheció" : "es medianoche";

            String biome = getBiomeName(player).replace("_", " ");
            String dim = getDimensionName(player).replace("the_", "").replace("_", " ");
            int hearts = (int) Math.ceil(player.getHealth() / 2);
            int food = player.getFoodData().getFoodLevel();

            String estado = "[nombre] está en el " + dim + ", " + timeDesc +
                    ", bioma: " + biome +
                    ", vida: " + hearts + " corazones, hambre: " + food + "/20." +
                    " Di algo espontáneo y natural sobre la situación.";

            BotEvent event = new BotEvent(
                    player.getUUID(),
                    estado,
                    Impact.NORMAL,
                    System.currentTimeMillis()
            );
            blackboard.publishEvent(event);
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
            if (dim.equals(net.minecraft.world.level.Level.NETHER)) return "the_nether";
            if (dim.equals(net.minecraft.world.level.Level.END)) return "the_end";
            return "overworld";
        } catch (Exception e) {
            return "overworld";
        }
    }
}


