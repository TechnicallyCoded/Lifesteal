package com.tcoded.lifesteal.listener;

import com.tcoded.lifesteal.Lifesteal;
import com.tcoded.lifesteal.manager.PlayerDataManager;
import com.tcoded.lifesteal.model.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncPreLoginListener implements Listener {

    private final Lifesteal plugin;

    public AsyncPreLoginListener(Lifesteal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        PlayerData playerData = playerDataManager.loadPlayerData(event.getUniqueId());
        if (playerData == null) {
            playerDataManager.createPlayerDataFile(event.getUniqueId());
        }
    }

}
