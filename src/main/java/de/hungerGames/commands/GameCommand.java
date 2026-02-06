package de.hungerGames.commands;

import de.hungerGames.HungerGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommand implements CommandExecutor {

    private HungerGames plugin;

    public GameCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§c/hg start - Spiel starten");
            player.sendMessage("§c/hg stop - Spiel beenden");
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (!player.isOp()) {
                player.sendMessage("§cKeine Berechtigung!");
                return true;
            }
            plugin.getGameManager().startGame();
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            if (!player.isOp()) {
                player.sendMessage("§cKeine Berechtigung!");
                return true;
            }
            plugin.getGameManager().stopGame();
            return true;
        }

        return false;
    }
}