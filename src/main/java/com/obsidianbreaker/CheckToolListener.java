package com.obsidianbreaker;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class CheckToolListener implements Listener {

    private final ObsidianBreakerModern plugin;

    public CheckToolListener(ObsidianBreakerModern plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click on block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Ignore off-hand to prevent double-fire
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        // Check if feature is enabled
        if (!plugin.isCheckToolEnabled()) {
            return;
        }

        // Check if player has the check tool in hand
        if (player.getInventory().getItemInMainHand().getType() != plugin.getCheckToolItem()) {
            return;
        }

        // Check if player has check tool mode enabled OR has permission for always-on
        if (!plugin.hasCheckToolEnabled(player.getUniqueId()) && !player.hasPermission("obsidianbreaker.checktool.always")) {
            return;
        }

        // Check permission
        if (!player.hasPermission("obsidianbreaker.check")) {
            return;
        }

        // Cancel the interaction to prevent placing blocks etc
        event.setCancelled(true);

        // Get block info
        String blockType = block.getType().name();
        int maxDurability = plugin.getDurability(blockType);

        if (maxDurability <= 0) {
            player.sendMessage(ChatColor.GRAY + "[OB] " + ChatColor.YELLOW + blockType + ChatColor.WHITE + " is not a breakable block.");
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

        // Send info to player
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "══════ Block Info ══════");
        player.sendMessage(ChatColor.YELLOW + "Block: " + ChatColor.WHITE + blockType);
        player.sendMessage(ChatColor.YELLOW + "Health: " + bar.toString() + ChatColor.WHITE + " " + String.format("%.0f", healthPercent) + "%");
        player.sendMessage(ChatColor.YELLOW + "Damage: " + ChatColor.WHITE + currentDamage + "/" + maxDurability);
        player.sendMessage(ChatColor.YELLOW + "Hits Left: " + ChatColor.WHITE + remainingHits);
        player.sendMessage(ChatColor.GOLD + "════════════════════════");
    }
}
