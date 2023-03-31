package com.tcoded.lifesteal.util;

import com.tcoded.lifesteal.model.PlayerData;
import com.tcoded.lifesteal.model.PlayerGroupData;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayerDataUtil {
    public static void saveCurrentStateToPlayerNonGroupData(Player player, PlayerData playerData, AttributeInstance maxHp) {
        if (playerData.getMaxHpBeforeEnteringLifesteal() <= 0) return; // don't change anything if set to 0 or lower
        if (maxHp != null) playerData.setMaxHpBeforeEnteringLifesteal(HeartsUtil.doubleHpToIntHp(maxHp.getBaseValue()));
        playerData.setHpBeforeEnteringLifesteal(HeartsUtil.doubleHpToIntHp(player.getHealth()));
    }

    public static void applyPlayerGroupDataToPlayer(Player player, PlayerGroupData groupData, AttributeInstance maxHp) {
        if (groupData.getMaxHp() <= 0) return; // don't change anything if set to 0 or lower
        if (maxHp != null) maxHp.setBaseValue(groupData.getMaxHp());
        player.setHealth(groupData.getHp());
    }

    public static void saveCurrentStateToPlayerGroupData(Player player, PlayerGroupData groupData, AttributeInstance maxHp) {
        if (groupData.getMaxHp() <= 0) return; // don't change anything if set to 0 or lower
        if (maxHp != null) groupData.setMaxHp(HeartsUtil.doubleHpToIntHp(maxHp.getBaseValue()));
        groupData.setHp(HeartsUtil.doubleHpToIntHp(player.getHealth()));
    }

    public static void applyPlayerNonGroupDataToPlayer(Player player, PlayerData playerData, AttributeInstance maxHp) {
        if (playerData.getMaxHpBeforeEnteringLifesteal() <= 0) return; // don't change anything if set to 0 or lower
        if (maxHp != null) maxHp.setBaseValue(playerData.getMaxHpBeforeEnteringLifesteal());
        player.setHealth(playerData.getHpBeforeEnteringLifesteal());
    }
}
