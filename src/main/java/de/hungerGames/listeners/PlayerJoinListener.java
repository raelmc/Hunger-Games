package de.hungerGames.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import de.hungerGames.managers.GameManager;
import de.hungerGames.HungerGames; // Wichtig für dein Plugin

public class PlayerJoinListener implements Listener {

    private final HungerGames plugin;
    private final GameManager gameManager;

    public PlayerJoinListener(HungerGames plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getScoreboardManager().createScoreboard(player);

        if (!gameManager.isGameActive()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.setInvulnerable(true);
        }
    }
}
