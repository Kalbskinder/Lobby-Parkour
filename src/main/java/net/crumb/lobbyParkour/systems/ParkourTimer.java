package net.crumb.lobbyParkour.systems;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.utils.ConfigManager;
import net.crumb.lobbyParkour.utils.TextFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class ParkourTimer {
    private static boolean looping = false;
    private static final LobbyParkour plugin = LobbyParkour.getInstance();
    private static final String actionbar = ConfigManager.getFormat().getActionBar()
            .replace("%timer%", ConfigManager.getFormat().getTimer());
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final TextFormatter textFormatter = new TextFormatter();

    public static boolean isLooping() {
        return looping;
    }

    public static String formatTimer(float time, String message) {
        int totalMs = (int) (time * 1000);
        int minutes = (totalMs / 1000) / 60;
        int seconds = (totalMs / 1000) % 60;
        int millis = totalMs % 1000 / 10;

        return message
                .replace("%m%", String.format("%02d", minutes))
                .replace("%s%", String.format("%02d", seconds))
                .replace("%ms%", String.format("%02d", millis))

                // TODO: Replace these with the real checkpoints count
                .replace("%checkpoint%", "0")
                .replace("%checkpoint_total%", "0");
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
                    // Display actionbar
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        String formattedTime = formatTimer(session.getElapsedSeconds(), actionbar);
                        Map<String, String> placeholders = Map.of(
                                "parkour_name", session.getParkourName(),
                                "player_name", player.getName()
                        );
                        Component finalActionbar = textFormatter.formatString(formattedTime, player, placeholders);
                        player.sendActionBar(finalActionbar);
                    }
                });
            }
        }.runTaskTimer(plugin, 0L, 1L); // 1L = alle 1 Tick
    }
}

