package net.crumb.lobbyParkour.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ItemMaker {
    private static final Logger logger = Logger.getLogger("PTZ");
    private static final MiniMessage mm = MiniMessage.miniMessage();

    // Creates an ItemStack with the specified properties and optional right-click action.
    public static ItemStack createItem(String item, int amount, String itemName, List<String> lore) {

        // Validate input
        if (item == null || item.trim().isEmpty()) {
            logger.warning("Invalid item identifier: " + item);
            return null;
        }

        if (amount < 1) {
            logger.warning("Invalid amount: " + amount + ". Setting to 1.");
            amount = 1;
        }

        // Parse material
        String materialName = item.toUpperCase().replace("MINECRAFT:", "");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            logger.warning("Invalid material: " + item);
            return null;
        }

        // Create ItemStack
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            logger.warning("Failed to get ItemMeta for material: " + materialName);
            return itemStack;
        }

        // Set display name
        if (itemName != null && !itemName.trim().isEmpty()) {
            meta.displayName(mm.deserialize("<!italic>" + itemName));
        } else {
            meta.setHideTooltip(true);
        }

        // Set lore
        if (lore != null && !lore.isEmpty()) {
            List<Component> loreComponents = lore.stream()
                    .filter(Objects::nonNull)
                    .map(line -> mm.deserialize("<!italic>" + line))
                    .collect(Collectors.toList());

            meta.lore(loreComponents);
        }

        // Make item not lose durability
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // Apply meta
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    // Gives the item to a player in the specified inventory slot.
    public static void giveItemToPlayer(Player player, ItemStack item, int slot) {
        if (player == null || !player.isOnline()) {
            logger.warning("Cannot give item to null or offline player");
            return;
        }
        if (item == null) {
            logger.warning("Cannot give null item to player: " + player.getName());
            return;
        }

        // Available inventory slots of a players GUI
        if (slot < 0 || slot > 40) {
            logger.warning("Invalid inventory slot: " + slot + " for player: " + player.getName());
            return;
        }

        // Handle main inventory (0-35), armor (36-39), or off-hand (40)
        if (slot <= 35) {
            player.getInventory().setItem(slot, item);
        } else if (slot == 40) {
            player.getInventory().setItemInOffHand(item);
        } else {
            // Armor slots (36: helmet, 37: chestplate, 38: leggings, 39: boots)
            switch (slot) {
                case 39:
                    player.getInventory().setHelmet(item);
                    break;
                case 38:
                    player.getInventory().setChestplate(item);
                    break;
                case 37:
                    player.getInventory().setLeggings(item);
                    break;
                case 36:
                    player.getInventory().setBoots(item);
                    break;
                default:
                    return;
            }
        }

        player.updateInventory(); // Update the inventory changes
    }
}