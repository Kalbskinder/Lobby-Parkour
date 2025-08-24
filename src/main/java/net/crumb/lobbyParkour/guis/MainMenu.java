package net.crumb.lobbyParkour.guis;

import net.crumb.lobbyParkour.utils.ItemMaker;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainMenu {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();


    public static void openMenu(Player player) {
        if (!player.hasPermission("lpk.admin")) return;
        Inventory gui = Bukkit.createInventory(null, 9 * 3, miniMessage.deserialize("         <bold><gradient:#d81bf5:#fa2dc3>ʟᴏʙʙʏ ᴘᴀʀᴋᴏᴜʀ</gradient>"));
        List<String> emptyLore = new ArrayList<>();

        ItemStack backgroundDark = ItemMaker.createItem("minecraft:purple_stained_glass_pane", 1, "", emptyLore);
        ItemStack backgroundNormal = ItemMaker.createItem("minecraft:magenta_stained_glass_pane", 1, "", emptyLore);
        ItemStack backgroundLight = ItemMaker.createItem("minecraft:pink_stained_glass_pane", 1, "", emptyLore);

        ItemStack parkourList = ItemMaker.createItem("minecraft:paper", 1, "<green>⚑ <green>Parkour List", Arrays.asList("<gray>Manage your parkour courses", "<yellow>Click to view!"));
        ItemStack newParkour = ItemMaker.createItem("minecraft:light_weighted_pressure_plate", 1, "<green><bold>+ <reset><!italic><green>Create a new parkour", Arrays.asList("<gray>Setup a new parkour course", "<yellow>Click to setup!"));
        ItemStack parkourLeaderboard = ItemMaker.createItem("minecraft:name_tag", 1, "<green>✯ Parkour Leaderboards", Arrays.asList("<gray>Setup a parkour leaderboard", "<yellow>Click to manage!"));
        ItemStack reloadParkours = ItemMaker.createItem("minecraft:clock", 1, "<green>🔁 Reload Parkours", Arrays.asList("<gray>Reload all parkours on the server", "<yellow>Click to reload!"));

        gui.setItem(0, backgroundDark);
        gui.setItem(1, backgroundDark);
        gui.setItem(2, backgroundDark);

        gui.setItem(3, backgroundNormal);
        gui.setItem(4, backgroundNormal);
        gui.setItem(5, backgroundNormal);
        gui.setItem(6, backgroundNormal);

        gui.setItem(7, backgroundLight);
        gui.setItem(8, backgroundLight);

        gui.setItem(9, backgroundDark);
        gui.setItem(17, backgroundLight);
        gui.setItem(18, backgroundDark);
        gui.setItem(19, backgroundDark);

        gui.setItem(20, backgroundNormal);
        gui.setItem(21, backgroundNormal);
        gui.setItem(22, backgroundNormal);
        gui.setItem(23, backgroundNormal);

        gui.setItem(24, backgroundLight);
        gui.setItem(25, backgroundLight);
        gui.setItem(26, backgroundLight);

        gui.setItem(10, newParkour);
        gui.setItem(11, parkourList);
        gui.setItem(12, parkourLeaderboard);
        gui.setItem(16, reloadParkours);

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.0f);
    }
}
