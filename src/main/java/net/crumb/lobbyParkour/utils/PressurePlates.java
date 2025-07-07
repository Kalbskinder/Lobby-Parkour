package net.crumb.lobbyParkour.utils;

import org.bukkit.Material;

import java.util.List;

public class PressurePlates {
    public static List<String> get() {

        return List.of(
                "minecraft:oak_pressure_plate",
                "minecraft:spruce_pressure_plate",
                "minecraft:birch_pressure_plate",
                "minecraft:jungle_pressure_plate",
                "minecraft:acacia_pressure_plate",
                "minecraft:cherry_pressure_plate",
                "minecraft:dark_oak_pressure_plate",
                "minecraft:mangrove_pressure_plate",
                "minecraft:bamboo_pressure_plate",
                "minecraft:crimson_pressure_plate",
                "minecraft:warped_pressure_plate",
                "minecraft:stone_pressure_plate",
                "minecraft:polished_blackstone_pressure_plate",
                "minecraft:light_weighted_pressure_plate",
                "minecraft:heavy_weighted_pressure_plate"
        );
    }

    public static String formatPlateName(String plateId) {
        String name = plateId.replace("minecraft:", "");
        String[] parts = name.split("_");

        StringBuilder formatted = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            formatted.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }
        return formatted.toString().trim();
    }

    public static boolean isPressurePlate(Material material) {
        return material != null && material.name().endsWith("_PRESSURE_PLATE");
    }
}
