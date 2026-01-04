package com.obsidianbreaker;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ObsidianBreakerCommand implements CommandExecutor, TabCompleter {

    private final ObsidianBreakerModern plugin;

    public ObsidianBreakerCommand(ObsidianBreakerModern plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("obsidianbreaker.reload")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                    return true;
                }
                plugin.loadConfiguration();
                sender.sendMessage(ChatColor.GREEN + "ObsidianBreakerModern configuration reloaded!");
                return true;

            case "check":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (!sender.hasPermission("obsidianbreaker.check")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                    return true;
                }
                checkBlock((Player) sender);
                return true;

            case "tool":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (!sender.hasPermission("obsidianbreaker.check")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                    return true;
                }
                toggleTool((Player) sender);
                return true;

            case "settool":
                if (!sender.hasPermission("obsidianbreaker.settool")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ob settool <material>");
                    sender.sendMessage(ChatColor.GRAY + "Example: /ob settool STICK");
                    return true;
                }
                setTool(sender, args[1]);
                return true;

            case "clear":
                if (!sender.hasPermission("obsidianbreaker.clear")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                    return true;
                }
                BlockDamageManager.getInstance().clearAllDamage();
                sender.sendMessage(ChatColor.GREEN + "All block damage data has been cleared!");
                return true;

            case "list":
                if (!sender.hasPermission("obsidianbreaker.list")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                    return true;
                }
                listBlocks(sender, args);
                return true;

            case "stats":
                if (!sender.hasPermission("obsidianbreaker.stats")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                    return true;
                }
                showStats(sender);
                return true;

            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════ ObsidianBreakerModern ═══════");
        sender.sendMessage(ChatColor.YELLOW + "/ob tool " + ChatColor.WHITE + "- Toggle check tool mode");
        sender.sendMessage(ChatColor.YELLOW + "/ob check " + ChatColor.WHITE + "- Check block you're looking at");
        sender.sendMessage(ChatColor.YELLOW + "/ob settool <item> " + ChatColor.WHITE + "- Set check tool item");
        sender.sendMessage(ChatColor.YELLOW + "/ob list [page] " + ChatColor.WHITE + "- List configured blocks");
        sender.sendMessage(ChatColor.YELLOW + "/ob stats " + ChatColor.WHITE + "- Show plugin statistics");
        sender.sendMessage(ChatColor.YELLOW + "/ob reload " + ChatColor.WHITE + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/ob clear " + ChatColor.WHITE + "- Clear all damage data");
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
        sender.sendMessage(ChatColor.GRAY + "Tip: Use /ob tool then right-click blocks with " + 
                          ChatColor.WHITE + plugin.getCheckToolItem().name() + ChatColor.GRAY + " to check durability!");
    }

    private void toggleTool(Player player) {
        plugin.toggleCheckTool(player.getUniqueId());
        boolean enabled = plugin.hasCheckToolEnabled(player.getUniqueId());
        
        if (enabled) {
            player.sendMessage(ChatColor.GREEN + "Check tool mode " + ChatColor.WHITE + "ENABLED");
            player.sendMessage(ChatColor.GRAY + "Right-click blocks with " + ChatColor.YELLOW + 
                             plugin.getCheckToolItem().name() + ChatColor.GRAY + " to check durability.");
        } else {
            player.sendMessage(ChatColor.RED + "Check tool mode " + ChatColor.WHITE + "DISABLED");
        }
    }

    private void setTool(CommandSender sender, String materialName) {
        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            
            // Update config
            plugin.getConfig().set("settings.check-tool.item", material.name());
            plugin.saveConfig();
            plugin.loadConfiguration();
            
            sender.sendMessage(ChatColor.GREEN + "Check tool item set to: " + ChatColor.WHITE + material.name());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid material: " + materialName);
            sender.sendMessage(ChatColor.GRAY + "Use Minecraft material names like STICK, BLAZE_ROD, etc.");
        }
    }

    private void checkBlock(Player player) {
        Block block = player.getTargetBlockExact(5);
        
        if (block == null || block.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Look at a block to check its durability.");
            return;
        }

        String blockType = block.getType().name();
        int maxDurability = plugin.getDurability(blockType);

        if (maxDurability <= 0) {
            player.sendMessage(ChatColor.YELLOW + blockType + ChatColor.WHITE + " is not a breakable block.");
            return;
        }

        int currentDamage = BlockDamageManager.getInstance().getDamage(block.getLocation());
        int remainingHits = maxDurability - currentDamage;
        double healthPercent = ((double) remainingHits / maxDurability) * 100;

        // Build health bar
        int barLength = 20;
        int filledBars = (int) ((double) remainingHits / maxDurability * barLength);
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                bar.append(ChatColor.GREEN).append("█");
            } else {
                bar.append(ChatColor.RED).append("█");
            }
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "══════ Block Info ══════");
        player.sendMessage(ChatColor.YELLOW + "Block: " + ChatColor.WHITE + blockType);
        player.sendMessage(ChatColor.YELLOW + "Health: " + bar.toString() + ChatColor.WHITE + " " + String.format("%.0f", healthPercent) + "%");
        player.sendMessage(ChatColor.YELLOW + "Damage: " + ChatColor.WHITE + currentDamage + "/" + maxDurability);
        player.sendMessage(ChatColor.YELLOW + "Hits Left: " + ChatColor.WHITE + remainingHits);
        player.sendMessage(ChatColor.GOLD + "════════════════════════");
    }

    private void listBlocks(CommandSender sender, String[] args) {
        Map<String, Integer> blocks = plugin.getBlockDurabilities();
        
        if (blocks.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No blocks configured!");
            return;
        }

        // Pagination
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) blocks.size() / itemsPerPage);
        page = Math.max(1, Math.min(page, totalPages));

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(blocks.entrySet());
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, entries.size());

        sender.sendMessage(ChatColor.GOLD + "═══ Configured Blocks (Page " + page + "/" + totalPages + ") ═══");
        
        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            sender.sendMessage(ChatColor.YELLOW + entry.getKey() + ": " + ChatColor.WHITE + entry.getValue() + " hits");
        }

        if (page < totalPages) {
            sender.sendMessage(ChatColor.GRAY + "Use /ob list " + (page + 1) + " for next page");
        }
    }

    private void showStats(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══ ObsidianBreakerModern Stats ═══");
        sender.sendMessage(ChatColor.YELLOW + "Configured Blocks: " + ChatColor.WHITE + plugin.getBlockDurabilities().size());
        sender.sendMessage(ChatColor.YELLOW + "Damaged Blocks: " + ChatColor.WHITE + BlockDamageManager.getInstance().getTotalDamagedBlocks());
        sender.sendMessage(ChatColor.YELLOW + "Explosion Radius: " + ChatColor.WHITE + plugin.getExplosionRadius());
        sender.sendMessage(ChatColor.YELLOW + "Check Tool: " + ChatColor.WHITE + plugin.getCheckToolItem().name());
        sender.sendMessage(ChatColor.YELLOW + "Show Cracks: " + ChatColor.WHITE + plugin.isShowCracks());
        sender.sendMessage(ChatColor.YELLOW + "Persist Damage: " + ChatColor.WHITE + plugin.isPersistDamage());
        sender.sendMessage(ChatColor.YELLOW + "Underwater Explosions: " + ChatColor.WHITE + plugin.isUnderwaterExplosions());
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("tool", "check", "settool", "list", "stats", "reload", "clear", "help");
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("settool")) {
            // Suggest some common materials
            List<String> materials = Arrays.asList("STICK", "BLAZE_ROD", "BONE", "FEATHER", "PAPER", "CLOCK", "COMPASS");
            for (String mat : materials) {
                if (mat.startsWith(args[1].toUpperCase())) {
                    completions.add(mat);
                }
            }
        }
        
        return completions;
    }
}
