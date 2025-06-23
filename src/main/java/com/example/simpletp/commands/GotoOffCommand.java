package com.example.simpletp.commands;

import com.example.simpletp.SimpleTP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GotoOffCommand implements CommandExecutor {
    
    private final SimpleTP plugin;
    
    public GotoOffCommand(SimpleTP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("SimpleTP.offline")) {
            String message = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        // Check if offline TP is enabled
        if (!plugin.getConfigManager().isOfflineTpEnabled()) {
            String message = plugin.getConfig().getString("messages.feature-disabled", "&cThis feature is disabled in the configuration!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        // Check if console is allowed to use this command
        if (!(sender instanceof Player)) {
            if (!plugin.getConfigManager().isConsoleUseEnabled()) {
                String message = plugin.getConfig().getString("messages.console-not-allowed", "&cThis command cannot be used from console!");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return true;
            }
            sender.sendMessage(ChatColor.RED + "Console cannot teleport!");
            return true;
        }
        
        Player senderPlayer = (Player) sender;
        
        // Check arguments
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /gotooff <player>");
            return true;
        }
        
        String targetName = args[0];
        
        // First check if player is online
        Player onlinePlayer = Bukkit.getPlayer(targetName);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            sender.sendMessage(ChatColor.YELLOW + targetName + " is online! Use /goto instead.");
            return true;
        }
        
        // Get offline player
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
        
        // Check if player has ever played
        if (!offlinePlayer.hasPlayedBefore()) {
            String message = plugin.getConfig().getString("messages.player-not-found", "&cPlayer not found!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        // Don't teleport to yourself
        if (offlinePlayer.getUniqueId().equals(senderPlayer.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You cannot teleport to yourself!");
            return true;
        }
        
        // Get the last known location
        Location lastLocation = plugin.getDataManager().getLastKnownLocation(offlinePlayer);
        
        if (lastLocation == null) {
            sender.sendMessage(ChatColor.RED + "No last known location found for " + offlinePlayer.getName() + "!");
            return true;
        }
        
        if (!plugin.getMultiverseHelper().isValidLocation(lastLocation)) {
            sender.sendMessage(ChatColor.RED + "The last known location for " + offlinePlayer.getName() + " is in an unloaded or invalid world!");
            return true;
        }
        
        // Perform the teleportation
        boolean success = plugin.getMultiverseHelper().safeTeleport(senderPlayer, lastLocation);
        
        if (success) {
            // Send messages
            String teleportedMessage = plugin.getConfig().getString("messages.teleported-to-player", "&aYou have been teleported to {player}!");
            teleportedMessage = teleportedMessage.replace("{player}", offlinePlayer.getName() + "'s last location");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', teleportedMessage));
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info(sender.getName() + " teleported to " + offlinePlayer.getName() + "'s last location");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to teleport to " + offlinePlayer.getName() + "'s last location!");
            plugin.getLogger().warning("Failed to teleport " + sender.getName() + " to " + offlinePlayer.getName() + "'s last location");
        }
        
        return true;
    }
} 