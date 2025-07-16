package net.crumb.lobbyParkour.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.systems.LeaderboardUpdater;
import net.crumb.lobbyParkour.utils.ConfigManager;
import net.crumb.lobbyParkour.utils.TextFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class EntityRemove implements Listener {

    private static final LobbyParkour plugin = LobbyParkour.getInstance();
    private static final TextFormatter textFormatter = new TextFormatter();
    private static final LeaderboardUpdater updater = LeaderboardUpdater.getInstance();


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
        UUID uuid = entity.getUniqueId();

        if (isSuppressed(uuid)) {
            unsuppress(uuid);
            return;
        }

        try {
            ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
            Query query = new Query(database.getConnection());

            // START PLATE
            String mapName = query.getMapNameByStartUuid(uuid);
            if (mapName != null) {
                Location loc = query.getStartLocation(mapName);
                World world = loc.getWorld();
                Component startText = textFormatter.formatString(ConfigManager.getFormat().getStartPlate(), Map.of("parkour_name", mapName));
                Location textDisplayLocation = loc.clone().add(0.5, 1.0, 0.5);

                TextDisplay newStart = world.spawn(textDisplayLocation, TextDisplay.class, td -> {
                    td.text(startText);
                    td.setBillboard(Display.Billboard.CENTER);
                });
                query.updateStartEntityUuid(mapName, newStart.getUniqueId());
                return;
            }

            // END PLATE
            String mapName2 = query.getMapNameByEndUuid(uuid);
            if (mapName2 != null) {
                Location loc = query.getEndLocation(mapName2);
                World world = loc.getWorld();
                Component endText = textFormatter.formatString(ConfigManager.getFormat().getEndPlate(), Map.of("parkour_name", mapName2));
                Location textDisplayLocation = loc.clone().add(0.5, 1.0, 0.5);

                TextDisplay newEnd = world.spawn(textDisplayLocation, TextDisplay.class, td -> {
                    td.text(endText);
                    td.setBillboard(Display.Billboard.CENTER);
                });
                query.updateEndEntityUuid(mapName2, newEnd.getUniqueId());
                return;
            }

            // LEADERBOARD LINE
            Map<String, Object> lineInfo = query.getLeaderboardLineByUuid(uuid);

            if (lineInfo != null) {

                Location location = ((Location) lineInfo.get("location")).clone();
                int position = (int) lineInfo.get("position");
                int leaderboardId = (int) lineInfo.get("leaderboard_id");
                World world = location.getWorld();

                Entity newEntity;

                if (position == -1) {
                    // ItemDisplay (rotating item)
                    var displayItemConfig = ConfigManager.getFormat().getLeaderboard().getDisplayItem();
                    Material displayMaterial = displayItemConfig.getItem();
                    boolean enchantGlint = displayItemConfig.hasEnchantGlint();

                    ItemStack item = new ItemStack(displayMaterial);
                    ItemMeta meta = item.getItemMeta();
                    if (enchantGlint) {
                        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                    item.setItemMeta(meta);

                    ItemDisplay itemDisplay = world.spawn(location, ItemDisplay.class);
                    itemDisplay.setItemStack(item);
                    itemDisplay.setBillboard(Display.Billboard.VERTICAL);
                    itemDisplay.setTransformation(new Transformation(
                            new Vector3f(0.0f, 0.0f, 0.0f),
                            new Quaternionf(),
                            new Vector3f(0.5f, 0.5f, 0.5f),
                            new Quaternionf()
                    ));
                    newEntity = itemDisplay;



                } else {
                    // Title (position == 0) or empty line (position > 0)
                    Component text;
                    if (position == 0) {
                        String parkourName = query.getParkourNameByLeaderboard(leaderboardId);
                        text = textFormatter.formatString(ConfigManager.getFormat().getLeaderboard().getTitle(), Map.of("parkour_name", parkourName));
                    } else {
                        text = textFormatter.formatString(ConfigManager.getFormat().getLeaderboard().getEmptyLineStyle());
                    }

                    TextDisplay textDisplay = world.spawn(location, TextDisplay.class, td -> {
                        td.text(text);
                        td.setBillboard(Display.Billboard.CENTER);
                    });
                    newEntity = textDisplay;
                }

                query.updateLeaderboardLineEntityUuid(uuid, newEntity.getUniqueId());
                updater.updateCache();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
