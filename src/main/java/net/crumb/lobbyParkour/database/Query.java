package net.crumb.lobbyParkour.database;

import net.crumb.lobbyParkour.utils.LocationHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Query {
    private final Connection connection;

    public Query(Connection connection) {
        this.connection = connection;
    }

    public void createParkour(String name, Location startLocation, UUID entityUuid) throws SQLException {
        String sql = "INSERT INTO parkours (pk_name, start_cp, start_cp_material, start_cp_entity_uuid) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, LocationHelper.locationToString(startLocation));
            statement.setString(3, "minecraft:light_weighted_pressure_plate");
            statement.setString(4, entityUuid.toString());
            statement.executeUpdate();
        }
    }

    public Location getStartLocation(String parkourName) throws SQLException {
        String sql = "SELECT start_cp FROM parkours WHERE pk_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, parkourName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return LocationHelper.stringToLocation(rs.getString("start_cp"));
                }
            }
        }
        return null;
    }

    public ItemStack getStartType(String mapName) throws SQLException {
        String sql = "SELECT start_cp_material FROM parkours WHERE pk_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mapName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String materialName = rs.getString("start_cp_material");
                    if (materialName.startsWith("minecraft:")) {
                        materialName = materialName.substring(10);
                    }

                    Material material = Material.matchMaterial(materialName.toUpperCase());
                    if (material != null) {
                        return new ItemStack(material);
                    }
                }
            }
        }
        return null;
    }

    public UUID getStartEntityUuid(String mapName) throws SQLException {
        String sql = "SELECT start_cp_entity_uuid FROM parkours WHERE pk_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mapName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String entityUuid = rs.getString("start_cp_entity_uuid");
                    return UUID.fromString(entityUuid);
                }
            }
        }
        return null;
    }

    public String getMapNameByStartUuid(UUID entityUUID) throws SQLException {
        String sql = "SELECT pk_name FROM parkours WHERE start_cp_entity_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, entityUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("pk_name");
                }
            }
        }
        return null;
    }

    public void updateStartEntityUuid(String mapName, UUID newUuid) throws SQLException{
        String sql = "UPDATE parkours SET start_cp_entity_uuid = ? WHERE pk_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newUuid.toString());
            stmt.setString(2, mapName);
            stmt.executeUpdate();
        }
    }

    public void updateStartType(String mapName, String newType) throws SQLException {
        String sql = "UPDATE parkours SET start_cp_material = ? WHERE pk_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newType);
            stmt.setString(2, mapName);
            stmt.executeUpdate();
        }
    }

    public String getMapnameByPkSpawn(Location location) throws SQLException {
        String sql = "SELECT pk_name FROM parkours WHERE start_cp = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, LocationHelper.locationToString(location));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("pk_name");
                }
            }
        }
        return sql;
    }

    public boolean parkourExists(String name) throws SQLException {
        String sql = "SELECT 1 FROM parkours WHERE pk_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<String> parkourMaps() throws SQLException {
        List<String> parkourNames = new ArrayList<>();
        String sql = "SELECT pk_name FROM parkours";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                parkourNames.add(rs.getString("pk_name"));
            }
        }

        return parkourNames;
    }

    public void renameParkour(String oldName, String newName) throws SQLException {
        String sql = "UPDATE parkours SET pk_name = ? WHERE pk_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, oldName);
            stmt.executeUpdate();
        }
    }

    public void deleteParkour(String parkourName) throws SQLException {
        String sql = "DELETE FROM parkours WHERE pk_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, parkourName);
            stmt.executeUpdate();
        }
    }



}
