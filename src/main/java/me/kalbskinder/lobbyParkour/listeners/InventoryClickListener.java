package me.kalbskinder.lobbyParkour.listeners;

import me.kalbskinder.lobbyParkour.LobbyParkour;
import me.kalbskinder.lobbyParkour.database.ParkoursDatabase;
import me.kalbskinder.lobbyParkour.database.Query;
import me.kalbskinder.lobbyParkour.guis.MainMenu;
import me.kalbskinder.lobbyParkour.guis.MapListMenu;
import me.kalbskinder.lobbyParkour.guis.MapManageMenu;
import me.kalbskinder.lobbyParkour.guis.ParkourStartPlateGUI;
import me.kalbskinder.lobbyParkour.utils.ItemMaker;
import me.kalbskinder.lobbyParkour.utils.PressurePlates;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryClickListener implements Listener {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LobbyParkour plugin = LobbyParkour.getInstance();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null) return;
        if (clickedInventory.getType().equals(InventoryType.ANVIL)) return;


        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String displayName = "";
        List<String> itemLore = new ArrayList<>();
        ItemMeta meta = clickedItem.getItemMeta();

        if (meta.hasDisplayName()) {
            displayName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        }

        if (meta != null && meta.lore() != null) {
            meta.lore().forEach(component -> itemLore.add(PlainTextComponentSerializer.plainText().serialize(component)));
        }

        String menuTitle = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        menuTitle = menuTitle.trim();

        if (menuTitle.equals("Lobby Parkour")) {
            event.setCancelled(true);


            if (displayName.equals("+ Create a new parkour")) {
                player.getInventory().clear();
                player.getOpenInventory().close();
                ItemMaker.giveItemToPlayer(player, ItemMaker.createItem("minecraft:light_weighted_pressure_plate", 1, "<green>Parkour Start", Arrays.asList("<gray>Place this where you want", "<gray>your parkour to start.")), 0);
            }

            if (displayName.equals("⚑ Parkour List")) {
                MapListMenu.openMenu(player);
            }
        }

        if (menuTitle.equals("Parkour List")) {
            event.setCancelled(true);

            if (displayName.equals("Back")) {
                MainMenu.openMenu(player);
            }

            if (displayName.equals("Close")) {
                clickedInventory.close();
            }

            if (clickedItem.getType().toString().equals("GRASS_BLOCK")) {
                MapManageMenu.openMenu(player, displayName);
            }
        }

        if (menuTitle.equals("Manage Parkour")) {
            event.setCancelled(true);

            if (displayName.equals("Back")) {
                MapListMenu.openMenu(player);
            }

            if (displayName.equals("Close")) {
                clickedInventory.close();
            }

            if (displayName.equals("Rename Parkour")) {
                MapManageMenu.openRenameAnvil(player, itemLore.get(1));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.1f, 2.0f);
            }

            if (displayName.equals("Delete Parkour")) {
                try {
                    ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                    Query query = new Query(database.getConnection());
                    Component loreLine = event.getView().getItem(10).getItemMeta().lore().get(1);
                    String name = PlainTextComponentSerializer.plainText().serialize(loreLine);

                    Location loc = query.getStartLocation(name);
                    loc.getBlock().setType(Material.AIR);
                    query.deleteParkour(name);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                MapListMenu.openMenu(player);
            }

            if (displayName.equals("Change Type")) {
                Component loreLine = event.getView().getItem(10).getItemMeta().lore().get(1);
                String name = PlainTextComponentSerializer.plainText().serialize(loreLine);
                ParkourStartPlateGUI.openMenu(player, name);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.1f, 2.0f);
            }

            if (displayName.equals("Teleport to plate")) {
                Component loreLine = event.getView().getItem(10).getItemMeta().lore().get(1);
                String name = PlainTextComponentSerializer.plainText().serialize(loreLine);

                Location loc = null;

                try {
                    ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                    Query query = new Query(database.getConnection());

                    loc = query.getStartLocation(name);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                if (loc == null) return;

                player.teleport(loc);
            }
        }

        if (menuTitle.equals("Change Type")) {
            event.setCancelled(true);

            if (displayName.equals("Back")) {
                String currentParkour = PlainTextComponentSerializer.plainText().serialize(clickedInventory.getItem(0).lore().get(0));
                MapManageMenu.openMenu(player, currentParkour);
            }

            if (displayName.equals("Close")) {
                clickedInventory.close();
            }

            final String itemName = displayName;

            PressurePlates.get().forEach(plate -> {
                if (itemName.equals(PressurePlates.formatPlateName(plate))) {
                    Component loreLine = event.getView().getItem(0).getItemMeta().lore().get(0);
                    Location loc = null;

                    // Get location of plate
                    try {
                        ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                        Query query = new Query(database.getConnection());
                        loc = query.getStartLocation(PlainTextComponentSerializer.plainText().serialize(loreLine));
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    if (loc == null) return;
                    Material material = Material.matchMaterial(plate.replace("minecraft:", ""));

                    if (material != null) {
                        loc.getBlock().setType(material);

                        try {
                            ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                            Query query = new Query(database.getConnection());
                            String currentParkour = PlainTextComponentSerializer.plainText().serialize(clickedInventory.getItem(0).lore().get(0));

                            query.updateStartType(currentParkour, plate);

                            ParkourStartPlateGUI.openMenu(player, currentParkour);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                    } else {
                        System.out.println("Unknown material: " + plate);
                    }
                }
            });

        }
    }
}
