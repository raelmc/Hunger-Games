package de.hungerGames.managers;

import de.hungerGames.HungerGames;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.WorldBorder;

import java.util.*;

public class GameManager {

    private final HungerGames plugin;

    private boolean gameActive = false;
    private int session = 0;

    private World world;
    private WorldBorder border;

    private BukkitTask gameTask;
    private BukkitTask borderTask;

    private double borderSize;
    private double borderTarget;

    private long sessionStart;

    private final Map<UUID, PlayerData> playerData = new HashMap<>();

    public GameManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    /* =====================================================
       GAME CONTROL
       ===================================================== */

    public void startGame() {
        if (gameActive) return;

        world = Bukkit.getWorld("world");
        if (world == null) {
            Bukkit.getLogger().severe("World 'world' not found!");
            return;
        }

        border = world.getWorldBorder();
        setupBorder();

        gameActive = true;
        session = 1;
        sessionStart = System.currentTimeMillis();

        initPlayers();
        startBorderTask();
        startGameTask();

        applySessionLogic();
        broadcast("§6§lHUNGER GAMES STARTET!");
    }

    private boolean countdownRunning = false;
    private int countdownTaskId = -1;

    public void startGameCountdown() {
        if (gameActive || countdownRunning) return;

        countdownRunning = true;
        final int[] time = {15};

        broadcast("§6§lHUNGER GAMES");
        broadcast("§7Startet in §e15 §7Sekunden!");

        countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin,
                () -> {

                    if (time[0] == 0) {
                        Bukkit.getScheduler().cancelTask(countdownTaskId);
                        countdownRunning = false;

                        broadcast("§a§lLos geht's!");
                        startGame();
                        return;
                    }

                    if (time[0] <= 5 || time[0] == 10 || time[0] == 15) {
                        broadcast("§7Start in §e" + time[0] + " §7Sekunden");
                        Bukkit.getOnlinePlayers().forEach(p ->
                                p.playSound(p.getLocation(),
                                        Sound.BLOCK_NOTE_BLOCK_PLING,
                                        1f, 1f)
                        );
                    }

                    time[0]--;

                },
                0L,
                20L
        );
    }

    public void stopGame() {
        gameActive = false;

        if (gameTask != null) gameTask.cancel();
        if (borderTask != null) borderTask.cancel();

        broadcast("§c§lHUNGER GAMES BEENDET!");
    }

    /* =====================================================
       BORDER
       ===================================================== */

    private void setupBorder() {
        border.setCenter(0, 0);
        border.setDamageAmount(1.0);
        border.setDamageBuffer(0);
        border.setWarningDistance(10);
    }

    private void startBorderTask() {
        borderTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gameActive) return;

            if (borderSize < borderTarget) {
                borderSize = Math.min(borderSize + 5, borderTarget);
                border.setSize(borderSize);
            }

            if (borderSize > borderTarget) {
                borderSize = Math.max(borderSize - 5, borderTarget);
                border.setSize(borderSize);
            }

        }, 0L, 10L);
    }

    /* =====================================================
       SESSIONS
       ===================================================== */

    public void nextSession() {
        if (!gameActive) return;

        session++;
        sessionStart = System.currentTimeMillis();

        applySessionLogic();
    }

    private void applySessionLogic() {
        broadcast("§6§lSESSION " + session + " STARTET!");

        switch (session) {

            case 1:
                borderSize = 10;
                borderTarget = 2500;
                border.setSize(borderSize);
                break;

            case 2:
            case 3:
            case 4:
            case 5:
                borderSize = 2500;
                borderTarget = 2500;
                border.setSize(borderSize);
                break;

            case 6:
                shrinkTo(2000);
                break;

            case 7:
                shrinkTo(1500);
                break;

            case 8:
                shrinkTo(1000);
                break;

            case 9:
                shrinkTo(500);
                break;

            case 10:
                broadcast("§4§l⚠ FINALE!");
                border.setSize(15, 120); // 2 Minuten animiert
                break;

            default:
                stopGame();
                break;
        }
    }


    private void shrinkTo(double size) {
        borderTarget = size;
        broadcast("§4⚠ BORDER SCHRUMPFT AUF " + (int) size);
    }

    /* =====================================================
       GAME LOOP
       ===================================================== */

    private void startGameTask() {
        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gameActive) return;

            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getScoreboardManager().updateScoreboard(player, this);
            }

        }, 0L, 20L);
    }

    /* =====================================================
       PLAYERS
       ===================================================== */

    private void initPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerData.put(player.getUniqueId(), new PlayerData(player));
            plugin.getScoreboardManager().createScoreboard(player);
        }
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public void playerDeath(Player player) {
        PlayerData data = getPlayerData(player);
        if (data == null) return;

        data.lives--;
        data.deaths++;

        Bukkit.broadcastMessage(
                "§c" + player.getName() + " §7ist gestorben! §8(§e" + data.getLives() + " §7/ 5 Leben§8)"
        );


        if (data.lives <= 0) {
            player.setGameMode(GameMode.SPECTATOR);
            Bukkit.broadcastMessage("§6" + player.getName() + " ist ausgeschieden!");
        }
    }


    public void addKill(Player killer) {
        PlayerData data = playerData.get(killer.getUniqueId());
        if (data != null) data.kills++;
    }

    /* =====================================================
       UTILS
       ===================================================== */

    private void broadcast(String msg) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(msg));
    }

    public int getSession() {
        return session;
    }

    public double getBorderSize() {
        return border.getSize();
    }

    public long getSessionTimeSeconds() {
        return (System.currentTimeMillis() - sessionStart) / 1000;
    }

    /* =====================================================
       PLAYER DATA
       ===================================================== */

    public static class PlayerData {
        private final Player player;
        private int lives = 5;
        private int kills = 0;
        private int deaths = 0;

        public PlayerData(Player player) {
            this.player = player;
        }

        public Player getPlayer() { return player; }
        public int getLives() { return lives; }
        public int getKills() { return kills; }
        public int getDeaths() { return deaths; }
    }

    public GameManager.PlayerData getPlayerData(Player player) {
        return playerData.get(player.getUniqueId());
    }

    public long getRemainingTimeSeconds() {
        long total = 60 * 60; // 1 Stunde
        long elapsed = (System.currentTimeMillis() - sessionStart) / 1000;
        return Math.max(0, total - elapsed);
    }

}
