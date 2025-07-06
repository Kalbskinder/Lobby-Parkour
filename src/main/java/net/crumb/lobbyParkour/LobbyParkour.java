package net.crumb.lobbyParkour;

import net.crumb.lobbyParkour.commands.BaseCommand;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Logger;

public final class LobbyParkour extends JavaPlugin {
    private ParkoursDatabase parkoursDatabase;
    private static LobbyParkour instance;

    public static LobbyParkour getInstance() {
        return instance;
    }

    private void startUpMessage() {
        Logger logger = Logger.getLogger("Lobby-Parkour");
        logger.info("-------------------------------");
        logger.info("        LPK - Lobby Parkour       ");
        logger.info("          Version: 1.0.0");
        logger.info("           Author: crumb");
        logger.info("--------------------------------");
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new InventoryClickListener(), this);
        pm.registerEvents(new BlockPlaceListener(this), this);
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new RenameItemListener(), this);
        pm.registerEvents(new EntityRemove(), this);
    }

    @Override
    public void onEnable() {
        instance = this;

        getCommand("lpk").setExecutor(new BaseCommand(this));

        saveDefaultConfig();
        startUpMessage();
        registerListeners();

        try {
            parkoursDatabase = new ParkoursDatabase(getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to connect to the database! " + ex.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            parkoursDatabase.closeConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
