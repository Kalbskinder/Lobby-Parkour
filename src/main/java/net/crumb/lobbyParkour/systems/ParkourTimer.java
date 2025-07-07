package net.crumb.lobbyParkour.systems;

import net.crumb.lobbyParkour.LobbyParkour;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ParkourTimer {
    private static boolean looping = false;
    private static final LobbyParkour plugin = LobbyParkour.getInstance();
    private static final String startMessage =  plugin.getConfig().getString("formatting.timer", "<color:#7ae0ff>%m%:%s%:%ms%</color> <color:#39aacc>⌚</color>   <dark_gray>|</dark_gray>   <color:#54ff7f><color:#57ff65>%cp%</color></color><color:#b8b8b8>/%cp-max%</color> <green>⚑</green>");
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static boolean isLooping() {
        return looping;
    }

    private static String formatTimer(float time) {
        int totalMs = (int) (time * 1000);
        int minutes = (totalMs / 1000) / 60;
        int seconds = (totalMs / 1000) % 60;
        int millis = totalMs % 1000;

        return startMessage
                .replace("%m%", String.format("%02d", minutes))
                .replace("%s%", String.format("%02d", seconds))
                .replace("%ms%", String.format("%02d", millis))

                // TODO: Replace these with the real checkpoints count
                .replace("%cp%", "0")
                .replace("%cp-max%", "0");
    }

    public static void setLooping(boolean looping) {
        ParkourTimer.looping = looping;
    }

    public static void start() {
        if (looping) return;
        setLooping(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (ParkourSessionManager.getSessions().isEmpty()) {
                    setLooping(false);
                    cancel();
                    return;
                }

                ParkourSessionManager.getSessions().forEach((uuid, session) -> {
                    float currentTime = session.getTime();
                    session.setTime(currentTime + 0.05f); // 1 Tick = 0.05 seconds

                    // Display actionbar
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        String formattedTime = formatTimer(session.getTime());
                        player.sendActionBar(miniMessage.deserialize(formattedTime));
                    }
                });
            }
        }.runTaskTimer(plugin, 0L, 1L); // 1L = alle 1 Tick
    }
}

