package de.hungerGames.listeners;

import de.hungerGames.HungerGames;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private HungerGames plugin;

    public PlayerDeathListener(HungerGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getGameManager().isGameActive()) return;

        plugin.getGameManager().playerDeath(event.getEntity());
    }
}