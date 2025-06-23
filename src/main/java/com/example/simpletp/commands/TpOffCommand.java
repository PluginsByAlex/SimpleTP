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

public class TpOffCommand implements CommandExecutor {
    
    private final SimpleTP plugin;
    
    public TpOffCommand(SimpleTP plugin) {
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
        if (!(sender instanceof Player) && !plugin.getConfigManager().isConsoleUseEnabled()) {
            String message = plugin.getConfig().getString("messages.console-not-allowed", "&cThis command cannot be used from console!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        // Check arguments
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /tpoff <player1> <player2>");
            sender.sendMessage(ChatColor.GRAY + "Teleports player1 to player2 (supports offline players)");
            return true;
        }
        
        String player1Name = args[0];
        String player2Name = args[1];
        
        // Get player1 (the one to be teleported)
        Player player1Online = Bukkit.getPlayer(player1Name);
        OfflinePlayer player1Offline = Bukkit.getOfflinePlayer(player1Name);
        
        // Get player2 (the destination)
        Player player2Online = Bukkit.getPlayer(player2Name);
        OfflinePlayer player2Offline = Bukkit.getOfflinePlayer(player2Name);
        
        // Check if player1 exists
        if (player1Online == null && !player1Offline.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "Player '" + player1Name + "' not found!");
            return true;
        }
        
        // Check if player2 exists
        if (player2Online == null && !player2Offline.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "Player '" + player2Name + "' not found!");
            return true;
        }
        
        // Don't teleport player to themselves
        if (player1Name.equalsIgnoreCase(player2Name)) {
            sender.sendMessage(ChatColor.RED + "You cannot teleport a player to themselves!");
            return true;
        }
        
        Location destinationLocation = null;
        
        // Determine destination location
        if (player2Online != null && player2Online.isOnline()) {
            // Player2 is online, use their current location
            destinationLocation = player2Online.getLocation();
        } else {
            // Player2 is offline, use their last known location
            destinationLocation = plugin.getDataManager().getLastKnownLocation(player2Offline);
            
            if (destinationLocation == null) {
                sender.sendMessage(ChatColor.RED + "No last known location found for offline player '" + player2Name + "'!");
                return true;
            }
            
            if (!plugin.getMultiverseHelper().isValidLocation(destinationLocation)) {
                sender.sendMessage(ChatColor.RED + "The last known location for '" + player2Name + "' is in an unloaded or invalid world!");
                return true;
            }
        }
        
        // Perform teleportation
        if (player1Online != null && player1Online.isOnline()) {
            // Player1 is online, teleport immediately
            boolean success = plugin.getMultiverseHelper().safeTeleport(player1Online, destinationLocation);
            
            if (success) {
                String targetLocation = player2Online != null && player2Online.isOnline() ? 
                    player2Online.getName() : player2Offline.getName() + "'s last location";
                
                String teleportedMessage = plugin.getConfig().getString("messages.teleported-to-player", "&aYou have been teleported to {player}!");
                teleportedMessage = teleportedMessage.replace("{player}", targetLocation);
                player1Online.sendMessage(ChatColor.translateAlternateColorCodes('&', teleportedMessage));
                
                sender.sendMessage(ChatColor.GREEN + "Successfully teleported " + player1Online.getName() + " to " + targetLocation + "!");
                
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info(sender.getName() + " used tpoff to teleport " + player1Online.getName() + " to " + targetLocation);
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to teleport " + player1Online.getName() + "!");
                plugin.getLogger().warning("Failed tpoff command from " + sender.getName() + " for " + player1Online.getName());
            }
        } else {
            // Player1 is offline, set pending teleport
            plugin.getDataManager().setPendingTeleport(player1Offline.getUniqueId(), destinationLocation);
            
            String targetLocation = player2Online != null && player2Online.isOnline() ? 
                player2Online.getName() : player2Offline.getName() + "'s last location";
            
            String message = plugin.getConfig().getString("messages.offline-tp-set", "&aOffline teleportation set for {player}. They will be teleported when they join!");
            message = message.replace("{player}", player1Offline.getName() + " to " + targetLocation);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info(sender.getName() + " set offline teleport for " + player1Offline.getName() + " to " + targetLocation);
            }
        }
        
        return true;
    }
} 