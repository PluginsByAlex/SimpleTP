package com.example.simpletp.commands;

import com.example.simpletp.SimpleTP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SimpleTpCommand implements CommandExecutor {
    
    private final SimpleTP plugin;
    
    public SimpleTpCommand(SimpleTP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("SimpleTP.admin")) {
            String message = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        // Check arguments
        if (args.length == 0) {
            // Show plugin info
            sender.sendMessage(ChatColor.GREEN + "=== SimpleTP Plugin ===");
            sender.sendMessage(ChatColor.YELLOW + "Version: " + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "Author: " + plugin.getDescription().getAuthors().get(0));
            sender.sendMessage(ChatColor.YELLOW + "Commands: /bring, /bringoff, /bringall, /bringalloff, /goto, /gotooff, /tpoff");
            sender.sendMessage(ChatColor.GRAY + "Use '/simpletp reload' to reload the configuration");
            
            // Show status
            sender.sendMessage(ChatColor.GREEN + "=== Status ===");
            sender.sendMessage(ChatColor.YELLOW + "Offline TP: " + (plugin.getConfigManager().isOfflineTpEnabled() ? "Enabled" : "Disabled"));
            sender.sendMessage(ChatColor.YELLOW + "Bring All: " + (plugin.getConfigManager().isBringAllEnabled() ? "Enabled" : "Disabled"));
            sender.sendMessage(ChatColor.YELLOW + "Console Commands: " + (plugin.getConfigManager().isConsoleUseEnabled() ? "Enabled" : "Disabled"));
            sender.sendMessage(ChatColor.YELLOW + "Multiverse-Core: " + (plugin.getMultiverseHelper().isMultiverseEnabled() ? "Detected" : "Not found"));
            sender.sendMessage(ChatColor.YELLOW + "Debug Mode: " + (plugin.getConfigManager().isDebugMode() ? "Enabled" : "Disabled"));
            
            // Show data stats
            int pendingCount = 0;
            int locationCount = 0;
            try {
                // We can't easily get these counts without exposing the maps, so we'll skip for now
                sender.sendMessage(ChatColor.GRAY + "Use debug mode for detailed data information");
            } catch (Exception e) {
                // Ignore
            }
            
            return true;
        }
        
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            // Reload the plugin
            try {
                plugin.reload();
                
                String message = plugin.getConfig().getString("messages.reload-success", "&aSimpleTP configuration reloaded!");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                
                sender.sendMessage(ChatColor.GREEN + "Configuration values:");
                sender.sendMessage(ChatColor.YELLOW + "  Offline TP: " + plugin.getConfigManager().isOfflineTpEnabled());
                sender.sendMessage(ChatColor.YELLOW + "  Bring All: " + plugin.getConfigManager().isBringAllEnabled());
                sender.sendMessage(ChatColor.YELLOW + "  Console Commands: " + plugin.getConfigManager().isConsoleUseEnabled());
                sender.sendMessage(ChatColor.YELLOW + "  Debug Mode: " + plugin.getConfigManager().isDebugMode());
                
                plugin.getLogger().info("Configuration reloaded by " + sender.getName());
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Error reloading configuration: " + e.getMessage());
                plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
                e.printStackTrace();
            }
            
            return true;
        }
        
        // Invalid arguments
        sender.sendMessage(ChatColor.RED + "Usage: /simpletp [reload]");
        return true;
    }
} 