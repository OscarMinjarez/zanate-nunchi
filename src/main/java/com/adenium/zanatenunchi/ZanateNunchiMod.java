package com.adenium.zanatenunchi;

import com.adenium.zanatenunchi.ai.OllamaClient;
import com.adenium.zanatenunchi.ai.OllamaHealthCheck;
import com.adenium.zanatenunchi.ai.PersonalityGenerator;
import com.adenium.zanatenunchi.ai.PromptManager;
import com.adenium.zanatenunchi.blackboard.Blackboard;
import com.adenium.zanatenunchi.command.ZanateCommandHandler;
import com.adenium.zanatenunchi.config.ModConfig;
import com.adenium.zanatenunchi.controller.BotController;
import com.adenium.zanatenunchi.data.DataManager;
import com.adenium.zanatenunchi.observers.ChatObserver;
import com.adenium.zanatenunchi.observers.CombatObserver;
import com.adenium.zanatenunchi.observers.LanguageObserver;
import com.adenium.zanatenunchi.observers.PlayerStatusObserver;
import com.adenium.zanatenunchi.observers.WorldObserver;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ZanateNunchiMod implements ModInitializer {

    public static final String MOD_ID = "zanatenunchi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private Blackboard blackboard;
    private DataManager dataManager;
    private BotController botController;
    private OllamaHealthCheck healthCheck;
    private boolean ollamaAvailable = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando Zanate Nunchi...");

        // Registrar comandos del mod
        ZanateCommandHandler.register();

        ModConfig config = ModConfig.getInstance();
        LOGGER.info("Configuración cargada: modelo={}, url={}", config.getOllamaModel(), config.getOllamaUrl());

        initializeComponents();

        healthCheck = new OllamaHealthCheck(config);
        ollamaAvailable = healthCheck.checkConnection();
        if (ollamaAvailable) {
            healthCheck.checkModelAvailable();
        }

        registerObservers();
        registerServerLifecycle();

        LOGGER.info("Bot Ollama inicializado con arquitectura Blackboard. {}",
                ollamaAvailable ? "Ollama disponible." : "⚠ Ollama NO disponible.");
    }

    private void initializeComponents() {
        blackboard = Blackboard.getInstance();
        OllamaClient ollamaClient = OllamaClient.getInstance();
        PromptManager promptManager = PromptManager.getInstance();
        dataManager = new DataManager(blackboard);

        PersonalityGenerator personalityGenerator = new PersonalityGenerator(blackboard, ollamaClient, dataManager);
        botController = new BotController(blackboard, ollamaClient, promptManager, dataManager);

        // Observers (Se registran como locales para limpiar campos de clase si no se usan fuera)
        new LanguageObserver(blackboard).register();
        new CombatObserver(blackboard).register();
        new WorldObserver(blackboard).register();
        new PlayerStatusObserver(blackboard).register();

        ChatObserver chatObserver = new ChatObserver(blackboard);
        chatObserver.setPersonalityGenerator(personalityGenerator);
        chatObserver.register();
    }

    private void registerObservers() {
        botController.register();
    }

    private void registerServerLifecycle() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
    }

    private void onServerStarted(MinecraftServer server) {
        blackboard.setCurrentServer(server);

        if (!ollamaAvailable) {
            ollamaAvailable = healthCheck.checkConnection();
        }

        // TODO: Obtener nombre del mundo y seed cuando las mappings sean correctas
        // String worldName = server.getWorldData().getLevelName();
        // long seed = server.overworld().getSeed();
        
        String worldName = "mundo";
        long seed = 0;

        dataManager.initializeForWorld(worldName, seed);
        dataManager.loadData();
        blackboard.clearAllState();
    }

    private void onServerStopping(MinecraftServer server) {
        dataManager.saveData();
        blackboard.clearAllState();
        blackboard.setCurrentServer(null);
    }
}