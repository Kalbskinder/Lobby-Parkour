package net.crumb.lobbyParkour.utils;

import net.crumb.lobbyParkour.LobbyParkour;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtils {
    private static final LobbyParkour plugin = LobbyParkour.getInstance();

    public static void playSoundSequence(Player player, Sound sound, float volume, float pitch, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }, delayTicks);
    }
}
