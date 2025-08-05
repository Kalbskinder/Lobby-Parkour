package net.crumb.lobbyParkour.systems;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.listeners.EntityRemove;
import net.crumb.lobbyParkour.utils.ConfigManager;

import net.crumb.lobbyParkour.utils.TextFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
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
    private static final TextFormatter textFormatter = new TextFormatter();

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
            List<Map.Entry<UUID, Integer>> titleUUIDs = query.getTitleLines();
            cache.put("itemUUID", itemUUIDs);
            cache.put("titleUUID", titleUUIDs);
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
            format.put("line-" + (i + 1), (line == null || line.isEmpty()) ? format.get("default-line-style") : line);
        }

        format.put("display-enabled", displayItem.isEnabled());
        format.put("display-item", displayItem.getItem());
        format.put("display-glint", displayItem.hasEnchantGlint());
        format.put("leaderboard-update", settings.getLeaderboardUpdateRate());
        format.put("leaderboard-query-update", settings.getLeaderboardQueryRate());
    }

    public void updateStatic() {
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
        } else {
            Set<UUID> uuids = new HashSet<>((List<UUID>) cache.get("itemUUID"));
            if (!uuids.isEmpty()) {
                for (UUID uuid : uuids) {
                    EntityRemove.suppress(uuid);
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity != null) {
                        entity.remove();
                    }
                }
                updateCache();
            }
        }

        Set<Map.Entry<UUID, Integer>> titleEntries = new HashSet<>((List<Map.Entry<UUID, Integer>>) cache.get("titleUUID"));
        if (!titleEntries.isEmpty()) {
            try {
                ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                Query query = new Query(database.getConnection());

                for (Map.Entry<UUID, Integer> entry : titleEntries) {
                    UUID uuid = entry.getKey();
                    int leaderboardId = entry.getValue();

                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity == null || entity.isDead()) continue;
                    if (!(entity instanceof org.bukkit.entity.TextDisplay textDisplay)) continue;

                    String parkourName;
                    try {
                        parkourName = query.getParkourNameByLeaderboard(leaderboardId);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (parkourName == null) {
                        getLogger().warning("Parkour name is null for leaderboard ID " + leaderboardId);
                        continue;
                    }

                    String rawTitle = (String) format.get("title");
                    Component formattedTitle = textFormatter.formatString(rawTitle, Map.of("parkour_name", parkourName));

                    if (!textDisplay.text().equals(formattedTitle)) {
                        textDisplay.text(formattedTitle);
                    }
                }

                database.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Object> getCache() {
        Map<String, Object> map = new HashMap<>();
        map.put("cache", cache);
        map.put("format", format);
        return map;
    }

    private static String formatTimer(float time) {
        int totalMs = (int) (time * 1000);
        int minutes = (totalMs / 1000) / 60;
        int seconds = (totalMs / 1000) % 60;
        int millis = totalMs % 1000 / 10;

        String timerFormat = ConfigManager.getFormat().getTimer();
        return timerFormat
                .replace("%m%", String.format("%02d", minutes))
                .replace("%s%", String.format("%02d", seconds))
                .replace("%ms%", String.format("%02d", millis));
    }

    public void updateTimes(int parkourId) {
        try {
            ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
            Query query = new Query(database.getConnection());

            List<Map.Entry<UUID, Float>> times = query.getParkourTimes(parkourId);
            times.sort(Comparator.comparingDouble(Map.Entry::getValue)); // Ensure best times first

            int maxDisplayed = (Integer) format.get("maximum-displayed");
            if (times.size() > maxDisplayed) {
                times = new ArrayList<>(times.subList(0, maxDisplayed));
            }

// Map leaderboard line positions to their TextDisplay UUIDs
            Map<Integer, UUID> positionToEntityUUID = new HashMap<>();
            Map<Integer, List<Map.Entry<UUID, Integer>>> allLeaderboards = query.getAllTimesLinesForParkour(parkourId);

            for (Map.Entry<Integer, List<Map.Entry<UUID, Integer>>> leaderboardEntry : allLeaderboards.entrySet()) {
                List<Map.Entry<UUID, Integer>> lineEntries = leaderboardEntry.getValue();

                // Map position -> UUID
                for (Map.Entry<UUID, Integer> lineInfo : lineEntries) {
                    positionToEntityUUID.put(lineInfo.getValue(), lineInfo.getKey());
                }

                for (int position = 1; position <= maxDisplayed; position++) {
                    UUID uuid = positionToEntityUUID.get(position);
                    if (uuid == null) continue;

                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity == null || entity.isDead()) continue;
                    if (!(entity instanceof org.bukkit.entity.TextDisplay textDisplay)) continue;

                    if (position - 1 < times.size()) {
                        Map.Entry<UUID, Float> timeEntry = times.get(position - 1);
                        UUID playerUuid = timeEntry.getKey();
                        float time = timeEntry.getValue();

                        String playerName = Bukkit.getOfflinePlayer(playerUuid).getName();
                        if (playerName == null) playerName = "Steve";

                        String rawLine = (String) format.get("line-" + position);
                        if (rawLine == null || rawLine.isBlank()) {
                            rawLine = (String) format.get("default-line-style");
                        }

                        String formattedTime = formatTimer(time);
                        rawLine = rawLine.replace("%timer%", formattedTime);

                        Component newText = textFormatter.formatString(
                                rawLine,
                                Map.of("player_name", playerName, "position", String.valueOf(position))
                        );

                        if (!textDisplay.text().equals(newText)) {
                            textDisplay.text(newText);
                        }
                    } else {
                        Component emptyText = textFormatter.formatString(
                                (String) format.get("empty-line-style")
                        );

                        if (!textDisplay.text().equals(emptyText)) {
                            textDisplay.text(emptyText);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
