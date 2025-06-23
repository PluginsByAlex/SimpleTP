package com.example.simpletp.commands;

import com.example.simpletp.SimpleTP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GotoCommand implements CommandExecutor {
    
    private final SimpleTP plugin;
    
    public GotoCommand(SimpleTP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("SimpleTP.goto")) {
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
            sender.sendMessage(ChatColor.RED + "Console cannot teleport!");
            return true;
        }
        
        Player senderPlayer = (Player) sender;
        
        // Check arguments
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /goto <player>");
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
            sender.sendMessage(ChatColor.RED + "You cannot teleport to yourself!");
            return true;
        }
        
        // Perform the teleportation
        boolean success = plugin.getMultiverseHelper().safeTeleport(senderPlayer, targetPlayer.getLocation());
        
        if (success) {
            // Send messages
            String teleportedMessage = plugin.getConfig().getString("messages.teleported-to-player", "&aYou have been teleported to {player}!");
            teleportedMessage = teleportedMessage.replace("{player}", targetPlayer.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', teleportedMessage));
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info(sender.getName() + " teleported to " + targetPlayer.getName());
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to teleport to " + targetPlayer.getName() + "!");
            plugin.getLogger().warning("Failed to teleport " + sender.getName() + " to " + targetPlayer.getName());
        }
        
        return true;
    }
} 