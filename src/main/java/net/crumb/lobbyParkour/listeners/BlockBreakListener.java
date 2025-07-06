package net.crumb.lobbyParkour.listeners;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.sql.SQLException;

public class BlockBreakListener implements Listener {
    private final LobbyParkour plugin;

    public BlockBreakListener(LobbyParkour plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Location blockLocation = block.getLocation();
        Location aboveLocation = blockLocation.clone().add(0, 1, 0);

        try {
            ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
            Query query = new Query(database.getConnection());

            // Check block location
            String parkourName = query.getMapnameByPkSpawn(blockLocation);
            if (parkourName != null) {
                Location startLocation = query.getStartLocation(parkourName);
                if (startLocation != null && startLocation.equals(blockLocation)) {
                    e.setCancelled(true);
                    return;
                }
            }

            // Check above block
            String parkourNameAbove = query.getMapnameByPkSpawn(aboveLocation);
            if (parkourNameAbove != null) {
                Location startLocationAbove = query.getStartLocation(parkourNameAbove);
                if (startLocationAbove != null && startLocationAbove.equals(aboveLocation)) {
                    e.setCancelled(true);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
