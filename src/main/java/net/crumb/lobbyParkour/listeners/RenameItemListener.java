package net.crumb.lobbyParkour.listeners;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.guis.MapListMenu;
import net.crumb.lobbyParkour.utils.MMUtils;
import net.crumb.lobbyParkour.utils.Prefixes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;

import java.sql.SQLException;
import java.util.UUID;

public class RenameItemListener implements Listener {
    private static final LobbyParkour plugin = LobbyParkour.getInstance();
    private static final String prefix = Prefixes.getPrefix();

    @EventHandler
    public void onItemRename(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getView().getType() != InventoryType.ANVIL) return;

        String title = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        if (!title.equalsIgnoreCase("Rename Parkour")) return;
        if (!player.hasPermission("lpk.admin")) return;

        if (e.getSlot() == 0) {
            e.setCancelled(true);
            return;
        }
        if (e.getSlot() != 2) return;

        AnvilView inventory = (AnvilView) e.getView();
        String itemName = PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(inventory.getRenameText()));
        String oldName = inventory.getItem(0).getItemMeta().getDisplayName();

        player.closeInventory();
        player.getInventory().clear();

        try {
            ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
            Query query = new Query(database.getConnection());

            if (query.parkourExists(itemName)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<color:#52a3ff>ⓘ</color> <color:#ff3358>A parkour with the same already exists!</color>"));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                player.closeInventory();
                return;
            }

            query.renameParkour(oldName, itemName);
            UUID entityUUID = query.getStartEntityUuid(itemName);
            Location startLocation = query.getStartLocation(itemName);
            World world = startLocation.getWorld();
            Entity entity = world.getEntity(entityUUID);
            TextDisplay textDisplay = (entity instanceof TextDisplay) ? (TextDisplay) entity : null;
            assert textDisplay != null;
            textDisplay.text(MiniMessage.miniMessage().deserialize("<green>⚑</green> <white>"+itemName));
            player.sendMessage(MiniMessage.miniMessage().deserialize("<color:#52a3ff>ⓘ</color> <color:#57ff65>The parkour <white>"+oldName+"</white> has been renamed to <white>"+itemName+"</white>!</color>"));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        MapListMenu.openMenu(player);

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        if (e.getView().getType() != InventoryType.ANVIL) return;

        String title = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        if (!title.contains("Rename Parkour")) return;

        Inventory anvilInventory = e.getInventory();

        ItemStack result = anvilInventory.getItem(2);
        if (result != null) {
            anvilInventory.setItem(2, null);
        }

        anvilInventory.setItem(0, null);
        anvilInventory.setItem(1, null);

        player.getInventory().remove(result);
    }


}