package com.example.simpletp.commands;

import com.example.simpletp.SimpleTP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class BringAllCommand implements CommandExecutor {
    
    private final SimpleTP plugin;
    
    public BringAllCommand(SimpleTP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("SimpleTP.bringall")) {
            String message = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        // Check if bring all is enabled
        if (!plugin.getConfigManager().isBringAllEnabled()) {
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
            sender.sendMessage(ChatColor.RED + "Console cannot be a teleport destination for /bringall command!");
            return true;
        }
        
        Player senderPlayer = (Player) sender;
        
        // Check arguments
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /bringall");
            return true;
        }
        
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int teleportedCount = 0;
        int failedCount = 0;
        
        for (Player player : onlinePlayers) {
            // Skip the sender
            if (player.equals(senderPlayer)) {
                continue;
            }
            
            boolean success = plugin.getMultiverseHelper().safeTeleport(player, senderPlayer.getLocation());
            
            if (success) {
                teleportedCount++;
                String targetMessage = plugin.getConfig().getString("messages.teleported", "&aYou have been teleported!");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', targetMessage));
            } else {
                failedCount++;
                plugin.getLogger().warning("Failed to bring " + player.getName() + " to " + sender.getName());
            }
        }
        
        // Send result message
        if (teleportedCount > 0) {
            sender.sendMessage(ChatColor.GREEN + "Successfully teleported " + teleportedCount + " player(s) to you!");
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info(sender.getName() + " brought " + teleportedCount + " players to their location");
            }
        }
        
        if (failedCount > 0) {
            sender.sendMessage(ChatColor.RED + "Failed to teleport " + failedCount + " player(s)!");
        }
        
        if (teleportedCount == 0 && failedCount == 0) {
            sender.sendMessage(ChatColor.YELLOW + "No other players are online to teleport!");
        }
        
        return true;
    }
} 