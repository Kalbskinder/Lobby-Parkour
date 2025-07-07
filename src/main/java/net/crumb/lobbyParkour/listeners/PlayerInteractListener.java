package net.crumb.lobbyParkour.listeners;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.guis.MapManageMenu;
import net.crumb.lobbyParkour.systems.ParkourSession;
import net.crumb.lobbyParkour.systems.ParkourSessionManager;
import net.crumb.lobbyParkour.systems.ParkourTimer;
import net.crumb.lobbyParkour.utils.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.crumb.lobbyParkour.utils.PressurePlates.isPressurePlate;

public class PlayerInteractListener implements Listener {
    private static final LobbyParkour plugin = LobbyParkour.getInstance();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK && player.hasPermission("lpk.admin") && PressurePlates.isPressurePlate(event.getClickedBlock().getType())) {
            Block block = event.getClickedBlock();
            if (block == null) return;
            Location location = block.getLocation();
            if (location == null) return;
            String parkourName = "";

            try {
                ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
                Query query = new Query(database.getConnection());

                List<Object[]> pkStarts = query.getAllParkourStarts();
                boolean isPkStart = pkStarts.stream().anyMatch(entry -> (entry[1]).equals(location));

                List<Object[]> pkEnds = query.getAllParkourEnds();
                boolean isPkEnd = pkEnds.stream().anyMatch(entry -> (entry[1]).equals(location));

                if (isPkStart) {
                    parkourName = query.getMapnameByPkSpawn(location);
                } else if (isPkEnd) {
                    parkourName = query.getMapnameByPkEnd(location);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if (parkourName.isEmpty()) return;
            MapManageMenu.openMenu(player, parkourName);
        }

        if (event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            if (block != null && isPressurePlate(block.getType())) {
                Location location = block.getLocation();
                String parkourName = "";
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

                if (isPkStart) {
                    ParkourTimer.start();
                    if (ParkourSessionManager.isInSession(player.getUniqueId())) {
                        ParkourSessionManager.setTime(player.getUniqueId(), 0f);
                    } else {
                        ParkourSessionManager.startSession(player.getUniqueId(), parkourName);
                        List<String> emptyLore = new ArrayList<>();
                        String resetPkActionId = player.getUniqueId() + "reset-pk";
                        String leavePkActionId = player.getUniqueId() + "leave-pk";
                        String lastCheckpointActionId = player.getUniqueId() + "last-checkpoint-pk";

                        ItemActionHandler.registerAction(resetPkActionId, p -> {
                            p.teleport(location);
                        });

                        ItemActionHandler.registerAction(leavePkActionId, p -> {
                            p.teleport(location);
                        });

                        ItemActionHandler.registerAction(lastCheckpointActionId, p -> {
                            p.sendMessage("Teleported to the last checkpoint!");
                        });

                        ItemStack restItem = ActionItemMaker.createItem("minecraft:oak_door", 1, "<red>Reset", emptyLore, resetPkActionId);
                        ItemStack leaveItem = ActionItemMaker.createItem("minecraft:red_bed", 1, "<red>Leave", emptyLore, leavePkActionId);
                        ItemStack lastCpItem = ActionItemMaker.createItem("minecraft:heavy_weighted_pressure_plate", 1, "<green>Last Checkpoint", emptyLore, lastCheckpointActionId);

                        ItemMaker.giveItemToPlayer(player, restItem, 4);
                        ItemMaker.giveItemToPlayer(player, leaveItem, 5);
                        ItemMaker.giveItemToPlayer(player, lastCpItem, 6);
                    }
                } else {
                    if (ParkourSessionManager.isInSession(player.getUniqueId())) {
                        player.sendMessage("Finished parkour in: " + ParkourSessionManager.getTime(player.getUniqueId()));
                        ParkourSessionManager.endSession(player.getUniqueId());
                    }
                }
            }
        }
    }
}
