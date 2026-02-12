package de.hungerGames.listeners;

import de.hungerGames.HungerGames;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageListener implements Listener {

    private final HungerGames plugin;

    public PlayerDamageListener(HungerGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {

        if (!plugin.getGameManager().isGameActive()) return;

        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        if (plugin.getGameManager().hasPvpProtection(damaged)) {
            event.setCancelled(true);
            damager.sendMessage("§cDieser Spieler hat noch PvP-Schutz!");
            return;
        }

        if (plugin.getGameManager().hasPvpProtection(damager)) {
            event.setCancelled(true);
            damager.sendMessage("§cDu hast noch PvP-Schutz!");
        }
    }
}
