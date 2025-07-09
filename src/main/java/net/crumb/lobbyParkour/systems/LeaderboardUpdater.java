package net.crumb.lobbyParkour.systems;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.utils.ConfigManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LeaderboardUpdater {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final Map<String, Object> format = new ConcurrentHashMap<>();
    private BukkitRunnable spinTask;
    private BukkitRunnable updateTask;
    private static final LobbyParkour plugin = LobbyParkour.getInstance();

    public void updateCache() {
        try {
            ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
            Query query = new Query(database.getConnection());
            List<UUID> itemUUIDs = query.getItemLinesUuid();
            cache.put("itemUUID", itemUUIDs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFormat() {
        ConfigManager.Format.Leaderboard leaderboard = ConfigManager.getFormat().getLeaderboard();
        ConfigManager.Format.Leaderboard.DisplayItem displayItem = leaderboard.getDisplayItem();
        ConfigManager.Settings settings = ConfigManager.getSettings();

        format.clear();
        format.put("title", leaderboard.getTitle());
        format.put("default-line-style", leaderboard.getDefaultLineStyle());
        format.put("personal-best-style", leaderboard.getPersonalBestStyle());
        format.put("empty-line-style", leaderboard.getEmptyLineStyle());
        format.put("maximum-displayed", leaderboard.getMaximumDisplayed());
        format.put("personal-best-enabled", leaderboard.isPersonalBestEnabled());

        List<String> lines = leaderboard.getLines();
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            format.put("line-" + (i + 1), (line == null || line.isEmpty()) ? "" : line);
        }

        format.put("display-enabled", displayItem.isEnabled());
        format.put("display-item", displayItem.getItem());
        format.put("display-glint", displayItem.hasEnchantGlint());
        format.put("leaderboard-update", settings.getLeaderboardUpdateRate());
    }

    public Map<String, Object> getCache() {
        updateCache();
        updateFormat();
        Map<String, Object> map = new HashMap<>();
        map.put("cache", cache);
        map.put("format", format);
        return map;
    }

    public void startUpdating() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Implementation of update logic goes here
            }
        };

        Object rateObj = format.get("leaderboard-update");
        long updateRate = (rateObj instanceof Number) ? ((Number) rateObj).longValue() : 100L;

        updateTask.runTaskTimer(plugin, 0L, updateRate);
    }

    public void startSpinning() {
        if (!ConfigManager.getFormat().getLeaderboard().getDisplayItem().isSpinEnabled()) {
            stopSpinning();
            return;
        }

        if (spinTask != null) {
            spinTask.cancel();
        }

        spinTask = new BukkitRunnable() {
            double angle = 0.0;

            @Override
            public void run() {
                List<UUID> uuids = (List<UUID>) cache.get("itemUUID");
                if (uuids == null || uuids.isEmpty()) return;

                angle += Math.toRadians(3.6);
                if (angle >= Math.PI * 2) {
                    angle -= Math.PI * 2;
                }

                Quaternionf rotation = new Quaternionf().rotateY((float) angle);
                for (UUID uuid : uuids) {
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity instanceof ItemDisplay itemDisplay && !entity.isDead()) {
                        itemDisplay.setTransformation(new Transformation(
                                new Vector3f(0.0f, 0.0f, 0.0f),
                                rotation,
                                new Vector3f(0.5f, 0.5f, 0.5f),
                                new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
                        ));
                    }
                }
            }
        };

        spinTask.runTaskTimer(plugin, 0L, 1L);
    }

    public void stopSpinning() {
        if (spinTask != null) {
            spinTask.cancel();
            spinTask = null;
        }
    }
}
