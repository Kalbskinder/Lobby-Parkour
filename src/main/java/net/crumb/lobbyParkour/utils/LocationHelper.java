package net.crumb.lobbyParkour.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationHelper {
    public static String locationToString(Location loc) {
        return loc.getWorld().getName() + "," +
               loc.getX() + "," +
               loc.getY() + "," +
               loc.getZ() + "," +
               loc.getYaw() + "," +
               loc.getPitch();
    }

    public static Location stringToLocation(String str) {
        String[] parts = str.split(",");
        World world = Bukkit.getWorld(parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);
        return new Location(world, x, y, z, yaw, pitch);
    }

}
