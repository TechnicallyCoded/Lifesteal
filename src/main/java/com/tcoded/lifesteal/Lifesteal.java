package com.tcoded.lifesteal;

import com.google.common.collect.ImmutableList;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.lifesteal.listener.*;
import com.tcoded.lifesteal.manager.PlayerDataManager;
import com.tcoded.lifesteal.model.LifestealGroup;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Lifesteal extends JavaPlugin {

    // Utils
    private FoliaLib foliaLib;

    // Config
    private ImmutableList<LifestealGroup> lifestealGroups;
    private boolean loseHeartsOnNonPlayerDeath;
    private int hpLoseAmount;
    private boolean allowLifestealing;
    private int maxHp;
    private String bonusItemForRegen;
    private int maxHpForBonusItem;

    // Managers
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        // Utils
        this.foliaLib = new FoliaLib(this);

        // Load config
        saveDefaultConfig();

        // Get config values
        loseHeartsOnNonPlayerDeath = getConfig().getBoolean("lose-hearts-on-non-player-death");
        hpLoseAmount = getConfig().getInt("hp-lose-amount");
        allowLifestealing = getConfig().getBoolean("allow-lifestealing");
        maxHp = getConfig().getInt("max-hp");
        bonusItemForRegen = getConfig().getString("bonus-item-for-regen");
        maxHpForBonusItem = getConfig().getInt("max-hp-for-bonus-item");

        // Get data for each group
        ImmutableList.Builder<LifestealGroup> lifestealGroupsBuilder = ImmutableList.builder();
        for (String group : getConfig().getConfigurationSection("groups").getKeys(false)) {
            String hubWorld = getConfig().getString("groups." + group + ".hub-world");
            int minHealth = getConfig().getInt("groups." + group + ".min-health");
            List<String> worlds = getConfig().getStringList("groups." + group + ".worlds");

            // Save world to list
            LifestealGroup lifestealGroup = new LifestealGroup(group, hubWorld, minHealth, worlds);
            lifestealGroupsBuilder.add(lifestealGroup);
        }
        lifestealGroups = lifestealGroupsBuilder.build();

        // Initialize managers
        playerDataManager = new PlayerDataManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new AsyncPreLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new ChangeWorldListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemClickListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
    }

    @Override
    public void onDisable() {
        // unregister all listeners
        HandlerList.unregisterAll(this);

        // cancel all tasks
        foliaLib.getImpl().cancelAllTasks();

        // save player data
        playerDataManager.saveAllPlayerData();
    }

    // All getters

    public boolean isLoseHeartsOnNonPlayerDeath() {
        return loseHeartsOnNonPlayerDeath;
    }

    public int getHpLoseAmount() {
        return hpLoseAmount;
    }

    public boolean isAllowLifestealing() {
        return allowLifestealing;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public String getBonusItemForRegen() {
        return bonusItemForRegen;
    }

    public int getMaxHpForBonusItem() {
        return maxHpForBonusItem;
    }

    public LifestealGroup getLifestealGroup(String name) {
        for (LifestealGroup lifestealGroup : lifestealGroups) {
            if (lifestealGroup.getName().equals(name)) {
                return lifestealGroup;
            }
        }
        return null;
    }

    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }

    public boolean isWorldInGroup(String worldName) {
        return getLifestealGroup(worldName) != null;
    }

    public LifestealGroup getLifestealGroupByWorldName(String worldName) {
        return this.lifestealGroups.stream()
                .filter(lifestealGroup -> lifestealGroup.isWorldInGroup(worldName))
                .findFirst()
                .orElse(null);
    }

    public FoliaLib getFoliaLib() {
        return this.foliaLib;
    }
}
