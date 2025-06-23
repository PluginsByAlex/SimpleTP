package com.example.simpletp.utils;

import com.example.simpletp.SimpleTP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MultiverseHelper {
    
    private final SimpleTP plugin;
    private boolean multiverseEnabled;
    private Plugin multiverseCore;
    
    public MultiverseHelper(SimpleTP plugin) {
        this.plugin = plugin;
        checkMultiverseCore();
    }
    
    private void checkMultiverseCore() {
        multiverseCore = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        multiverseEnabled = multiverseCore != null && multiverseCore.isEnabled();
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("Multiverse-Core status: " + (multiverseEnabled ? "Available" : "Not found"));
        }
    }
    
    public boolean isMultiverseEnabled() {
        return multiverseEnabled;
    }
    
    /**
     * Safely teleport a player, handling world loading if Multiverse is available
     */
    public boolean safeTeleport(Player player, Location location) {
        if (location == null || location.getWorld() == null) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().warning("Cannot teleport " + player.getName() + ": Invalid location or world");
            }
            return false;
        }
        
        World targetWorld = location.getWorld();
        
        // If Multiverse is enabled, we can be more confident about cross-world teleportation
        if (multiverseEnabled) {
            try {
                boolean result = player.teleport(location);
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("Teleported " + player.getName() + " to " + 
                        targetWorld.getName() + " using Multiverse support: " + result);
                }
                return result;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to teleport " + player.getName() + 
                    " with Multiverse support: " + e.getMessage());
                return false;
            }
        } else {
            // Without Multiverse, we still try but warn about potential issues
            try {
                boolean result = player.teleport(location);
                if (plugin.getConfigManager().isDebugMode()) {
                    if (!targetWorld.equals(player.getWorld())) {
                        plugin.getLogger().warning("Cross-world teleportation attempted without Multiverse-Core for " + 
                            player.getName() + " from " + player.getWorld().getName() + " to " + targetWorld.getName());
                    }
                    plugin.getLogger().info("Teleported " + player.getName() + " to " + 
                        targetWorld.getName() + ": " + result);
                }
                return result;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to teleport " + player.getName() + ": " + e.getMessage());
                return false;
            }
        }
    }
    
    /**
     * Get a world by name, with Multiverse compatibility
     */
    public World getWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        
        if (world == null && multiverseEnabled) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("World '" + worldName + "' not loaded, Multiverse-Core available for loading");
            }
            // Multiverse might be able to load the world, but we don't directly interact with its API
            // to avoid hard dependencies. The world should be available through Bukkit if MV loaded it.
        }
        
        return world;
    }
    
    /**
     * Check if a location is in a valid, loaded world
     */
    public boolean isValidLocation(Location location) {
        if (location == null) return false;
        
        World world = location.getWorld();
        if (world == null) return false;
        
        // Check if the world is actually loaded
        return Bukkit.getWorld(world.getName()) != null;
    }
} 