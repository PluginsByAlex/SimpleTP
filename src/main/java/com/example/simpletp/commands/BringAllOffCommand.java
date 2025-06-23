package com.example.simpletp.commands;

import com.example.simpletp.SimpleTP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class BringAllOffCommand implements CommandExecutor {
    
    private final SimpleTP plugin;
    
    public BringAllOffCommand(SimpleTP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("SimpleTP.bringall") || !sender.hasPermission("SimpleTP.offline")) {
            String message = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        // Check if bring all and offline TP are enabled
        if (!plugin.getConfigManager().isBringAllEnabled() || !plugin.getConfigManager().isOfflineTpEnabled()) {
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
            sender.sendMessage(ChatColor.RED + "Console cannot be a teleport destination for /bringalloff command!");
            return true;
        }
        
        Player senderPlayer = (Player) sender;
        
        // Check arguments
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /bringalloff");
            return true;
        }
        
        // Handle online players first
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int onlineTeleportedCount = 0;
        int onlineFailedCount = 0;
        
        for (Player player : onlinePlayers) {
            // Skip the sender
            if (player.equals(senderPlayer)) {
                continue;
            }
            
            boolean success = plugin.getMultiverseHelper().safeTeleport(player, senderPlayer.getLocation());
            
            if (success) {
                onlineTeleportedCount++;
                String targetMessage = plugin.getConfig().getString("messages.teleported", "&aYou have been teleported!");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', targetMessage));
            } else {
                onlineFailedCount++;
                plugin.getLogger().warning("Failed to bring " + player.getName() + " to " + sender.getName());
            }
        }
        
        // Handle offline players
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        int offlineSetCount = 0;
        
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            // Skip the sender and online players
            if (offlinePlayer.getUniqueId().equals(senderPlayer.getUniqueId()) || offlinePlayer.isOnline()) {
                continue;
            }
            
            // Check if player has ever played
            if (!offlinePlayer.hasPlayedBefore()) {
                continue;
            }
            
            // Set pending teleport
            plugin.getDataManager().setPendingTeleport(offlinePlayer.getUniqueId(), senderPlayer.getLocation());
            offlineSetCount++;
        }
        
        // Send result messages
        if (onlineTeleportedCount > 0) {
            sender.sendMessage(ChatColor.GREEN + "Successfully teleported " + onlineTeleportedCount + " online player(s) to you!");
        }
        
        if (offlineSetCount > 0) {
            sender.sendMessage(ChatColor.GREEN + "Set offline teleportation for " + offlineSetCount + " offline player(s)!");
        }
        
        if (onlineFailedCount > 0) {
            sender.sendMessage(ChatColor.RED + "Failed to teleport " + onlineFailedCount + " online player(s)!");
        }
        
        if (onlineTeleportedCount == 0 && offlineSetCount == 0 && onlineFailedCount == 0) {
            sender.sendMessage(ChatColor.YELLOW + "No other players found to teleport!");
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info(sender.getName() + " used bringalloff - Online: " + onlineTeleportedCount + 
                " teleported, " + onlineFailedCount + " failed. Offline: " + offlineSetCount + " set for teleport.");
        }
        
        return true;
    }
} 