package de.hungerGames.managers;

import de.hungerGames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {

    private HungerGames plugin;

    public ScoreboardManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    public void createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("hg", "dummy", "§6§lHUNGER GAMES");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Scores setzen
        objective.getScore("§e ").setScore(5);
        objective.getScore("§cLeben: ").setScore(4);
        objective.getScore("§eKills: ").setScore(3);
        objective.getScore("§6 ").setScore(2);
        objective.getScore("§aBorder schrumpft in: ").setScore(1);

        player.setScoreboard(scoreboard);
    }

    public void updateScoreboard(Player player) {
        // Scoreboard Update Logik
    }

    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }
}