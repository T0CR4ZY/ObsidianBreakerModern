package com.obsidianbreaker;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ObsidianBreakerModern extends JavaPlugin {

    private static ObsidianBreakerModern instance;
    private Map<String, Integer> blockDurabilities;
    private int explosionRadius;
    private boolean showCracks;
    private boolean persistDamage;
    private boolean underwaterExplosions;
    private Material checkToolItem;
    private boolean checkToolEnabled;
    private Set<UUID> checkToolUsers;

    @Override
    public void onEnable() {
        instance = this;
        checkToolUsers = new HashSet<>();
        
        // Save default config
        saveDefaultConfig();
        
        // Load configuration
        loadConfiguration();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        getServer().getPluginManager().registerEvents(new CheckToolListener(this), this);
        
        // Register commands
        ObsidianBreakerCommand cmdExecutor = new ObsidianBreakerCommand(this);
        getCommand("ob").setExecutor(cmdExecutor);
        getCommand("ob").setTabCompleter(cmdExecutor);
        getCommand("obsidianbreaker").setExecutor(cmdExecutor);
        getCommand("obsidianbreaker").setTabCompleter(cmdExecutor);
        
        getLogger().info("ObsidianBreakerModern has been enabled!");
        getLogger().info("Loaded " + blockDurabilities.size() + " block durabilities.");
        getLogger().info("Check tool item: " + checkToolItem.name());
    }

    @Override
    public void onDisable() {
        // Save any pending block damage data
        BlockDamageManager.getInstance().saveData();
        getLogger().info("ObsidianBreakerModern has been disabled!");
    }

    public void loadConfiguration() {
        reloadConfig();
        FileConfiguration config = getConfig();
        
        blockDurabilities = new HashMap<>();
        explosionRadius = config.getInt("settings.explosion-radius", 3);
        showCracks = config.getBoolean("settings.show-cracks", true);
        persistDamage = config.getBoolean("settings.persist-damage", true);
        underwaterExplosions = config.getBoolean("settings.underwater-explosions", true);
        checkToolEnabled = config.getBoolean("settings.check-tool.enabled", true);
        
        // Load check tool item
        String toolName = config.getString("settings.check-tool.item", "STICK");
        try {
            checkToolItem = Material.valueOf(toolName.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid check tool item: " + toolName + ". Using STICK.");
            checkToolItem = Material.STICK;
        }
        
        // Load block durabilities
        if (config.contains("durability")) {
            for (String key : config.getConfigurationSection("durability").getKeys(false)) {
                int durability = config.getInt("durability." + key);
                blockDurabilities.put(key.toUpperCase(), durability);
            }
        }
        
        // Initialize block damage manager
        BlockDamageManager.getInstance().initialize(this, persistDamage);
    }

    public static ObsidianBreakerModern getInstance() {
        return instance;
    }

    public Map<String, Integer> getBlockDurabilities() {
        return blockDurabilities;
    }

    public int getExplosionRadius() {
        return explosionRadius;
    }

    public boolean isShowCracks() {
        return showCracks;
    }

    public boolean isPersistDamage() {
        return persistDamage;
    }

    public boolean isUnderwaterExplosions() {
        return underwaterExplosions;
    }

    public int getDurability(String blockType) {
        return blockDurabilities.getOrDefault(blockType.toUpperCase(), -1);
    }

    public Material getCheckToolItem() {
        return checkToolItem;
    }

    public boolean isCheckToolEnabled() {
        return checkToolEnabled;
    }

    public Set<UUID> getCheckToolUsers() {
        return checkToolUsers;
    }

    public void toggleCheckTool(UUID uuid) {
        if (checkToolUsers.contains(uuid)) {
            checkToolUsers.remove(uuid);
        } else {
            checkToolUsers.add(uuid);
        }
    }

    public boolean hasCheckToolEnabled(UUID uuid) {
        return checkToolUsers.contains(uuid);
    }
}
