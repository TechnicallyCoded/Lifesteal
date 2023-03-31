package com.tcoded.lifesteal.listener;

import com.tcoded.lifesteal.Lifesteal;
import com.tcoded.lifesteal.model.LifestealGroup;
import com.tcoded.lifesteal.model.PlayerData;
import com.tcoded.lifesteal.model.PlayerGroupData;
import com.tcoded.lifesteal.util.PlayerDataUtil;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class JoinListener implements Listener {

    private final Lifesteal plugin;

    public JoinListener(Lifesteal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(uuid);

        applyPlayerData(this.plugin, player, playerData);
    }

    public static void applyPlayerData(Lifesteal plugin, Player player, PlayerData playerData) {
        World world = player.getWorld();
        String worldName = world.getName();

        // Get group
        LifestealGroup group = plugin.getLifestealGroupByWorldName(worldName);
        if (group == null) {
            return;
        }

        // Max HP attribute
        AttributeInstance maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);


        // Create group data if it exists
        PlayerGroupData groupData = playerData.getGroupData(group.getName());
        if (groupData == null) {
            groupData = new PlayerGroupData(group.getName(), 20, 20);
            playerData.addGroupData(groupData);
        }

        // Kick player out of the world if they shouldn't be able to enter it anymore
        int groupDataMaxHp = groupData.getMaxHp();
        if (groupDataMaxHp <= 0) {
            String hubWorldName = group.getHubWorld();
            if (hubWorldName != null) {
                World hubWorld = plugin.getServer().getWorld(hubWorldName);
                if (hubWorld != null) player.teleport(hubWorld.getSpawnLocation());
                return;
            }
        }

        // Apply new player data
        PlayerDataUtil.applyPlayerGroupDataToPlayer(player, groupData, maxHp);

    }

}
