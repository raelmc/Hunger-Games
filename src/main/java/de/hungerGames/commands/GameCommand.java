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
            player.sendMessage("§c/hg setup - Spiel setup lobby");
            player.sendMessage("§c/hg setlives <player> <anzahl> - Setze die spieler eines bestimmten Spielers fest");
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

        if (args[0].equalsIgnoreCase("setup")) {
            if (!player.isOp()) {
                player.sendMessage("§cKeine Berechtigung!");
                return true;
            }
            plugin.getGameManager().prepareLobby();
            return true;
        }

        if (args[0].equalsIgnoreCase("setlives")) {

            if (!player.isOp()) {
                player.sendMessage("§cKeine Berechtigung!");
                return true;
            }

            if (args.length < 3) {
                player.sendMessage("§cUsage: /hg setlives <player> <anzahl>");
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[1]);

            if (target == null) {
                player.sendMessage("§cSpieler nicht gefunden!");
                return true;
            }

            int lives;

            try {
                lives = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cBitte eine gültige Zahl eingeben!");
                return true;
            }

            plugin.getGameManager().setLives(target, lives);

            player.sendMessage("§aDu hast die Leben von §e" + target.getName() + " §aauf §6" + lives + " §agesetzt.");

            return true;
        }

        return false;
    }
}