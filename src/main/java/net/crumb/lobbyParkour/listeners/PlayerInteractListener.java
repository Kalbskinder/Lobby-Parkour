package net.crumb.lobbyParkour.listeners;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.guis.MapManageMenu;
import net.crumb.lobbyParkour.utils.MMUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.SQLException;

import static net.crumb.lobbyParkour.utils.PressurePlates.isPressurePlate;

public class PlayerInteractListener implements Listener {
    private static final LobbyParkour plugin = LobbyParkour.getInstance();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK && player.hasPermission("lpk.admin")) {
            Block block = event.getClickedBlock();
            if (block == null) return;
            Location location = block.getLocation();
            if (location == null) return;
            String parkourName = "";

            try {
                ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                Query query = new Query(database.getConnection());
                parkourName = query.getMapnameByPkSpawn(location);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if (parkourName.isEmpty()) return;
            MapManageMenu.openMenu(player, parkourName);
        }
        if (event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            if (block != null && isPressurePlate(block.getType())) {
                Location location = block.getLocation();
                String parkourName = "";

                try {
                    ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                    Query query = new Query(database.getConnection());
                    parkourName = query.getMapnameByPkSpawn(location);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                if (parkourName.isEmpty()) return;
                MMUtils.sendMessage(player, "Parkour: "+parkourName);
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<color:#7ae0ff>00:00:000</color> <color:#39aacc>⌚</color>   <dark_gray>|</dark_gray>   <color:#54ff7f><color:#57ff65>0</color></color><color:#b8b8b8>/10</color> <green>⚑</green>"));
            }
        }
    }
}
