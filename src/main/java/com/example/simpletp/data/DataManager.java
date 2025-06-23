package com.example.simpletp.data;

import com.example.simpletp.SimpleTP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    
    private final SimpleTP plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    // In-memory storage for quick access
    private Map<UUID, Location> pendingTeleports; // Players who need to be teleported on login
    private Map<UUID, Location> lastKnownLocations; // Last known logout locations
    
    public DataManager(SimpleTP plugin) {
        this.plugin = plugin;
        this.pendingTeleports = new HashMap<>();
        this.lastKnownLocations = new HashMap<>();
        
        setupDataFile();
        loadData();
    }
    
    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("Created new data.yml file");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml file: " + e.getMessage());
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    public void loadData() {
        pendingTeleports.clear();
        lastKnownLocations.clear();
        
        // Load pending teleports
        if (dataConfig.contains("pending-teleports")) {
            for (String uuidString : dataConfig.getConfigurationSection("pending-teleports").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    Location location = deserializeLocation(dataConfig.getString("pending-teleports." + uuidString));
                    if (location != null) {
                        pendingTeleports.put(uuid, location);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load pending teleport for " + uuidString + ": " + e.getMessage());
                }
            }
        }
        
        // Load last known locations
        if (dataConfig.contains("last-locations")) {
            for (String uuidString : dataConfig.getConfigurationSection("last-locations").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    Location location = deserializeLocation(dataConfig.getString("last-locations." + uuidString));
                    if (location != null) {
                        lastKnownLocations.put(uuid, location);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load last location for " + uuidString + ": " + e.getMessage());
                }
            }
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("Loaded " + pendingTeleports.size() + " pending teleports and " + 
                lastKnownLocations.size() + " last known locations");
        }
    }
    
    public void saveData() {
        // Save pending teleports
        dataConfig.set("pending-teleports", null); // Clear existing data
        if (!pendingTeleports.isEmpty()) {
            for (Map.Entry<UUID, Location> entry : pendingTeleports.entrySet()) {
                dataConfig.set("pending-teleports." + entry.getKey().toString(), 
                    serializeLocation(entry.getValue()));
            }
        }
        
        // Save last known locations
        dataConfig.set("last-locations", null); // Clear existing data
        if (!lastKnownLocations.isEmpty()) {
            for (Map.Entry<UUID, Location> entry : lastKnownLocations.entrySet()) {
                dataConfig.set("last-locations." + entry.getKey().toString(), 
                    serializeLocation(entry.getValue()));
            }
        }
        
        try {
            dataConfig.save(dataFile);
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("Saved data to data.yml");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }
    
    // Pending teleports management
    public void setPendingTeleport(UUID playerUuid, Location location) {
        pendingTeleports.put(playerUuid, location);
        saveData();
        
        if (plugin.getConfigManager().isDebugMode()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
            plugin.getLogger().info("Set pending teleport for " + player.getName() + " to " + 
                location.getWorld().getName() + ":" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
        }
    }
    
    public Location getPendingTeleport(UUID playerUuid) {
        return pendingTeleports.get(playerUuid);
    }
    
    public boolean hasPendingTeleport(UUID playerUuid) {
        return pendingTeleports.containsKey(playerUuid);
    }
    
    public void removePendingTeleport(UUID playerUuid) {
        pendingTeleports.remove(playerUuid);
        saveData();
        
        if (plugin.getConfigManager().isDebugMode()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
            plugin.getLogger().info("Removed pending teleport for " + player.getName());
        }
    }
    
    // Last known locations management
    public void setLastKnownLocation(UUID playerUuid, Location location) {
        lastKnownLocations.put(playerUuid, location);
        saveData();
        
        if (plugin.getConfigManager().isDebugMode()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
            plugin.getLogger().info("Updated last known location for " + player.getName() + " to " + 
                location.getWorld().getName() + ":" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
        }
    }
    
    public Location getLastKnownLocation(UUID playerUuid) {
        return lastKnownLocations.get(playerUuid);
    }
    
    public Location getLastKnownLocation(OfflinePlayer player) {
        return getLastKnownLocation(player.getUniqueId());
    }
    
    // Utility methods for location serialization
    private String serializeLocation(Location location) {
        if (location == null || location.getWorld() == null) return null;
        
        return location.getWorld().getName() + ":" + 
               location.getX() + ":" + 
               location.getY() + ":" + 
               location.getZ() + ":" + 
               location.getYaw() + ":" + 
               location.getPitch();
    }
    
    private Location deserializeLocation(String locationString) {
        if (locationString == null || locationString.isEmpty()) return null;
        
        try {
            String[] parts = locationString.split(":");
            if (parts.length != 6) return null;
            
            World world = plugin.getMultiverseHelper().getWorld(parts[0]);
            if (world == null) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().warning("World '" + parts[0] + "' not found when deserializing location");
                }
                return null;
            }
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize location '" + locationString + "': " + e.getMessage());
            return null;
        }
    }
    
    // Cleanup methods
    public void cleanupOldData() {
        // Remove pending teleports for players who haven't been online in a while
        // This could be implemented to clean up very old pending teleports
        // For now, we keep all data as it should be relatively small
    }
} 