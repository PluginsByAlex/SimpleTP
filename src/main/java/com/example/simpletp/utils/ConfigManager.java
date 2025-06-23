package com.example.simpletp.utils;

import com.example.simpletp.SimpleTP;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final SimpleTP plugin;
    private FileConfiguration config;
    
    // Config options
    private boolean offlineTpEnabled;
    private boolean bringAllEnabled;
    private boolean consoleUseEnabled;
    private boolean debugMode;
    
    public ConfigManager(SimpleTP plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();
        
        // Reload config from disk
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Load configuration values
        offlineTpEnabled = config.getBoolean("enable-offline-tp", true);
        bringAllEnabled = config.getBoolean("enable-bringall", true);
        consoleUseEnabled = config.getBoolean("enable-console-commands", true);
        debugMode = config.getBoolean("debug-mode", false);
        
        if (debugMode) {
            plugin.getLogger().info("Configuration loaded:");
            plugin.getLogger().info("  Offline TP: " + offlineTpEnabled);
            plugin.getLogger().info("  Bring All: " + bringAllEnabled);
            plugin.getLogger().info("  Console Commands: " + consoleUseEnabled);
            plugin.getLogger().info("  Debug Mode: " + debugMode);
        }
    }
    
    public boolean isOfflineTpEnabled() {
        return offlineTpEnabled;
    }
    
    public boolean isBringAllEnabled() {
        return bringAllEnabled;
    }
    
    public boolean isConsoleUseEnabled() {
        return consoleUseEnabled;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
} 