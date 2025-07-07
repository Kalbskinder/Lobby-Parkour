package net.crumb.lobbyParkour.listeners;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.guis.MainMenu;
import net.crumb.lobbyParkour.guis.MapListMenu;
import net.crumb.lobbyParkour.guis.MapManageMenu;
import net.crumb.lobbyParkour.guis.ParkourStartPlateGUI;
import net.crumb.lobbyParkour.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.*;

public class InventoryClickListener implements Listener {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LobbyParkour plugin = LobbyParkour.getInstance();
    private static final TextFormatter textFormatter = new TextFormatter();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        if (!player.hasPermission("lpk.admin")) return;

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

        if (menuTitle.equals("ʟᴏʙʙʏ ᴘᴀʀᴋᴏᴜʀ")) {
            event.setCancelled(true);


            if (displayName.equals("+ Create a new parkour")) {
                player.getInventory().clear(0);
                player.getOpenInventory().close();
                MMUtils.sendMessage(player, "Please place the start of your parkour. <gray>(1/2)</gray>", MessageType.INFO);
                ItemMaker.giveItemToPlayer(player, ItemMaker.createItem("minecraft:light_weighted_pressure_plate", 1, "<green>Parkour Start", Arrays.asList("<gray>Place this where you want", "<gray>your parkour to start.")), 0);
            }   player.getInventory().setHeldItemSlot(0);

            if (displayName.equals("⚑ Parkour List")) {
                MapListMenu.openMenu(player);
            }

            if (displayName.equals("🔁 Reload Parkours")) {
                player.getOpenInventory().close();
                MMUtils.sendMessage(player, "Reloading all parkours...", MessageType.INFO);


                try {
                    ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                    Query query = new Query(database.getConnection());

                    List<Object[]> starts = query.getAllParkourStarts();

                    for (Object[] data : starts) {
                        String name = (String) data[0];
                        Location loc = (Location) data[1];
                        Material mat = (Material) data[2];

                        loc.getBlock().setType(mat);

                        UUID oldUuid = query.getStartEntityUuid(name);
                        if (oldUuid != null) {
                            Entity oldEntity = loc.getWorld().getEntity(oldUuid);
                            if (oldEntity instanceof TextDisplay) {
                                EntityRemove.suppress(oldUuid);
                                oldEntity.remove();
                            }
                        }


                        Map<String, String> placeholders = Map.of(
                                "parkour_name", name
                        );
                        Component startText = textFormatter.formatString(ConfigManager.getFormat().getStartPlate(), placeholders);

                        Location textDisplayLocation = new Location(loc.getWorld(), loc.getX() + 0.5, loc.getY() + 1.0, loc.getZ() + 0.5);
                        TextDisplay display = loc.getWorld().spawn(textDisplayLocation, TextDisplay.class, textDisplay -> {
                            textDisplay.text(startText);
                            textDisplay.setBillboard(Display.Billboard.CENTER);
                        });

                        query.updateStartEntityUuid(name, display.getUniqueId());
                    }

                    List<Object[]> ends = query.getAllParkourEnds();

                    for (Object[] data : ends) {
                        String name = (String) data[0];
                        Location loc = (Location) data[1];
                        Material mat = (Material) data[2];

                        loc.getBlock().setType(mat);

                        UUID oldUuid = query.getEndEntityUuid(name);
                        if (oldUuid != null) {
                            Entity oldEntity = loc.getWorld().getEntity(oldUuid);
                            if (oldEntity instanceof TextDisplay) {
                                EntityRemove.suppress(oldUuid);
                                oldEntity.remove();
                            }
                        }

                        Map<String, String> placeholders = Map.of(
                                "parkour_name", name
                        );
                        Component endText = textFormatter.formatString(ConfigManager.getFormat().getEndPlate(), placeholders);

                        Location textDisplayLocation = new Location(loc.getWorld(), loc.getX() + 0.5, loc.getY() + 1.0, loc.getZ() + 0.5);
                        TextDisplay display = loc.getWorld().spawn(textDisplayLocation, TextDisplay.class, textDisplay -> {
                            textDisplay.text(endText);
                            textDisplay.setBillboard(Display.Billboard.CENTER);
                        });

                        query.updateEndEntityUuid(name, display.getUniqueId());
                    }

                    MMUtils.sendMessage(player, "Parkours reloaded successfully!", MessageType.INFO);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    MMUtils.sendMessage(player, "There was an error while reloading the parkours!", MessageType.ERROR);
                }
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

                    Location startLocation = query.getStartLocation(name);
                    startLocation.getBlock().setType(Material.AIR);

                    UUID startEntityUuid = query.getStartEntityUuid(name);
                    World startLocationWorld = startLocation.getWorld();
                    EntityRemove.suppress(startEntityUuid);
                    Entity startEntity = startLocationWorld.getEntity(startEntityUuid);
                    assert startEntity != null;
                    startEntity.remove();

                    Location endLocation = query.getEndLocation(name);
                    endLocation.getBlock().setType(Material.AIR);

                    UUID endEntityUuid = query.getEndEntityUuid(name);
                    World endLocationWorld = endLocation.getWorld();
                    EntityRemove.suppress(endEntityUuid);
                    Entity endEntity = endLocationWorld.getEntity(endEntityUuid);
                    assert endEntity != null;
                    endEntity.remove();

                    query.deleteParkour(name);

                    MMUtils.sendMessage(player, "The parkour <white>"+name+"</white> has been deleted!", MessageType.INFO);

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
                MMUtils.sendMessage(player, "You have been teleported to the start of <white>"+name+"</white>!", MessageType.INFO);
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
                        plugin.getLogger().warning("Unknown material: \" + plate");
                    }
                }
            });

        }
    }
}
