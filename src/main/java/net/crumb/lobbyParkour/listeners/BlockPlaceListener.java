package net.crumb.lobbyParkour.listeners;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.utils.MMUtils;
import net.crumb.lobbyParkour.utils.Prefixes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
                MMUtils.sendMessage(player, "<hover:show_text:'<color:#52a3ff>✎</color> <color:#ffeb7a>Click to edit!</color>'><click:run_command:'/lpk'><color:#52a3ff>ⓘ</color> <color:#57ff65>A new parkour has been initialized. Do <white>/lpk</white> to edit your parkour.</color> <gray>(You can also click this message.)</gray></click></hover>");
                player.getInventory().remove(item);

                try {
                    ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                    Query query = new Query(database.getConnection());
                    String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.uu:HH:mm:ss"));

                    if (query.parkourMaps().size() == 28) {
                        MMUtils.sendMessage(player, "<color:#52a3ff>ⓘ</color> <color:#ff3358>You can't have more than 28 parkours!</color>");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                        event.setCancelled(true);
                        return;
                    }



                    World world = player.getWorld();
                    Location textDisplayLocation = new Location(world, location.getX() + 0.5, location.getY() + 1.0, location.getZ() + 0.5);
                    String parkourName = "New Parkour "+dateTime;
                    TextDisplay display = world.spawn(textDisplayLocation, TextDisplay.class, entity -> {
                        entity.text(MiniMessage.miniMessage().deserialize("<green>⚑</green> <white>"+parkourName));
                        entity.setBillboard(Display.Billboard.CENTER);
                    });

                    query.createParkour(parkourName, location, display.getUniqueId());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.1f, 2.0f);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

}
