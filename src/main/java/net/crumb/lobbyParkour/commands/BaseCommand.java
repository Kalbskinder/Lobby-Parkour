package net.crumb.lobbyParkour.commands;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.guis.MainMenu;
import net.crumb.lobbyParkour.utils.MMUtils;
import net.crumb.lobbyParkour.utils.MessageType;
import net.crumb.lobbyParkour.utils.Prefixes;
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
            MMUtils.sendMessage(player, "You don't have the permission to execute this command!", MessageType.ERROR);
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
                MMUtils.sendMessage(player, "Unknown command! Do <white>/lpk help</white> to see the list of available commands.", MessageType.WARNING);
            }
        }

        return true;
    }
}
