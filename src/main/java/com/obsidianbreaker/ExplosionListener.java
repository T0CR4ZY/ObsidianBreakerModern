package com.obsidianbreaker;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class ExplosionListener implements Listener {

    private final ObsidianBreakerModern plugin;

    public ExplosionListener(ObsidianBreakerModern plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // Handle TNT, Creeper, Wither, etc.
        Location explosionLoc = event.getLocation();
        
        // Check if underwater explosions are disabled
        if (!plugin.isUnderwaterExplosions() && isUnderwater(explosionLoc)) {
            return;
        }

        List<Block> blocksToRemove = new ArrayList<>();
        int radius = plugin.getExplosionRadius();

        // Check blocks in radius around explosion
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = explosionLoc.getBlock().getRelative(x, y, z);
                    
                    if (processBlock(block, explosionLoc)) {
                        blocksToRemove.add(block);
                    }
                }
            }
        }

        // Add broken blocks to the explosion
        for (Block block : blocksToRemove) {
            if (!event.blockList().contains(block)) {
                event.blockList().add(block);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        // Handle bed explosions, respawn anchor explosions, etc.
        Location explosionLoc = event.getBlock().getLocation();

        if (!plugin.isUnderwaterExplosions() && isUnderwater(explosionLoc)) {
            return;
        }

        List<Block> blocksToRemove = new ArrayList<>();
        int radius = plugin.getExplosionRadius();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = explosionLoc.getBlock().getRelative(x, y, z);
                    
                    if (processBlock(block, explosionLoc)) {
                        blocksToRemove.add(block);
                    }
                }
            }
        }

        for (Block block : blocksToRemove) {
            if (!event.blockList().contains(block)) {
                event.blockList().add(block);
            }
        }
    }

    private boolean processBlock(Block block, Location explosionLoc) {
        String blockType = block.getType().name();
        int maxDurability = plugin.getDurability(blockType);

        // Block not in config, ignore
        if (maxDurability <= 0) {
            return false;
        }

        // Calculate distance for damage falloff (optional)
        double distance = block.getLocation().distance(explosionLoc);
        if (distance > plugin.getExplosionRadius()) {
            return false;
        }

        // Get current damage and add 1
        BlockDamageManager manager = BlockDamageManager.getInstance();
        int currentDamage = manager.getDamage(block.getLocation());
        currentDamage++;

        // Check if block should break
        if (currentDamage >= maxDurability) {
            // Block breaks!
            manager.removeDamage(block.getLocation());
            
            // Play break effect
            playBreakEffect(block);
            
            return true;
        } else {
            // Block takes damage but doesn't break
            manager.setDamage(block.getLocation(), currentDamage);
            
            // Show crack effect
            if (plugin.isShowCracks()) {
                showCrackEffect(block, currentDamage, maxDurability);
            }
            
            // Play damage sound
            playDamageSound(block);
            
            return false;
        }
    }

    private boolean isUnderwater(Location loc) {
        Block block = loc.getBlock();
        return block.getType() == Material.WATER || 
               block.isLiquid();
    }

    private void playBreakEffect(Block block) {
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        block.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
        block.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }

    private void playDamageSound(Block block) {
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        block.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
    }

    private void showCrackEffect(Block block, int currentDamage, int maxDurability) {
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        
        // Calculate crack stage (0-9)
        float damagePercent = (float) currentDamage / maxDurability;
        int crackStage = (int) (damagePercent * 9);
        
        // Send block crack animation to nearby players
        block.getWorld().getPlayers().forEach(player -> {
            if (player.getLocation().distance(loc) <= 64) {
                // Spawn particles to show damage
                player.spawnParticle(Particle.BLOCK, loc, 10, 0.3, 0.3, 0.3, block.getBlockData());
            }
        });
    }
}
