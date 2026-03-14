package com.adenium.zanatenunchi.observers;

import com.adenium.zanatenunchi.blackboard.Blackboard;
import com.adenium.zanatenunchi.blackboard.BotEvent;
import com.adenium.zanatenunchi.blackboard.BotEvent.Impact;
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

            String cause = damageSource.getMsgId();

            // Extraer nombre del atacante (funciona bien server-side, confirmado con mobs)
            String attackerName = damageSource.getEntity() != null
                    ? damageSource.getEntity().getName().getString() : null;

            String prompt = buildDeathPrompt(cause, attackerName);

            BotEvent event = new BotEvent(
                    player.getUUID(),
                    prompt,
                    Impact.HIGH,
                    System.currentTimeMillis(),
                    true
            );

            blackboard.publishEvent(event);
        });
    }

    /**
     * Construye un prompt de muerte rico usando damageSource (más confiable que getCombatTracker).
     */
    private String buildDeathPrompt(String cause, String attackerName) {
        String prompt = switch (cause) {
            case "mob" -> attackerName != null
                    ? "Muerte verificada: " + attackerName + " mató a [nombre]."
                    : "Muerte verificada: un mob mató a [nombre].";
            case "player" -> attackerName != null
                    ? "Muerte verificada: " + attackerName + " (otro jugador) mató a [nombre]."
                    : "Muerte verificada: otro jugador mató a [nombre].";
            case "arrow" -> attackerName != null
                    ? "Muerte verificada: " + attackerName + " mató a [nombre] con una flecha."
                    : "Muerte verificada: [nombre] murió atravesado por una flecha.";
            case "fall" -> "Muerte verificada: [nombre] murió por caída desde muy alto.";
            case "outOfWorld" -> "Muerte verificada: [nombre] cayó al vacío y murió.";
            case "drown" -> "Muerte verificada: [nombre] se ahogó.";
            case "lava" -> "Muerte verificada: [nombre] cayó en lava y murió.";
            case "inFire", "onFire" -> "Muerte verificada: [nombre] murió quemado.";
            case "explosion", "explosion.player" -> attackerName != null
                    ? "Muerte verificada: [nombre] murió por la explosión de " + attackerName + "."
                    : "Muerte verificada: [nombre] murió por una explosión.";
            case "starve" -> "Muerte verificada: [nombre] murió de hambre.";
            case "magic" -> attackerName != null
                    ? "Muerte verificada: [nombre] murió por magia de " + attackerName + "."
                    : "Muerte verificada: [nombre] murió por magia.";
            case "wither" -> "Muerte verificada: [nombre] murió por efecto wither.";
            case "anvil" -> "Muerte verificada: un yunque aplastó a [nombre].";
            case "fallingBlock" -> "Muerte verificada: un bloque cayó sobre [nombre] y lo mató.";
            case "flyIntoWall" -> "Muerte verificada: [nombre] se estrelló volando con la elytra.";
            case "lightningBolt" -> "Muerte verificada: un rayo fulminó a [nombre].";
            case "cactus" -> "Muerte verificada: [nombre] murió pinchado por un cactus.";
            case "freeze" -> "Muerte verificada: [nombre] murió congelado.";
            case "hotFloor" -> "Muerte verificada: [nombre] murió por caminar sobre magma.";
            case "dragonBreath" -> "Muerte verificada: [nombre] murió por el aliento del dragón.";
            case "thorns" -> attackerName != null
                    ? "Muerte verificada: [nombre] murió por las espinas de " + attackerName + "."
                    : "Muerte verificada: [nombre] murió por daño de espinas.";
            default -> {
                if (attackerName != null) {
                    yield "Muerte verificada: [nombre] murió (" + cause + ") por culpa de " + attackerName + ".";
                }
                yield "Muerte verificada: [nombre] acaba de morir (" + cause + ").";
            }
        };
        return prompt + " Reacciona sin inventar detalles.";
    }

    private void registerPlayerDamage() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (!(entity instanceof ServerPlayer player)) return;
            if (player.isDeadOrDying()) return;

            // ~3 corazones de daño real o más (6 puntos) merece aviso.
            if (damageTaken < 6.0f) return;

            String uuid = player.getUUID().toString();
            long now = System.currentTimeMillis();
            long last = lastStrongHitMs.getOrDefault(uuid, 0L);
            if ((now - last) < STRONG_HIT_COOLDOWN_MS) return;
            lastStrongHitMs.put(uuid, now);

            String attackerName = source.getEntity() != null
                    ? source.getEntity().getName().getString() : null;
            String cause = source.getMsgId();
            int hearts = (int) Math.ceil(player.getHealth() / 2);
            int damage = (int) Math.ceil(damageTaken / 2);

            String prompt;
            if ("fall".equals(cause)) {
                prompt = "Golpe verificado: [nombre] sufrió una caída fuerte (-" + damage + " corazones). Le quedan " + hearts + " corazones.";
            } else if (attackerName != null) {
                prompt = "Golpe verificado: " + attackerName + " golpeó fuerte a [nombre] (-" + damage + " corazones). Le quedan " + hearts + " corazones.";
            } else {
                prompt = "Golpe verificado: [nombre] recibió un golpe fuerte (-" + damage + " corazones). Le quedan " + hearts + " corazones.";
            }

            BotEvent event = new BotEvent(
                    player.getUUID(),
                    prompt,
                    Impact.NORMAL,
                    System.currentTimeMillis()
            );

            blackboard.publishEvent(event);
        });
    }

    private void registerMobKills() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(damageSource.getEntity() instanceof ServerPlayer player)) return;
            if (entity instanceof ServerPlayer) return;

            String mobDisplayName = entity.getName().getString();
            String className = entity.getClass().getSimpleName();

            String prompt;
            Impact impact;

            if (className.equals("EnderDragon")) {
                prompt = "Combate verificado: [nombre] mató al Ender Dragon. Hazaña épica.";
                impact = Impact.HIGH;
            } else if (className.equals("WitherBoss")) {
                prompt = "Combate verificado: [nombre] derrotó al Wither.";
                impact = Impact.HIGH;
            } else if (className.equals("ElderGuardian")) {
                prompt = "Combate verificado: [nombre] derrotó al Elder Guardian.";
                impact = Impact.HIGH;
            } else if (className.equals("Warden")) {
                prompt = "Combate verificado: [nombre] mató a un Warden. Eso es casi imposible.";
                impact = Impact.HIGH;
            } else if (className.equals("Evoker")) {
                prompt = "Combate verificado: [nombre] mató a un " + mobDisplayName + ".";
                impact = Impact.NORMAL;
            } else if (className.equals("Creeper")) {
                if (ThreadLocalRandom.current().nextInt(100) >= 25) return;
                prompt = "Combate verificado: [nombre] mató a un " + mobDisplayName + " antes de que explotara.";
                impact = Impact.LOW;
            } else {
                if (ThreadLocalRandom.current().nextInt(100) >= 8) return;
                prompt = "Combate verificado: [nombre] mató a un " + mobDisplayName + ".";
                impact = Impact.LOW;
            }

            BotEvent event = new BotEvent(
                    player.getUUID(),
                    prompt,
                    impact,
                    System.currentTimeMillis()
            );

            blackboard.publishEvent(event);
        });
    }
}

