package net.crumb.lobbyParkour.utils;

import net.crumb.lobbyParkour.LobbyParkour;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ActionItemMaker {
    private static final Logger logger = Logger.getLogger("Lobby-Parkour");
    private static final NamespacedKey ACTION_KEY = new NamespacedKey(LobbyParkour.getPlugin(LobbyParkour.class), "item_action");
    private static final MiniMessage mm = MiniMessage.miniMessage();

    // Creates an ItemStack with the specified properties and optional right-click action.
    public static ItemStack createItem(String item, int amount, String itemName, List<String> lore, String actionId) {

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
        }

        // Set lore
        if (lore != null && !lore.isEmpty()) {
            List<Component> loreComponents = lore.stream()
                    .filter(Objects::nonNull)
                    .map(line -> mm.deserialize("<!italic>" + line))
                    .collect(Collectors.toList());

            meta.lore(loreComponents);
        }

        // Set action identifier
        if (actionId != null && !actionId.trim().isEmpty()) {
            meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, actionId);
        }

        // Make item not lose durability
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // Apply meta
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    // Gets the action identifier from an ItemStack.
    public static String getActionId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(ACTION_KEY, PersistentDataType.STRING);
    }
}