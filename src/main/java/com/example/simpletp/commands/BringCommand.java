package com.example.simpletp.commands;

import com.example.simpletp.SimpleTP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BringCommand implements CommandExecutor {
    
    private final SimpleTP plugin;
    
    public BringCommand(SimpleTP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("SimpleTP.bring")) {
            String message = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command!");
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
            sender.sendMessage(ChatColor.RED + "Console cannot be a teleport destination for /bring command!");
            return true;
        }
        
        Player senderPlayer = (Player) sender;
        
        // Check arguments
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /bring <player>");
            return true;
        }
        
        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetName);
        
        if (targetPlayer == null) {
            String message = plugin.getConfig().getString("messages.player-not-found", "&cPlayer not found!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        if (!targetPlayer.isOnline()) {
            String message = plugin.getConfig().getString("messages.player-offline", "&cPlayer is offline!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        // Don't teleport to yourself
        if (targetPlayer.equals(senderPlayer)) {
            sender.sendMessage(ChatColor.RED + "You cannot teleport yourself to yourself!");
            return true;
        }
        
        // Perform the teleportation
        boolean success = plugin.getMultiverseHelper().safeTeleport(targetPlayer, senderPlayer.getLocation());
        
        if (success) {
            // Send messages
            String teleportedMessage = plugin.getConfig().getString("messages.teleported-player", "&a{player} has been teleported to you!");
            teleportedMessage = teleportedMessage.replace("{player}", targetPlayer.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', teleportedMessage));
            
            String targetMessage = plugin.getConfig().getString("messages.teleported", "&aYou have been teleported!");
            targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', targetMessage));
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info(sender.getName() + " brought " + targetPlayer.getName() + " to their location");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to teleport " + targetPlayer.getName() + "!");
            plugin.getLogger().warning("Failed to bring " + targetPlayer.getName() + " to " + sender.getName());
        }
        
        return true;
    }
} 