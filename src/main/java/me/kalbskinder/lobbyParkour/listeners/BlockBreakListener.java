package me.kalbskinder.lobbyParkour.listeners;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import me.kalbskinder.lobbyParkour.LobbyParkour;
import me.kalbskinder.lobbyParkour.database.ParkoursDatabase;
import me.kalbskinder.lobbyParkour.database.Query;
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

        try {
            ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
            Query query = new Query(database.getConnection());
            String parkourName = query.getMapnameByPkSpawn(blockLocation);
            if (parkourName == null) return;
            Location start_cpLocation = query.getStartLocation(parkourName);
            if (start_cpLocation == null) return;

            if (start_cpLocation.equals(blockLocation)) {
                e.setCancelled(true);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
