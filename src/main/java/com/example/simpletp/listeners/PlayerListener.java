package com.example.simpletp.listeners;

import com.example.simpletp.SimpleTP;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
    
    private final SimpleTP plugin;
    
    public PlayerListener(SimpleTP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has a pending teleport
        if (plugin.getDataManager().hasPendingTeleport(player.getUniqueId())) {
            Location teleportLocation = plugin.getDataManager().getPendingTeleport(player.getUniqueId());
            
            if (teleportLocation != null && plugin.getMultiverseHelper().isValidLocation(teleportLocation)) {
                // Delay the teleport slightly to ensure the player is fully loaded
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            boolean success = plugin.getMultiverseHelper().safeTeleport(player, teleportLocation);
                            
                            if (success) {
                                String message = plugin.getConfig().getString("messages.teleported", "&aYou have been teleported!");
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                
                                if (plugin.getConfigManager().isDebugMode()) {
                                    plugin.getLogger().info("Successfully teleported " + player.getName() + " on join");
                                }
                            } else {
                                plugin.getLogger().warning("Failed to teleport " + player.getName() + " on join");
                                player.sendMessage(ChatColor.RED + "Failed to teleport you to the requested location.");
                            }
                            
                            // Remove the pending teleport regardless of success
                            plugin.getDataManager().removePendingTeleport(player.getUniqueId());
                        }
                    }
                }.runTaskLater(plugin, 20L); // Wait 1 second (20 ticks)
            } else {
                // Invalid location, remove the pending teleport
                plugin.getDataManager().removePendingTeleport(player.getUniqueId());
                plugin.getLogger().warning("Removed invalid pending teleport for " + player.getName());
            }
        }
        
        // Update last known location
        plugin.getDataManager().setLastKnownLocation(player.getUniqueId(), player.getLocation());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Save the player's last known location
        plugin.getDataManager().setLastKnownLocation(player.getUniqueId(), player.getLocation());
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("Saved logout location for " + player.getName());
        }
    }
} 