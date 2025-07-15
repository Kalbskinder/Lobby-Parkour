package net.crumb.lobbyParkour.utils;

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

    public static void sendMessage(Player player, String message, MessageType messageType) {
        Component prefix = Component.empty();
        Component parsed;

        switch (messageType) {
            case INFO:
                prefix = mm.deserialize("<color:#52a3ff>ⓘ</color> ");
                parsed = mm.deserialize("<color:#57ff65>" + message + "</color>");
                break;
            case WARNING:
                prefix = mm.deserialize("<color:#ffd321>⚠</color> ");
                parsed = mm.deserialize("<color:#ffeb7a>" + message + "</color>");
                break;
            case ERROR:
                prefix = mm.deserialize("<color:#ad1f39>☒</color> ");
                parsed = mm.deserialize("<color:#ff3358>" + message + "</color>");
                break;
            case NONE:
                player.sendMessage(mm.deserialize(message));
                return;
            default:
                player.sendMessage(mm.deserialize(message));
                return;
        }

        player.sendMessage(prefix.append(parsed));
    }
}
