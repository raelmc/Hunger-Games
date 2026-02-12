package de.hungerGames.managers;

import de.hungerGames.HungerGames;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private final HungerGames plugin;

    private boolean gameActive = false;
    private int session = 0;

    private World world;
    private WorldBorder border;

    private BukkitTask gameTask;
    private BukkitTask sessionTask;

    private long sessionStart;
    private long sessionDurationSeconds;

    private final Map<UUID, Long> pvpProtection = new HashMap<>();

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

        int minutes = plugin.getConfigManager()
                .getInt("sessions.duration-minutes", 60);

        sessionDurationSeconds = minutes * 60L;

        gameActive = true;
        session = 1;
        sessionStart = System.currentTimeMillis();

        initPlayers();
        startGameTask();
        startSessionTimer();

        applySessionLogic();

        broadcast("§6§lHUNGER GAMES STARTET!");
    }

    public void stopGame() {

        gameActive = false;

        if (gameTask != null) gameTask.cancel();
        if (sessionTask != null) sessionTask.cancel();

        broadcast("§c§lHUNGER GAMES BEENDET!");

        for (Player player : Bukkit.getOnlinePlayers()) {

            player.setGameMode(GameMode.ADVENTURE);
            player.setInvulnerable(true);
        }
    }

    public boolean hasPvpProtection(Player player) {
        Long end = pvpProtection.get(player.getUniqueId());
        if (end == null) return false;

        if (System.currentTimeMillis() > end) {
            pvpProtection.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    public void givePvpProtection(Player player, int seconds) {
        long end = System.currentTimeMillis() + (seconds * 1000L);
        pvpProtection.put(player.getUniqueId(), end);
    }


    public void prepareLobby() {

        world = Bukkit.getWorld("world");
        if (world == null) return;

        border = world.getWorldBorder();

        border.setCenter(0, 0);
        border.setSize(10);
        border.setDamageAmount(0);

        gameActive = false;
        session = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {

            player.teleport(new Location(world, 0.5, world.getHighestBlockYAt(0,0) + 1, 0.5));
            player.setGameMode(GameMode.ADVENTURE);
            player.setInvulnerable(true);

            plugin.getScoreboardManager().createScoreboard(player);
        }
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

    /* =====================================================
       SESSION SYSTEM
       ===================================================== */

    private void startSessionTimer() {

        sessionTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (!gameActive) return;

            if (getRemainingTimeSeconds() <= 0) {
                nextSession();
            }

        }, 20L, 20L);
    }

    public void nextSession() {

        if (!gameActive) return;

        session++;
        sessionStart = System.currentTimeMillis();

        applySessionLogic();
    }

    private void applySessionLogic() {

        broadcast("§6§lSESSION " + session + " STARTET!");

        int startSize = plugin.getConfigManager()
                .getInt("border.start-size", 2500);

        int finalSize = plugin.getConfigManager()
                .getInt("border.final-size", 15);

        switch (session) {

            case 1:
                // Von 10 → 2500
                border.setSize(10);
                border.setSize(startSize, 60);
                break;

            case 2:
            case 3:
            case 4:
            case 5:
                // Bleibt bei 2500
                border.setSize(startSize);
                break;

            case 6:
                border.setSize(2000, sessionDurationSeconds);
                break;

            case 7:
                border.setSize(1500, sessionDurationSeconds);
                break;

            case 8:
                border.setSize(1000, sessionDurationSeconds);
                break;

            case 9:
                border.setSize(500, sessionDurationSeconds);
                break;

            case 10:
                broadcast("§4§l⚠ FINALE!");
                border.setSize(15, sessionDurationSeconds);
                break;

            default:
                stopGame();
                break;
        }
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
       PLAYER SYSTEM
       ===================================================== */

    private void initPlayers() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            playerData.put(player.getUniqueId(), new PlayerData(player));

            // Survival aktivieren
            player.setGameMode(GameMode.SURVIVAL);

            // Unverwundbarkeit deaktivieren
            player.setInvulnerable(false);

            plugin.getScoreboardManager().createScoreboard(player);
        }
    }

    public void playerDeath(Player player) {

        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return;

        data.lives--;
        data.deaths++;


        if (data.lives >= 2) {
            Bukkit.broadcastMessage(
                    "§c" + player.getName() +
                            " §7ist gestorben! §8(§e" +
                            data.lives + " §7/ 5 Leben§8)"
            );
        }

        if (data.lives <= 0) {

            player.setGameMode(GameMode.SPECTATOR);
            player.setInvulnerable(true);

            Bukkit.broadcastMessage(
                    "§6" + player.getName() + " §7ist ausgeschieden!"
            );

            return; // Wichtig!
        }

        // Nur wenn noch Leben übrig
        givePvpProtection(player, 300);
        player.sendMessage("§eDu hast 5 Minuten PvP-Schutz!");
    }


    public void addKill(Player killer) {

        PlayerData data = playerData.get(killer.getUniqueId());
        if (data != null) data.kills++;
    }

    /* =====================================================
       UTIL
       ===================================================== */

    private void broadcast(String msg) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(msg));
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public int getSession() {
        return session;
    }

    public double getBorderSize() {
        return border.getSize();
    }

    public long getRemainingTimeSeconds() {

        long elapsed = (System.currentTimeMillis() - sessionStart) / 1000;
        return Math.max(0, sessionDurationSeconds - elapsed);
    }

    public void updateAllScoreboards() {
        plugin.getScoreboardManager().updateAllScoreboards(this);
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

    public PlayerData getPlayerData(Player player) {
        return playerData.get(player.getUniqueId());
    }
}
