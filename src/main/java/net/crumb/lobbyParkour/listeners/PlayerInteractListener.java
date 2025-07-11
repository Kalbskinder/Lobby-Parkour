package net.crumb.lobbyParkour.listeners;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.guis.MapManageMenu;
import net.crumb.lobbyParkour.systems.ParkourSession;
import net.crumb.lobbyParkour.systems.ParkourSessionManager;
import net.crumb.lobbyParkour.systems.ParkourTimer;
import net.crumb.lobbyParkour.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.crumb.lobbyParkour.utils.PressurePlates.isPressurePlate;

public class PlayerInteractListener implements Listener {
    private static final LobbyParkour plugin = LobbyParkour.getInstance();
    private static final TextFormatter textFormatter = new TextFormatter();
    private static String parkourName = "";


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK && player.hasPermission("lpk.admin") && PressurePlates.isPressurePlate(event.getClickedBlock().getType())) {
            Block block = event.getClickedBlock();
            if (block == null) return;
            Location location = block.getLocation();
            if (location == null) return;

            try {
                ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                Query query = new Query(database.getConnection());

                List<Object[]> pkStarts = query.getAllParkourStarts();
                boolean isPkStart = pkStarts.stream().anyMatch(entry -> (entry[1]).equals(location));

                List<Object[]> pkEnds = query.getAllParkourEnds();
                boolean isPkEnd = pkEnds.stream().anyMatch(entry -> (entry[1]).equals(location));

                if (isPkStart) {
                    parkourName = query.getMapNameByPkSpawn(location);
                } else if (isPkEnd) {
                    parkourName = query.getMapNameByPkEnd(location);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if (parkourName.isEmpty()) return;
            MapManageMenu.openMenu(player, parkourName);
        }

        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && player.isSneaking()) {
            RayTraceResult result = player.rayTraceEntities(5);
            if (result == null) return;

            Entity hitEntity = result.getHitEntity();
            if (hitEntity instanceof TextDisplay textDisplay) {
                player.sendMessage("You clicked on a TextDisplay!");
                event.setCancelled(true); // Optional: prevent other interaction behavior
            }
        }

        if (event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            if (block != null && isPressurePlate(block.getType())) {
                Location location = block.getLocation();
                boolean isPkStart = false;
                boolean isPkEnd = false;

                try {
                    ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                    Query query = new Query(database.getConnection());

                    List<Object[]> pkStarts = query.getAllParkourStarts();
                    isPkStart = pkStarts.stream().anyMatch(entry -> (entry[1]).equals(location));

                    List<Object[]> pkEnds = query.getAllParkourEnds();
                    isPkEnd = pkEnds.stream().anyMatch(entry -> (entry[1]).equals(location));

                    if (isPkStart) {
                        parkourName = query.getMapNameByPkSpawn(location);
                    } else if (isPkEnd) {
                        parkourName = query.getMapNameByPkEnd(location);
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                if (parkourName.isEmpty()) return;


                // Check if the player is starting a parkour
                if (isPkStart) {
                    ParkourTimer.start();
                    // If player is already doing parkour, reset the timer to 0s
                    if (ParkourSessionManager.isInSession(player.getUniqueId())) {
                        ParkourSessionManager.setTime(player.getUniqueId(), 0f);
                    } else {
                        ParkourSessionManager.startSession(player.getUniqueId(), parkourName);
                        List<String> emptyLore = new ArrayList<>();

                        // Item action ids for right-click actions
                        String resetPkActionId = player.getUniqueId() + "reset-pk";
                        String leavePkActionId = player.getUniqueId() + "leave-pk";
                        String lastCheckpointActionId = player.getUniqueId() + "last-checkpoint-pk";

                        String timer = ParkourTimer.formatTimer(ParkourSessionManager.getSession(player.getUniqueId()).getTime(), ConfigManager.getFormat().getTimer());

                        ItemActionHandler.registerAction(resetPkActionId, p -> {
                            p.teleport(location);
                            Component resetMessage = textFormatter.formatString(ConfigManager.getFormat().getResetMessage(), player, Map.of(
                                    "parkour_name", parkourName,
                                    "player_name", player.getName(),
                                    "timer", timer
                            ));
                            p.sendMessage(resetMessage);
                        });

                        ItemActionHandler.registerAction(leavePkActionId, p -> {
                            p.getInventory().clear();
                            if (ParkourSessionManager.isInSession(player.getUniqueId())) {
                                ParkourSessionManager.endSession(player.getUniqueId());

                                // Send leave/cancel message
                                Component leaveMessage = textFormatter.formatString(ConfigManager.getFormat().getCancelMessage(), player, Map.of(
                                        "parkour_name", parkourName,
                                        "player_name", player.getName(),
                                        "timer", timer
                                ));
                                p.sendMessage(leaveMessage);
                            }
                        });

                        ItemActionHandler.registerAction(lastCheckpointActionId, p -> {
                            p.sendMessage("Teleported to the last checkpoint!");
                        });

                        // Create parkour items
                        ItemStack restItem = ActionItemMaker.createItem("minecraft:oak_door", 1, "<red>Reset", emptyLore, resetPkActionId);
                        ItemStack leaveItem = ActionItemMaker.createItem("minecraft:red_bed", 1, "<red>Leave", emptyLore, leavePkActionId);
                        ItemStack lastCpItem = ActionItemMaker.createItem("minecraft:heavy_weighted_pressure_plate", 1, "<green>Last Checkpoint", emptyLore, lastCheckpointActionId);

                        // Apply inventory layout
                        player.getInventory().clear();
                        ItemMaker.giveItemToPlayer(player, restItem, 4);
                        ItemMaker.giveItemToPlayer(player, leaveItem, 5);
                        ItemMaker.giveItemToPlayer(player, lastCpItem, 3);

                        // Send start message
                        Component startMessage = textFormatter.formatString(ConfigManager.getFormat().getStartMessage(), player, Map.of(
                                "parkour_name", parkourName,
                                "player_name", player.getName()
                        ));
                        player.sendMessage(startMessage);
                    }
                } else {
                    String timer = ParkourTimer.formatTimer(ParkourSessionManager.getSession(player.getUniqueId()).getTime(), ConfigManager.getFormat().getTimer());

                    // Player finished the parkour
                    if (ParkourSessionManager.isInSession(player.getUniqueId())) {
                        ParkourSessionManager.endSession(player.getUniqueId()); // End session
                        player.getInventory().clear();

                        // Send end message
                        Component endMessage = textFormatter.formatString(ConfigManager.getFormat().getEndMessage(), player, Map.of(
                                "parkour_name", parkourName,
                                "player_name", player.getName(),
                                "timer", timer
                        ));

                        player.sendMessage(endMessage);
                    }
                }
            }
        }
    }
}
