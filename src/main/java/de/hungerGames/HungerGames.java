package de.hungerGames;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import de.hungerGames.listeners.PlayerDeathListener;
import de.hungerGames.listeners.PlayerDamageListener;
import de.hungerGames.commands.GameCommand;
import de.hungerGames.managers.GameManager;
import de.hungerGames.managers.ConfigManager;
import de.hungerGames.managers.ScoreboardManager;

public class HungerGames extends JavaPlugin {

    private static HungerGames instance;
    private GameManager gameManager;
    private ConfigManager configManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Konfiguration laden
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Manager initialisieren
        gameManager = new GameManager(this);
        scoreboardManager = new ScoreboardManager(this);
        
        // Listener registrieren
        registerListeners();
        
        // Commands registrieren
        registerCommands();
        
        getLogger().info("§a✓ Hunger Games Plugin geladen!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.stopGame();
        }
        getLogger().info("§c✗ Hunger Games Plugin deaktiviert!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
    }

    private void registerCommands() {
        getCommand("hg").setExecutor(new GameCommand(this));
    }

    public static HungerGames getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}