package com.adenium.zanatenunchi.observers;

import com.adenium.zanatenunchi.blackboard.Blackboard;
import com.adenium.zanatenunchi.blackboard.BotEvent;
import com.adenium.zanatenunchi.blackboard.BotEvent.Impact;
import com.adenium.zanatenunchi.util.LanguageManager;
import com.adenium.zanatenunchi.lang.IBotLanguageProvider;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CombatObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger("CombatObserver");

    private final Blackboard blackboard;
    private final Map<String, Long> lastStrongHitMs = new ConcurrentHashMap<>();
    private static final long STRONG_HIT_COOLDOWN_MS = 8_000L;

    public CombatObserver(Blackboard blackboard) {
        this.blackboard = blackboard;
    }

    public void register() {
        registerPlayerDeath();
        registerPlayerDamage();
        registerMobKills();
        LOGGER.info("CombatObserver registrado");
    }

    private void registerPlayerDeath() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayer player)) return;
            String langCode = "en_us"; // Test MVP
            IBotLanguageProvider langProvider = LanguageManager.getProvider(langCode);
            String cause = damageSource.getMsgId();
            String attackerName = damageSource.getEntity() != null
                    ? damageSource.getEntity().getName().getString() : null;
            String playerName = player.getName().getString();
            String promptText = langProvider.getDeathEvent(cause, attackerName, playerName);
            BotEvent event = new BotEvent(
                    player.getUUID(),
                    promptText,
                    Impact.HIGH,
                    System.currentTimeMillis(),
                    true
            );
            blackboard.publishEvent(event);
        });
    }

    private void registerPlayerDamage() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (!(entity instanceof ServerPlayer player)) return;
            if (player.isDeadOrDying()) return;
            if (damageTaken < 6.0f) return;
            String uuid = player.getUUID().toString();
            long now = System.currentTimeMillis();
            long last = lastStrongHitMs.getOrDefault(uuid, 0L);
            if ((now - last) < STRONG_HIT_COOLDOWN_MS) return;
            lastStrongHitMs.put(uuid, now);
            String langCode = "en_us"; // Test MVP
            IBotLanguageProvider langProvider = LanguageManager.getProvider(langCode);
            String attackerName = source.getEntity() != null
                    ? source.getEntity().getName().getString() : null;
            String cause = source.getMsgId();
            int hearts = (int) Math.ceil(player.getHealth() / 2);
            int damage = (int) Math.ceil(damageTaken / 2);
            String playerName = player.getName().getString();
            String promptText = langProvider.getDamageEvent(playerName, cause, attackerName, damage, hearts);
            BotEvent event = new BotEvent(
                    player.getUUID(),
                    promptText,
                    Impact.NORMAL,
                    System.currentTimeMillis(),
                    false
            );
            blackboard.publishEvent(event);
        });
    }

    private void registerMobKills() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(damageSource.getEntity() instanceof ServerPlayer player)) {
                return;
            }
            if (entity instanceof ServerPlayer) {
                return;
            }
            String className = entity.getClass().getSimpleName();
            Impact impact = Impact.LOW;
            // Filtros de probabilidad
            if (className.equals("Creeper")) {
                if (ThreadLocalRandom.current().nextInt(100) >= 25) {
                    return;
                }
            } else if (!className.equals("EnderDragon") && !className.equals("WitherBoss") && !className.equals("ElderGuardian") && !className.equals("Warden") && !className.equals("Evoker")) {
                if (ThreadLocalRandom.current().nextInt(100) >= 8) {
                    return;
                }
            } else {
                impact = Impact.HIGH; // Jefes y mobs difíciles
            }
            String langCode = "en_us"; // Test MVP
            IBotLanguageProvider langProvider = LanguageManager.getProvider(langCode);
            String mobDisplayName = entity.getName().getString();
            String playerName = player.getName().getString();
            String promptText = langProvider.getMobKillEvent(playerName, mobDisplayName, className);
            BotEvent event = new BotEvent(
                    player.getUUID(),
                    promptText,
                    impact,
                    System.currentTimeMillis(),
                    false
            );
            blackboard.publishEvent(event);
        });
    }
}