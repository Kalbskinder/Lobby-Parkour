package net.crumb.lobbyParkour.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TextFormatter {

    /**
     * Formats a string with custom placeholders, PlaceholderAPI (if enabled), and MiniMessage.
     *
     * @param text   The input string with placeholders.
     * @param player The player (can be OfflinePlayer or Player).
     * @param data   A map of custom placeholders to replace (e.g., %checkpoint%).
     * @return The formatted Adventure Component.
     */
    public Component formatString(String text, @Nullable OfflinePlayer player, @Nullable Map<String, String> data) {
        String formatted = text;

        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                formatted = formatted.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            formatted = PlaceholderAPI.setPlaceholders(player, formatted);
        }

        return MiniMessage.miniMessage().deserialize(formatted);
    }

    public Component formatString(String text, @Nullable Player player, @Nullable Map<String, String> data) {
        return formatString(text, (OfflinePlayer) player, data);
    }

    public Component formatString(String text, @Nullable OfflinePlayer player) {
        return formatString(text, player, null);
    }

    public Component formatString(String text, @Nullable Player player) {
        return formatString(text, (OfflinePlayer) player, null);
    }

    public Component formatString(String text, @Nullable Map<String, String> data) {
        return formatString(text, null, data);
    }

    public Component formatString(String text) {
        return formatString(text, null, null);
    }
}
