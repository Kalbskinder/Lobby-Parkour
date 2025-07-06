package me.kalbskinder.lobbyParkour.listeners;

import me.kalbskinder.lobbyParkour.LobbyParkour;
import me.kalbskinder.lobbyParkour.database.ParkoursDatabase;
import me.kalbskinder.lobbyParkour.database.Query;
import me.kalbskinder.lobbyParkour.utils.MMUtils;
import me.kalbskinder.lobbyParkour.utils.Prefixes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BlockPlaceListener implements Listener {
    private final LobbyParkour plugin;

    public BlockPlaceListener(LobbyParkour plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("lbk.admin")) return;

        ItemStack item = event.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        if (!meta.hasDisplayName()) return;

        String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        Location location = event.getBlock().getLocation();

        switch (itemName) {
            case "Parkour Start" -> {
                MMUtils.sendMessage(player, "<green>Parkour start has been placed");
                player.getInventory().remove(item);

                try {
                    ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                    Query query = new Query(database.getConnection());
                    String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.uu:HH:mm:ss"));

                    if (query.parkourMaps().size() == 28) {
                        MMUtils.sendMessage(player, Prefixes.getPrefix() + "<red>You can't have more than 28 parkour maps!");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                        event.setCancelled(true);
                        return;
                    }

                    query.createParkour("New Parkour " + dateTime, location);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.1f, 2.0f);

                    World world = player.getWorld();
                    Location textDisplayLocation = new Location(world, location.getX() + 0.5, location.getY() + 1.0, location.getZ() + 0.5);
                    TextDisplay display = world.spawn(textDisplayLocation, TextDisplay.class, entity -> {
                        entity.text(MiniMessage.miniMessage().deserialize("<green>New Parkour" + dateTime));
                        entity.setBillboard(Display.Billboard.CENTER);
                    });
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

}
