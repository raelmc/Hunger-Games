package de.hungerGames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent; // richtiges Event importieren
import de.hungerGames.managers.GameManager;

public class BlockBreakListener implements Listener {

    private final GameManager gameManager;

    public BlockBreakListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!gameManager.isBuildEnabled()) {
            event.setCancelled(true);
        }
    }
}
