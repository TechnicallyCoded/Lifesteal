package com.tcoded.lifesteal.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerData {

    private final UUID uuid;

    private final AtomicBoolean isNew;

    int hpBeforeEnteringLifesteal;
    int maxHpBeforeEnteringLifesteal;
    private final HashMap<String, PlayerGroupData> groupData = new HashMap<>();
    private AtomicBoolean saving;

    public PlayerData(UUID uuid, boolean isNew) {
        this.uuid = uuid;
        this.isNew = new AtomicBoolean(isNew);
        this.saving = new AtomicBoolean(false);
        hpBeforeEnteringLifesteal = 20;
        maxHpBeforeEnteringLifesteal = 20;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isNew() {
        return isNew.get();
    }

    public int getHpBeforeEnteringLifesteal() {
        return hpBeforeEnteringLifesteal;
    }

    public void setHpBeforeEnteringLifesteal(int hpBeforeEnteringLifesteal) {
        this.hpBeforeEnteringLifesteal = hpBeforeEnteringLifesteal;
    }

    public int getMaxHpBeforeEnteringLifesteal() {
        return maxHpBeforeEnteringLifesteal;
    }

    public void setMaxHpBeforeEnteringLifesteal(int maxHpBeforeEnteringLifesteal) {
        this.maxHpBeforeEnteringLifesteal = maxHpBeforeEnteringLifesteal;
    }

    public HashMap<String, PlayerGroupData> getAllGroupData() {
        return groupData;
    }

    public PlayerGroupData getGroupData(String group) {
        return groupData.get(group);
    }

    public void readConfigData(FileConfiguration config) {
        // clear existing data
        groupData.clear();

        // read new data
        hpBeforeEnteringLifesteal = config.getInt("hp-before-entering-lifesteal", 20);
        maxHpBeforeEnteringLifesteal = config.getInt("max-hp-before-entering-lifesteal-hp", 20);
        ConfigurationSection groupsSections = config.getConfigurationSection("groups");

        if (groupsSections == null) return;
        groupsSections.getKeys(false).forEach(group -> {
            int hp = config.getInt("groups." + group + ".hp");
            int maxHp = config.getInt("groups." + group + ".max-hp");
            groupData.put(group, new PlayerGroupData(group, hp, maxHp));
        });
    }

    public void writeConfigData(FileConfiguration config) {
        config.set("hp-before-entering-lifesteal", hpBeforeEnteringLifesteal);
        config.set("max-hp-before-entering-lifesteal", maxHpBeforeEnteringLifesteal);
        config.set("groups", null);
        groupData.forEach((group, data) -> {
            config.set("groups." + group + ".hp", data.getHp());
            config.set("groups." + group + ".max-hp", data.getMaxHp());
        });
    }

    public void addGroupData(PlayerGroupData groupData) {
        this.groupData.put(groupData.getName(), groupData);
    }

    public AtomicBoolean isSaving() {
        return this.saving;
    }
}
