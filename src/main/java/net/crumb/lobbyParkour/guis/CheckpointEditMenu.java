package net.crumb.lobbyParkour.guis;

import net.crumb.lobbyParkour.utils.ItemMaker;
import net.crumb.lobbyParkour.utils.LocationHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CheckpointEditMenu {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    public static void openMenu(Player player, String parkourName, Location location) {
        if (!player.hasPermission("lpk.admin")) return;
        Inventory gui = Bukkit.createInventory(null, 9 * 3, miniMessage.deserialize("<bold><gradient:#2200cc:#5e43e6>Manage Checkpoint<reset>"));
        List<String> emptyLore = new ArrayList<>();

        ItemStack background = ItemMaker.createItem("minecraft:blue_stained_glass_pane", 1, "", emptyLore);
        ItemStack backArrow = ItemMaker.createItem("minecraft:arrow", 1, "<green>Back", List.of("<gray>Previous page"));
        ItemStack closeButton = ItemMaker.createItem("minecraft:barrier", 1, "<red>Close", emptyLore);
        ItemStack deleteButton = ItemMaker.createItem("minecraft:tnt", 1, "<red>Delete Checkpoint", List.of("<yellow><bold>WARNING! <reset><!italic><yellow>Action can not be undone!", "<yellow>Click to delete!"));
        ItemStack changeCheckpointType = ItemMaker.createItem("minecraft:light_weighted_pressure_plate", 1, "<green>Change Type", List.of("<yellow>Click to change"));
        ItemStack relocateCheckpoint = ItemMaker.createItem("minecraft:compass", 1, "<green>Relocate Checkpoint", List.of("<yellow>Click to relocate"));
        ItemStack secretItem = ItemMaker.createItem("minecraft:blue_stained_glass_pane", 1, "", emptyLore);

        ItemMeta meta = secretItem.getItemMeta();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(parkourName));
        lore.add(Component.text(LocationHelper.locationToString(location)));
        meta.lore(lore);
        secretItem.setItemMeta(meta);


        int size = gui.getSize();

        for (int slot = 0; slot < size; slot++) {
            int row = slot / 9;
            int col = slot % 9;

            boolean isTopOrBottom = row == 0 || row == 2;
            boolean isLeftOrRight = col == 0 || col == 8;

            if (isTopOrBottom || isLeftOrRight) {
                gui.setItem(slot, background);
            }
        }

        gui.setItem(0, secretItem);
        gui.setItem(10, changeCheckpointType);
        gui.setItem(11, relocateCheckpoint);

        gui.setItem(21, backArrow);
        gui.setItem(22, closeButton);
        gui.setItem(26, deleteButton);


        player.openInventory(gui);
    }
}
