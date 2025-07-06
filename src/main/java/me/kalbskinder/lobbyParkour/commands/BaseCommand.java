package me.kalbskinder.lobbyParkour.commands;

import me.kalbskinder.lobbyParkour.LobbyParkour;
import me.kalbskinder.lobbyParkour.guis.MainMenu;
import me.kalbskinder.lobbyParkour.utils.MMUtils;
import me.kalbskinder.lobbyParkour.utils.Prefixes;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BaseCommand implements CommandExecutor {
    private final LobbyParkour plugin;
    private static final String prefix = Prefixes.getPrefix();

    public BaseCommand(LobbyParkour plugin) {
        this.plugin = plugin;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        if (!player.hasPermission("lpk.admin")) {
            MMUtils.sendMessage(player, prefix + "<red>You don't have permission to execute this command.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            return true;
        }

        if (args.length == 0) {
            MainMenu.openMenu(player);
            return true;
        }

        switch (args[0]) {
            case "help" -> {

            }

            default -> {
                MMUtils.sendMessage(player, prefix + "<white>Use <yellow>/lpk help<white> for a list of commands.");
            }
        }

        return true;
    }
}
