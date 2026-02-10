package de.hungerGames.managers;

import de.hungerGames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {

    private final HungerGames plugin;

    public ScoreboardManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    /* =====================================================
       CREATE
       ===================================================== */

    public void createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective(
                "hg",
                "dummy",
                "§6§lHUNGER GAMES "
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Spacer lines (wichtig für schönes Layout)
        objective.getScore("§7 ").setScore(10);

        Team time = scoreboard.registerNewTeam("time");
        time.addEntry("§fZeit:");
        objective.getScore("§fZeit:").setScore(8);

        objective.getScore("§7  ").setScore(7);

        Team lives = scoreboard.registerNewTeam("lives");
        lives.addEntry("§cLeben:");
        objective.getScore("§cLeben:").setScore(5);

        Team kills = scoreboard.registerNewTeam("kills");
        kills.addEntry("§eKills:");
        objective.getScore("§eKills:").setScore(4);

        objective.getScore("§7   ").setScore(2);

        Team border = scoreboard.registerNewTeam("border");
        border.addEntry("§aBorder:");
        objective.getScore("§aBorder:").setScore(1);

        player.setScoreboard(scoreboard);
    }

    /* =====================================================
       UPDATE
       ===================================================== */

    public void updateScoreboard(Player player, GameManager gameManager) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) return;

        GameManager.PlayerData data = gameManager.getPlayerData(player);
        if (data == null) return;

        Team lives = scoreboard.getTeam("lives");
        Team kills = scoreboard.getTeam("kills");
        Team border = scoreboard.getTeam("border");
        Team time = scoreboard.getTeam("time");

        if (lives != null) {
            lives.setSuffix(" §f" + data.getLives());
        }

        if (kills != null) {
            kills.setSuffix(" §f" + data.getKills());
        }

        if (border != null) {
            border.setSuffix(" §f" + (int) gameManager.getBorderSize());
        }

        if (time != null) {
            long seconds = gameManager.getRemainingTimeSeconds();
            time.setSuffix(" §f" + formatTime(seconds));
        }
    }

    /* =====================================================
       UPDATE ALL
       ===================================================== */

    public void updateAllScoreboards(GameManager gameManager) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player, gameManager);
        }
    }

    /* =====================================================
       TIME FORMAT
       ===================================================== */

    private String formatTime(long seconds) {
        long min = seconds / 60;
        long sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }
}
