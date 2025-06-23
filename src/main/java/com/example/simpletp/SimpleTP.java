package com.example.simpletp;

import com.example.simpletp.commands.*;
import com.example.simpletp.data.DataManager;
import com.example.simpletp.listeners.PlayerListener;
import com.example.simpletp.utils.ConfigManager;
import com.example.simpletp.utils.MultiverseHelper;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleTP extends JavaPlugin {
    
    private static SimpleTP instance;
    private DataManager dataManager;
    private ConfigManager configManager;
    private MultiverseHelper multiverseHelper;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        multiverseHelper = new MultiverseHelper(this);
        
        // Load configuration
        configManager.loadConfig();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("SimpleTP has been enabled!");
        
        // Check for Multiverse-Core
        if (multiverseHelper.isMultiverseEnabled()) {
            getLogger().info("Multiverse-Core detected! Cross-world teleportation is supported.");
        } else {
            getLogger().warning("Multiverse-Core not found. Cross-world teleportation may have limitations.");
        }
    }
    
    @Override
    public void onDisable() {
        // Save any pending data
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        getLogger().info("SimpleTP has been disabled!");
    }
    
    private void registerCommands() {
        // Register all commands with their executors
        getCommand("bring").setExecutor(new BringCommand(this));
        getCommand("bringoff").setExecutor(new BringOffCommand(this));
        getCommand("bringall").setExecutor(new BringAllCommand(this));
        getCommand("bringalloff").setExecutor(new BringAllOffCommand(this));
        getCommand("goto").setExecutor(new GotoCommand(this));
        getCommand("gotooff").setExecutor(new GotoOffCommand(this));
        getCommand("tpoff").setExecutor(new TpOffCommand(this));
        getCommand("simpletp").setExecutor(new SimpleTpCommand(this));
        
        // Set tab completers
        PlayerTabCompleter tabCompleter = new PlayerTabCompleter();
        getCommand("bring").setTabCompleter(tabCompleter);
        getCommand("bringoff").setTabCompleter(tabCompleter);
        getCommand("goto").setTabCompleter(tabCompleter);
        getCommand("gotooff").setTabCompleter(tabCompleter);
        getCommand("tpoff").setTabCompleter(new TpOffTabCompleter());
    }
    
    public static SimpleTP getInstance() {
        return instance;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MultiverseHelper getMultiverseHelper() {
        return multiverseHelper;
    }
    
    public void reload() {
        configManager.loadConfig();
        dataManager.loadData();
        getLogger().info("SimpleTP configuration reloaded!");
    }
} 