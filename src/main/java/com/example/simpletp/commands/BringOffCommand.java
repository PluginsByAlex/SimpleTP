package com.example.simpletp.commands;

import com.example.simpletp.SimpleTP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BringOffCommand implements CommandExecutor {
    
    private final SimpleTP plugin;
    
    public BringOffCommand(SimpleTP plugin) {
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
            sender.sendMessage(ChatColor.RED + "Console cannot be a teleport destination for /bringoff command!");
            return true;
        }
        
        Player senderPlayer = (Player) sender;
        
        // Check arguments
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /bringoff <player>");
            return true;
        }
        
        String targetName = args[0];
        
        // First check if player is online
        Player onlinePlayer = Bukkit.getPlayer(targetName);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            sender.sendMessage(ChatColor.YELLOW + targetName + " is online! Use /bring instead.");
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
        
        // Don't set teleport for yourself
        if (offlinePlayer.getUniqueId().equals(senderPlayer.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You cannot set an offline teleport for yourself!");
            return true;
        }
        
        // Set the pending teleport
        plugin.getDataManager().setPendingTeleport(offlinePlayer.getUniqueId(), senderPlayer.getLocation());
        
        // Send confirmation message
        String message = plugin.getConfig().getString("messages.offline-tp-set", "&aOffline teleportation set for {player}. They will be teleported when they join!");
        message = message.replace("{player}", offlinePlayer.getName());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info(sender.getName() + " set offline teleport for " + offlinePlayer.getName() + " to their location");
        }
        
        return true;
    }
} 