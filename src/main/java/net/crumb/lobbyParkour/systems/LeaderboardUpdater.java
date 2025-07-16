package net.crumb.lobbyParkour.systems;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.utils.ConfigManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.bukkit.Bukkit.getLogger;

public class LeaderboardUpdater {
    private static final LeaderboardUpdater instance = new LeaderboardUpdater();
    public static LeaderboardUpdater getInstance() {
        return instance;
    }

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
        format.put("leaderboard-query-update", settings.getLeaderboardQueryRate());
    }

    public Map<String, Object> getCache() {
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
                if ((boolean) format.get("display-enabled")) {
                    Set<UUID> uuids = new HashSet<>((List<UUID>) cache.get("itemUUID"));
                    if (!uuids.isEmpty()) {
                        Material expectedMaterial = (Material) format.get("display-item");
                        boolean glintEnabled = (boolean) format.get("display-glint");

                        for (UUID uuid : uuids) {
                            Entity entity = Bukkit.getEntity(uuid);
                            if (!(entity instanceof ItemDisplay itemDisplay)) continue;

                            if (entity.isDead()) {
                                entity.remove(); // Extra safety
                                continue;
                            }

                            ItemStack stack = itemDisplay.getItemStack();
                            ItemMeta meta = stack.getItemMeta();

                            // Remove entity if material doesn't match
                            if (stack.getType() != expectedMaterial) {
                                itemDisplay.remove();
                                continue;
                            }

                            boolean hasGlint = meta.hasEnchant(Enchantment.UNBREAKING);

                            if (glintEnabled && !hasGlint) {
                                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                stack.setItemMeta(meta);
                                itemDisplay.setItemStack(stack);
                            } else if (!glintEnabled && hasGlint) {
                                meta.removeEnchant(Enchantment.UNBREAKING);
                                stack.setItemMeta(meta);
                                itemDisplay.setItemStack(stack);
                            }
                        }
                    }
                }
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
                Set<UUID> uuids = new HashSet<>((List<UUID>) cache.get("itemUUID"));

                if (uuids.isEmpty()) return;

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
