package com.tcoded.lifesteal.model;

import java.util.List;

public class LifestealGroup {

    private final String name;
    private String hubWorld;
    private int minHealth;
    private List<String> worldNames;

    public LifestealGroup(String name, String hubWorld, int minHealth, List<String> worlds) {
        this.name = name;
        this.hubWorld = hubWorld;
        this.minHealth = minHealth;
        this.worldNames = worlds;
    }

    public String getName() {
        return name;
    }

    public String getHubWorld() {
        return hubWorld;
    }

    public int getMinHealth() {
        return minHealth;
    }

    public List<String> getWorldNames() {
        return worldNames;
    }

    public boolean isWorldInGroup(String name) {
        return this.worldNames.contains(name);
    }
}
