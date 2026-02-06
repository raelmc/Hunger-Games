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
    private double borderSize;
    private double borderMaxSize;
    private int combatLogTimer;
    private BukkitTask gameTask;
    private BukkitTask borderTask;
    private Map<UUID, PlayerData> playerDataMap;
    private Map<UUID, UUID> teamMap;
    private World gameWorld;
    private long sessionStartTime;
    private int borderShrinkCounter;

    public GameManager(HungerGames plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
        this.teamMap = new HashMap<>();
        this.gameActive = false;
        this.currentSession = 0;
        this.borderSize = 2500;
        this.borderMaxSize = 2500;
        this.combatLogTimer = 20;
        this.borderShrinkCounter = 0;
    }

    public void startGame() {
        if (gameActive) return;
        
        gameActive = true;
        currentSession++;
        borderSize = 10;
        borderShrinkCounter = 0;
        sessionStartTime = System.currentTimeMillis();
        
        gameWorld = Bukkit.getWorld("world");
        broadcastColoredMessage("§6§l╔════════════════════════════════════╗");
        broadcastColoredMessage("§6§l║  HUNGER GAMES SESSION " + String.format("%02d", currentSession) + " GESTARTET  ║");
        broadcastColoredMessage("§6§l╚════════════════════════════════════╝");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = new PlayerData(player, 5, 0, 0);
            playerDataMap.put(player.getUniqueId(), data);
            plugin.getScoreboardManager().createScoreboard(player);
        }
        
        expandBorder();
        startGameLoop();
    }

    public void stopGame() {
        if (!gameActive) return;
        
        gameActive = false;
        if (gameTask != null) gameTask.cancel();
        if (borderTask != null) borderTask.cancel();
        
        broadcastColoredMessage("§c§l╔════════════════════════════════════╗");
        broadcastColoredMessage("§c§l║    SESSION " + String.format("%02d", currentSession) + " BEENDET         ║");
        broadcastColoredMessage("§c§l╚════════════════════════════════════╝");
        
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
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getScoreboardManager().updateScoreboard(player, this);
            }
            
        }, 0L, 20L);
    }

    private void expandBorder() {
        borderTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gameActive) return;
            
            if (borderSize < borderMaxSize) {
                borderSize += 5;
                updateWorldBorder();
            }
        }, 0L, 10L);
    }

    private void updateWorldBorder() {
        if (gameWorld != null) {
            gameWorld.getWorldBorder().setSize(borderSize);
        }
    }

    public void shrinkBorder() {
        if (currentSession >= 6 && currentSession <= 9 && borderShrinkCounter < 4) {
            borderMaxSize -= 500;
            borderSize = borderMaxSize;
            borderShrinkCounter++;
            updateWorldBorder();
            broadcastColoredMessage("§4§l⚠ BORDER SCHRUMPFT! Neue Größe: " + (int)borderSize);
        }
    }

    public void playerDeath(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data == null) return;
        
        data.removeLive();
        data.addDeath();
        dropPlayerHead(player);
        broadcastColoredMessage("§c" + player.getName() + " ist gestorben! (Leben: " + data.getLives() + "/5)");
        disablePvPForPlayer(player);
        
        if (data.getLives() <= 0) {
            player.teleport(player.getLocation());
            broadcastColoredMessage("§6" + player.getName() + " hat alle Leben verloren!");
        }
    }

    public void addKill(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data != null) {
            data.addKill();
            broadcastColoredMessage("§a" + player.getName() + " hat einen Kill! (Kills: " + data.getKills() + ");");
        }
    }

    private void dropPlayerHead(Player player) {
        if (gameWorld != null) {
            gameWorld.dropItem(player.getLocation(), new org.bukkit.inventory.ItemStack(org.bukkit.Material.PLAYER_HEAD));
        }
    }

    private void disablePvPForPlayer(Player player) {
        player.setInvulnerable(true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            player.setInvulnerable(false);
        }, 6000L);
    }

    public void broadcastColoredMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public void teleportAllPlayers(org.bukkit.Location location) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(location);
        }
        broadcastColoredMessage("§e✈ Alle Spieler wurden teleportiert!");
    }

    public void healAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setHealth(20.0);
            player.setFoodLevel(20);
        }
        broadcastColoredMessage("§a✓ Alle Spieler wurden geheilt!");
    }

    public void giveAllPlayersItems() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND, 10));
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_APPLE, 5));
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_CARROT, 10));
        }
        broadcastColoredMessage("§e✓ Alle Spieler erhielten Items!");
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public int getCurrentSession() {
        return currentSession;
    }

    public double getBorderSize() {
        return borderSize;
    }

    public double getBorderMaxSize() {
        return borderMaxSize;
    }

    public long getSessionDurationSeconds() {
        if (!gameActive) return 0;
        return (System.currentTimeMillis() - sessionStartTime) / 1000;
    }

    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    public class PlayerData {
        private Player player;
        private int lives;
        private int kills;
        private int deaths;

        public PlayerData(Player player, int lives, int kills, int deaths) {
            this.player = player;
            this.lives = lives;
            this.kills = kills;
            this.deaths = deaths;
        }

        public void removeLive() {
            this.lives--;
        }

        public void addKill() {
            this.kills++;
        }

        public void addDeath() {
            this.deaths++;
        }

        public int getLives() { return lives; }
        public int getKills() { return kills; }
        public int getDeaths() { return deaths; }
        public Player getPlayer() { return player; }
    }
}