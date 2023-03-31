package com.tcoded.lifesteal.manager;

import com.tcoded.lifesteal.Lifesteal;
import com.tcoded.lifesteal.model.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerDataManager {

    private final Lifesteal plugin;
    private final File playerDataFolder;

    private ConcurrentHashMap<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

    public PlayerDataManager(Lifesteal plugin) {
        this.plugin = plugin;


        File dataFolder = plugin.getDataFolder();
        playerDataFolder = new File(dataFolder, "playerdata");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }

    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    public void createPlayerDataFile(UUID uuid) {
        PlayerData data = new PlayerData(uuid, true);
        playerData.put(uuid, data);

        this.savePlayerData(data);
    }

    //load from file
    public PlayerData loadPlayerData(UUID uuid) {
        // load from file
        File file = new File(playerDataFolder, uuid.toString() + ".yml");
        if (!file.exists()) {
            return null;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        PlayerData data = new PlayerData(uuid, false);
        data.readConfigData(config);
        playerData.put(uuid, data);

        return data;
    }

    public void forgetPlayerData(UUID uuid) {
        playerData.remove(uuid);
    }

    public void savePlayerData(PlayerData playerData) {
        File file = new File(playerDataFolder, playerData.getUuid().toString() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        playerData.writeConfigData(config);

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> savePlayerDataAsync(PlayerData playerData) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            // File lock while saving
            AtomicBoolean savingFlag = playerData.isSaving();
            while (savingFlag.getAndSet(true)) {
                try {
                    // I know what I am doing.. I think
                    // noinspection SynchronizationOnLocalVariableOrMethodParameter
                    synchronized (savingFlag) {
                        savingFlag.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Write data
            savePlayerData(playerData);

            savingFlag.set(false);
            synchronized (savingFlag) {
                savingFlag.notifyAll();
            }

            future.complete(null);
        });

        return future;
    }

    public void replacePlayerData(UUID uuid, PlayerData playerData) {
        this.playerData.replace(uuid, playerData);
    }

    public void saveAllPlayerData() {
        for (PlayerData data : playerData.values()) {
            savePlayerData(data);
        }
    }
}
