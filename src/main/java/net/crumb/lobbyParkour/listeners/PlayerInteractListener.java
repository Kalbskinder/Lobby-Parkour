package net.crumb.lobbyParkour.listeners;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.guis.CheckpointEditMenu;
import net.crumb.lobbyParkour.guis.EditPlateTypeMenu;
import net.crumb.lobbyParkour.guis.MapManageMenu;
import net.crumb.lobbyParkour.systems.ParkourSession;
import net.crumb.lobbyParkour.systems.ParkourSessionManager;
import net.crumb.lobbyParkour.systems.ParkourTimer;
import net.crumb.lobbyParkour.utils.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

                // Check for checkpoints
                boolean isPkCheckpoint = false;

                if (!isPkStart && !isPkEnd) {
                    List<Object[]> allPkCheckpoints = query.getCheckpoints();
                    final Integer[] parkourId = {null};
                    allPkCheckpoints.forEach(checkpoint -> {
                        if (compareLocations(LocationHelper.stringToLocation((String) checkpoint[2]), location)) {
                            parkourId[0] = (Integer) checkpoint[1];
                        }
                    });

                    if (parkourId == null) {
                        MMUtils.sendMessage(player, "Could not find parkour id of the checkpoint.", MessageType.ERROR);
                        return;
                    }

                    List<Object[]> pkCheckpoints = query.getCheckpoints(parkourId[0]);
                    if (pkCheckpoints.isEmpty()) {
                        MMUtils.sendMessage(player, "No checkpoints found for parkour with id " + parkourId + ".", MessageType.ERROR);
                        return;
                    }

                    isPkCheckpoint = query.isCheckpoint(parkourId[0]);
                }

                // Execute actions
                if (isPkStart) {
                    // Open the manage menu of the parkour
                    parkourName = query.getMapnameByPkSpawn(location);
                    if (parkourName.isEmpty()) return;
                    MapManageMenu.openMenu(player, parkourName);
                } else if (isPkEnd) {
                    // Open the edit menu for the end plate
                    parkourName = query.getMapnameByPkEnd(location);
                    if (parkourName == null) return;
                    EditPlateTypeMenu.openMenu(player, parkourName, PlateType.END);
                    return;
                } else if (isPkCheckpoint) {
                    // Open the checkpoint manage menu
                    CheckpointEditMenu.openMenu(player, parkourName, location);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
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
                        parkourName = query.getMapnameByPkSpawn(location);
                    } else if (isPkEnd) {
                        parkourName = query.getMapnameByPkEnd(location);
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
                        // Reset session
                        ParkourSessionManager.endSession(player.getUniqueId());
                    }

                    if (!ParkourSessionManager.isInSession(player.getUniqueId())) {
                        ParkourSessionManager.startSession(player.getUniqueId(), parkourName);
                        ParkourSessionManager.getSession(player.getUniqueId()).resetTime();
                        List<String> emptyLore = new ArrayList<>();

                        // Item action ids for right-click actions
                        String resetPkActionId = player.getUniqueId() + "reset-pk";
                        String leavePkActionId = player.getUniqueId() + "leave-pk";
                        String lastCheckpointActionId = player.getUniqueId() + "last-checkpoint-pk";

                        String timer = ParkourTimer.formatTimer(ParkourSessionManager.getSession(player.getUniqueId()).getElapsedSeconds(), ConfigManager.getFormat().getTimer());

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
                    // Player finished the parkour
                    if (ParkourSessionManager.isInSession(player.getUniqueId())) {
                        ParkourSession session = ParkourSessionManager.getSession(player.getUniqueId());
                        if (!session.getParkourName().equals(parkourName)) return;
                        float timerMillis = ParkourSessionManager.getSession(player.getUniqueId()).getElapsedSeconds();
                        String timer = ParkourTimer.formatTimer(timerMillis, ConfigManager.getFormat().getTimer());
                        ParkourSessionManager.endSession(player.getUniqueId()); // End session
                        player.getInventory().clear();

                        try {
                            ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                            Query query = new Query(database.getConnection());
                            int id = query.getParkourIdFromName(parkourName);
                            query.saveTime(player.getUniqueId(), id, timerMillis);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

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

    public static boolean compareLocations(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        return loc1.getWorld().getName().equals(loc2.getWorld().getName()) &&
                loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }

}
