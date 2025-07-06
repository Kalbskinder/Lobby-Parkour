package me.kalbskinder.lobbyParkour.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

public class MMUtils {
    private static final MiniMessage mm = MiniMessage.miniMessage();

    // Sends a player a message with the minimessage format
    public static void sendMessage(Player player, String message) {
        Component parsed = mm.deserialize(message);
        player.sendMessage(parsed);
    }

    public static void sendMessageAsComponent(Player player, Component message) {
        player.sendMessage(message);
    }

    // Displays a player a title with the minimessage format
    public static void displayTitle (Player player, String title, String subtitle, float fadeIn, float stay, float fadeOut) {
        Component mainTitle = mm.deserialize(title);
        Component sub = mm.deserialize(subtitle);

        Title.Times times = Title.Times.times(
                Duration.ofMillis((long) (fadeIn * 1000)),
                Duration.ofMillis((long) (stay * 1000)),
                Duration.ofMillis((long) (fadeOut * 1000))
        );

        Title titleObject = Title.title(mainTitle, sub, times);
        player.showTitle(titleObject); // Show the title
    }
}
