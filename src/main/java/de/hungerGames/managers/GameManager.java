package de.hungerGames.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import de.hungerGames.HungerGames;
import java.util.*;

public class GameManager {

    private HungerGames plugin;
    private boolean gameActive;
    private int currentSession;
    private int borderSize;
    private int borderMaxSize;
    private int combatLogTimer;
    private BukkitTask gameTask;
    private Map<UUID, PlayerData> playerDataMap;
    private Map<UUID, UUID> teamMap;
    private World gameWorld;

    public GameManager(HungerGames plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
        this.teamMap = new HashMap<>();
        this.gameActive = false;
        this.currentSession = 0;
        this.borderSize = 2500;
        this.borderMaxSize = 2500;
        this.combatLogTimer = 20;
    }

    public void startGame() {
        if (gameActive) return;
        
        gameActive = true;
        currentSession++;
        borderSize = 10;
        
        gameWorld = Bukkit.getWorld("world");
        broadcastColoredMessage("§6§l=== HUNGER GAMES SESSION " + currentSession + " GESTARTET ===");
        
        // Spieler initialisieren
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerDataMap.put(player.getUniqueId(), new PlayerData(player, 5, 0));
        }
        
        // Border expandieren
        expandBorder();
        
        // Game Loop starten
        startGameLoop();
    }

    public void stopGame() {
        if (!gameActive) return;
        
        gameActive = false;
        if (gameTask != null) gameTask.cancel();
        
        broadcastColoredMessage("§c§l=== SESSION " + currentSession + " BEENDET ===");
        broadcastColoredMessage("§ePvP wurde deaktiviert! Alle Spieler werden gekickt...");
        
        // PvP deaktivieren
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setCanPickupItems(false);
        }
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer("§c§lSession beendet!");
            }
        }, 60L);
    }

    private void startGameLoop() {
        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gameActive) return;
            
            // Scoreboard aktualisieren
            plugin.getScoreboardManager().updateAllScoreboards();
            
            // Border Warnung (1 Minute vorher)
            // Implementierung folgt
            
        }, 0L, 20L);
    }

    private void expandBorder() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (borderSize < borderMaxSize) {
                borderSize += 50;
                updateWorldBorder();
            }
        }, 0L, 40L); // Schnelle Expansion
    }

    private void updateWorldBorder() {
        if (gameWorld != null) {
            gameWorld.getWorldBorder().setSize(borderSize);
        }
    }

    public void shrinkBorder() {
        if (currentSession >= 6 && currentSession <= 9) {
            borderSize -= 500;
            updateWorldBorder();
            broadcastColoredMessage("§4§l⚠ BORDER SCHRUMPFT! Neue Größe: " + borderSize);
        }
    }

    public void playerDeath(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data == null) return;
        
        data.removeLive();
        data.addDeath();
        
        // Player-Head droppen
        dropPlayerHead(player);
        
        broadcastColoredMessage("§c" + player.getName() + " ist gestorben! (Leben: " + data.getLives() + "/5);
        
        // PvP disablen für 5 Minuten
        disablePvPForPlayer(player);
        
        if (data.getLives() <= 0) {
            // Als Zuschauer respawnen
            player.teleport(player.getLocation());
            broadcastColoredMessage("§6" + player.getName() + " hat alle Leben verloren!");
        }
    }

    private void dropPlayerHead(Player player) {
        // Spieler-Kopf droppen (später implementiert)
    }

    private void disablePvPForPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        player.setInvulnerable(true);
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            player.setInvulnerable(false);
        }, 6000L); // 5 Minuten
    }

    public void broadcastColoredMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public int getCurrentSession() {
        return currentSession;
    }

    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    public class PlayerData {
        private Player player;
        private int lives;
        private int kills;
        private long combatLogTime;

        public PlayerData(Player player, int lives, int kills) {
            this.player = player;
            this.lives = lives;
            this.kills = kills;
            this.combatLogTime = 0;
        }

        public void removeLive() {
            this.lives--;
        }

        public void addKill() {
            this.kills++;
        }

        public void addDeath() {
            // Tod verarbeiten
        }

        public int getLives() { return lives; }
        public int getKills() { return kills; }
        public Player getPlayer() { return player; }
    }
}