package net.crumb.lobbyParkour.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.utils.ConfigManager;
import net.crumb.lobbyParkour.utils.TextFormatter;
import net.kyori.adventure.text.Component;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EntityRemove implements Listener {

    private static final LobbyParkour plugin = LobbyParkour.getInstance();
    private static final TextFormatter textFormatter = new TextFormatter();

    // 👇 Entities that should NOT be auto-respawned
    private static final Set<UUID> suppressed = new HashSet<>();

    public static void suppress(UUID uuid) {
        suppressed.add(uuid);
    }

    public static void unsuppress(UUID uuid) {
        suppressed.remove(uuid);
    }

    public static boolean isSuppressed(UUID uuid) {
        return suppressed.contains(uuid);
    }

    @EventHandler
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.TEXT_DISPLAY) return;

        UUID uuid = entity.getUniqueId();
        if (isSuppressed(uuid)) {
            unsuppress(uuid);
            return;
        }

        try {
            Map<String, Object> lineInfo;
            TextDisplay display;
            ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
            Query query = new Query(database.getConnection());

            String mapName = query.getMapNameByStartUuid(uuid);
            if (mapName != null) {

                Map<String, String> placeholders = Map.of(
                        "parkour_name", mapName
                );
                Component startText = textFormatter.formatString(ConfigManager.getFormat().getStartPlate(), placeholders);

                Location loc = query.getStartLocation(mapName);
                World world = loc.getWorld();
                Location textDisplayLocation = new Location(world, loc.getX() + 0.5, loc.getY() + 1.0, loc.getZ() + 0.5);
                display = world.spawn(textDisplayLocation, TextDisplay.class, textDisplay -> {
                    textDisplay.text(startText);
                    textDisplay.setBillboard(Display.Billboard.CENTER);
                });
                query.updateStartEntityUuid(mapName, display.getUniqueId());
            }

            String mapName2 = query.getMapNameByEndUuid(uuid);
            if (mapName2 != null) {
                Map<String, String> placeholders = Map.of(
                        "parkour_name", mapName2
                );
                Component endText = textFormatter.formatString(ConfigManager.getFormat().getEndPlate(), placeholders);

                Location loc = query.getEndLocation(mapName2);
                World world = loc.getWorld();
                Location textDisplayLocation = new Location(world, loc.getX() + 0.5, loc.getY() + 1.0, loc.getZ() + 0.5);
                display = world.spawn(textDisplayLocation, TextDisplay.class, textDisplay -> {
                    textDisplay.text(endText);
                    textDisplay.setBillboard(Display.Billboard.CENTER);
                });
                query.updateEndEntityUuid(mapName2, display.getUniqueId());
            }

            if ((lineInfo = query.getLeaderboardLineByUuid(uuid)) != null) {
                Component text;
                Location location = (Location)lineInfo.get("location");
                int position = (Integer)lineInfo.get("position");
                int leaderboardId = (Integer)lineInfo.get("leaderboard_id");
                if (position == 0) {
                    String parkourName = query.getParkourNameByLeaderboard(leaderboardId);
                    Map<String, String> placeholders = Map.of("parkour_name", parkourName);
                    text = textFormatter.formatString(ConfigManager.getFormat().getLeaderboard().getTitle(), placeholders);
                } else {
                    text = textFormatter.formatString(ConfigManager.getFormat().getLeaderboard().getEmptyLineStyle());
                }
                display = location.getWorld().spawn(location, TextDisplay.class, td -> {
                    td.text(text);
                    td.setBillboard(Display.Billboard.CENTER);
                });
                query.updateLeaderboardLineEntityUuid(display.getUniqueId());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
