package com.obsidianbreaker;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlockDamageManager {

    private static BlockDamageManager instance;
    private Map<String, Integer> blockDamage;
    private File dataFile;
    private FileConfiguration dataConfig;
    private ObsidianBreakerModern plugin;
    private boolean persistData;

    private BlockDamageManager() {
        blockDamage = new HashMap<>();
    }

    public static BlockDamageManager getInstance() {
        if (instance == null) {
            instance = new BlockDamageManager();
        }
        return instance;
    }

    public void initialize(ObsidianBreakerModern plugin, boolean persistData) {
        this.plugin = plugin;
        this.persistData = persistData;

        if (persistData) {
            dataFile = new File(plugin.getDataFolder(), "damage_data.yml");
            loadData();

            // Auto-save every 5 minutes
            new BukkitRunnable() {
                @Override
                public void run() {
                    saveData();
                }
            }.runTaskTimerAsynchronously(plugin, 6000L, 6000L);
        }
    }

    public int getDamage(Location loc) {
        String key = locationToKey(loc);
        return blockDamage.getOrDefault(key, 0);
    }

    public void setDamage(Location loc, int damage) {
        String key = locationToKey(loc);
        blockDamage.put(key, damage);
    }

    public void removeDamage(Location loc) {
        String key = locationToKey(loc);
        blockDamage.remove(key);
    }

    public void clearAllDamage() {
        blockDamage.clear();
        if (persistData && dataFile != null && dataFile.exists()) {
            dataFile.delete();
        }
    }

    private String locationToKey(Location loc) {
        return loc.getWorld().getName() + ":" + 
               loc.getBlockX() + ":" + 
               loc.getBlockY() + ":" + 
               loc.getBlockZ();
    }

    public void loadData() {
        if (!persistData || dataFile == null) return;

        if (!dataFile.exists()) {
            return;
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        blockDamage.clear();

        for (String key : dataConfig.getKeys(false)) {
            blockDamage.put(key, dataConfig.getInt(key));
        }

        plugin.getLogger().info("Loaded " + blockDamage.size() + " damaged blocks from storage.");
    }

    public void saveData() {
        if (!persistData || dataFile == null) return;

        dataConfig = new YamlConfiguration();

        for (Map.Entry<String, Integer> entry : blockDamage.entrySet()) {
            dataConfig.set(entry.getKey(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save damage data: " + e.getMessage());
        }
    }

    public int getTotalDamagedBlocks() {
        return blockDamage.size();
    }
}
