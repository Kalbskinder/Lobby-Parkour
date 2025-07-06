package net.crumb.lobbyParkour.listeners;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.guis.MapManageMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.SQLException;

public class ShiftClickListener implements Listener {
    private static final LobbyParkour plugin = LobbyParkour.getInstance();

    @EventHandler
    public void onShiftClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
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
        if (!player.hasPermission("lpk.admin")) return;
        MapManageMenu.openMenu(player, parkourName);
    }
}
