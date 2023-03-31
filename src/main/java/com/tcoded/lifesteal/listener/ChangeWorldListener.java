package com.tcoded.lifesteal.listener;

import com.tcoded.lifesteal.Lifesteal;
import com.tcoded.lifesteal.manager.PlayerDataManager;
import com.tcoded.lifesteal.model.LifestealGroup;
import com.tcoded.lifesteal.model.PlayerData;
import com.tcoded.lifesteal.model.PlayerGroupData;
import com.tcoded.lifesteal.util.PlayerDataUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;

public class ChangeWorldListener implements Listener {

    private final Lifesteal plugin;

    public ChangeWorldListener(Lifesteal plugin) {
        this.plugin = plugin;
    }

    // Change world event listener spigot api
    // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/player/PlayerChangedWorldEvent.html
    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        System.out.println("ChangeWorldListener.onChangeWorld");
        Player player = event.getPlayer();
        String prevWorld = event.getFrom().getName();
        String currentWorld = player.getWorld().getName();

        // Player data
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        // Prev world
        LifestealGroup prevWorldGroup = plugin.getLifestealGroupByWorldName(prevWorld);
        boolean prevWorldIsInGroup = prevWorldGroup != null;
        PlayerGroupData prevWorldGroupData = prevWorldIsInGroup ? playerData.getGroupData(prevWorldGroup.getName()) : null;

        // Current world
        LifestealGroup currentWorldGroup = plugin.getLifestealGroupByWorldName(currentWorld);
        boolean currentWorldIsInGroup = currentWorldGroup != null;
        PlayerGroupData currentWorldGroupData = currentWorldIsInGroup ? playerData.getGroupData(currentWorldGroup.getName()) : null;

        // Max hp
        AttributeInstance maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (prevWorldIsInGroup && currentWorldIsInGroup) {
            // (switching between worlds NOT in the same group)
            if (!prevWorldGroup.getName().equals(currentWorldGroup.getName())) {
                // Save current hp and max hp
                PlayerDataUtil.saveCurrentStateToPlayerGroupData(player, prevWorldGroupData, maxHp);
                playerDataManager.savePlayerDataAsync(playerData);

                applyCurrentWorldData(player, playerData, currentWorldGroup, currentWorldGroupData, maxHp);
            }
        }
        else if (prevWorldIsInGroup && !currentWorldIsInGroup) {
            // Save current hp and max hp
            PlayerDataUtil.saveCurrentStateToPlayerGroupData(player, prevWorldGroupData, maxHp);
            playerDataManager.savePlayerDataAsync(playerData);

            // Set hp and max hp of the new world
            PlayerDataUtil.applyPlayerNonGroupDataToPlayer(player, playerData, maxHp);

        }
        else if (!prevWorldIsInGroup && currentWorldIsInGroup) {
            // Save current hp and max hp
            PlayerDataUtil.saveCurrentStateToPlayerNonGroupData(player, playerData, maxHp);
            playerDataManager.savePlayerDataAsync(playerData);

            applyCurrentWorldData(player, playerData, currentWorldGroup, currentWorldGroupData, maxHp);
        }
        else {
            // ignore (switching between worlds not in any group)
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        System.out.println("ChangeWorldListener.onTeleport");
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getWorld().equals(to.getWorld())) {
            return;
        }

        Player player = event.getPlayer();
        String currentWorld = to.getWorld().getName();

        // Player data
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        // Current world
        LifestealGroup worldGroup = plugin.getLifestealGroupByWorldName(currentWorld);
        boolean worldIsInGroup = worldGroup != null;
        PlayerGroupData playerGroupData = worldIsInGroup ? playerData.getGroupData(worldGroup.getName()) : null;

        if (worldIsInGroup) {
            // Check if the player is permitted to enter the world
            if (playerGroupData.getMaxHp() <= 0) {
                if (worldGroup.getMinHealth() <= 0) {
                    event.setCancelled(true);
                    player.sendMessage(Component.text("You died in this world and cannot enter it again.", NamedTextColor.RED));
                }
                else {
                    // Reset max hp of the new world to the min health of the
                    // group to allow the player to enter the world
                    playerGroupData.setMaxHp(worldGroup.getMinHealth());
                }
            }
        }

    }

    private static void applyCurrentWorldData(Player player, PlayerData playerData, LifestealGroup currentWorldGroup,
                                              PlayerGroupData currentWorldPlayerGroupData, AttributeInstance maxHp) {
        PlayerGroupData playerGroupData = currentWorldPlayerGroupData;

        if (playerGroupData == null) {
            playerGroupData = new PlayerGroupData(currentWorldGroup.getName(), 20, 20);
            playerData.addGroupData(playerGroupData);
            System.out.println("CHANGE WORLD groupData = " + playerGroupData);
        }

        // Set hp and max hp of the new world
        PlayerDataUtil.applyPlayerGroupDataToPlayer(player, playerGroupData, maxHp);
    }


}
