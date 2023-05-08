package com.tcoded.lifesteal.listener;

import com.tcoded.folialib.enums.EntityTaskResult;
import com.tcoded.folialib.impl.ServerImplementation;
import com.tcoded.lifesteal.Lifesteal;
import com.tcoded.lifesteal.manager.PlayerDataManager;
import com.tcoded.lifesteal.model.LifestealGroup;
import com.tcoded.lifesteal.model.PlayerData;
import com.tcoded.lifesteal.model.PlayerGroupData;
import com.tcoded.lifesteal.util.PlayerDataUtil;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class QuitListener implements Listener {

    private final Lifesteal plugin;

    public QuitListener(Lifesteal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Event data
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();
        String worldName = world.getName();

        // Get player data
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(uuid);

        // Max hp attribute
        AttributeInstance maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        LifestealGroup lifestealGroup = this.plugin.getLifestealGroupByWorldName(worldName);
        if (lifestealGroup == null) {
            PlayerDataUtil.saveCurrentStateToPlayerNonGroupData(player, playerData, maxHp);
        } else {
            // Get group data
            PlayerGroupData groupData = playerData.getGroupData(lifestealGroup.getName());

            // Save current hp and max hp to player data
            PlayerDataUtil.saveCurrentStateToPlayerGroupData(player, groupData, maxHp);
        }


        // save player data
        playerDataManager.savePlayerDataAsync(playerData).thenRun(() -> {

            // remove data on the next tick
            Server server = plugin.getServer();

            // Folia support
            ServerImplementation foliaImpl = plugin.getFoliaLib().getImpl();

            // Run on the main thread or async for folia
            Player foundPlayer = foliaImpl.getPlayer(uuid);

            // Cleanup task
            Runnable cleanupTask = () -> {
                playerDataManager.forgetPlayerData(uuid);
            };

            CompletableFuture<EntityTaskResult> resultFuture = foliaImpl.runAtEntityWithFallback(foundPlayer, () -> {
                // Cleanup and remove player data
                if (foundPlayer == null || !foundPlayer.isOnline()) {
                    cleanupTask.run();
                }

                // The player is somehow back online - overwrite the data, the loaded data is probably outdated
                else {
                    playerDataManager.replacePlayerData(uuid, playerData);
                    JoinListener.applyPlayerData(plugin, foundPlayer, playerData);
                }
            }, cleanupTask);

            // If player's scheduler is retired, cleanup
            resultFuture.thenAccept(result -> {
                if (result == EntityTaskResult.SCHEDULER_RETIRED) {
                    cleanupTask.run();
                }
            });
        });
    }

}
