package net.crumb.lobbyParkour.listeners;

import net.crumb.lobbyParkour.systems.ParkourSessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (ParkourSessionManager.isInSession(player.getUniqueId())) {
            event.setCancelled(true); // Cancel the event
        }

    }

    // Check if a player is trying to remove an item while being in the parkour
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!ParkourSessionManager.isInSession(player.getUniqueId())) return; // Check if player is doing parkour

        event.setCancelled(true);
    }
}
