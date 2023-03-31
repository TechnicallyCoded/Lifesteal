package com.tcoded.lifesteal.model;

/**
 * The player data that is stored for each player. This is only the data for one group and is stored in
 * the genral {@link PlayerData} class.
 */
public class PlayerGroupData {

    private String name;
    private int hp;
    private int maxHp;

    public PlayerGroupData(String name, int hp, int maxHp) {
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

}
