package net.crumb.lobbyParkour.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.sql.SQLException;
import java.util.UUID;

public class EntityRemove implements Listener {

    private static final LobbyParkour plugin = LobbyParkour.getInstance();

    @EventHandler
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.TEXT_DISPLAY) {
            try {
                ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                Query query = new Query(database.getConnection());
                UUID entityUUID = entity.getUniqueId();

                String mapName = query.getMapNameByStartUuid(entityUUID);
                if (mapName != null) {
                    Location loc = query.getStartLocation(mapName);
                    World world = loc.getWorld();
                    Location textDisplayLocation = new Location(world, loc.getX() + 0.5, loc.getY() + 1.0, loc.getZ() + 0.5);
                    TextDisplay display = world.spawn(textDisplayLocation, TextDisplay.class, textDisplay -> {
                        textDisplay.text(MiniMessage.miniMessage().deserialize("<green>⚑</green> <white>"+mapName));
                        textDisplay.setBillboard(Display.Billboard.CENTER);
                    });
                    query.updateStartEntityUuid(mapName, display.getUniqueId());

                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
