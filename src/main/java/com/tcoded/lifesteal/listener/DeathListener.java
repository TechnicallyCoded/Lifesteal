package com.tcoded.lifesteal.listener;

import com.tcoded.lifesteal.Lifesteal;
import com.tcoded.lifesteal.manager.PlayerDataManager;
import com.tcoded.lifesteal.model.LifestealGroup;
import com.tcoded.lifesteal.model.PlayerData;
import com.tcoded.lifesteal.model.PlayerGroupData;
import com.tcoded.lifesteal.util.HeartChangeResult;
import com.tcoded.lifesteal.util.HeartsUtil;
import com.tcoded.lifesteal.util.PlayerDataUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final Lifesteal plugin;

    public DeathListener(Lifesteal plugin) {
        this.plugin = plugin;
    }

    // on death event
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        boolean allowLifestealing = this.plugin.isAllowLifestealing();
        if (!allowLifestealing) {
            return;
        }

        boolean loseHeartsOnNonPlayerDeath = this.plugin.isLoseHeartsOnNonPlayerDeath();
        int hpLoseAmount = this.plugin.getHpLoseAmount();
        int maxHp = this.plugin.getMaxHp();

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        LifestealGroup lifestealGroup = this.plugin.getLifestealGroupByWorldName(victim.getWorld().getName());
        if (lifestealGroup == null) {
            return;
        }

        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        PlayerData victimPlayerData = playerDataManager.getPlayerData(victim.getUniqueId());
        PlayerGroupData victimGroupData = victimPlayerData.getGroupData(lifestealGroup.getName());
        int minHp = lifestealGroup.getMinHealth();

        if (killer == null) {
            if (loseHeartsOnNonPlayerDeath) {
                HeartChangeResult victimResult = HeartsUtil.loseHearts(victim, hpLoseAmount, minHp, maxHp);
                if (victimResult == HeartChangeResult.ZERO_HP_REACHED) {
                    if (victimGroupData != null) victimGroupData.setMaxHp(0);

                    String hubWorldName = lifestealGroup.getHubWorld();
                    if (hubWorldName != null) {
                        World world = this.plugin.getServer().getWorld(hubWorldName);
                        if (world != null) victim.teleport(world.getSpawnLocation());
                    }
                }
                sendVictimLoseHpMessage(hpLoseAmount, maxHp, victim, victimResult);
            }
        } else {
            HeartChangeResult victimResult = HeartsUtil.loseHearts(victim, hpLoseAmount, minHp, maxHp);
            HeartChangeResult killerResult = HeartsUtil.gainHearts(killer, hpLoseAmount, maxHp);

            // Victim
            sendVictimLoseHpMessage(hpLoseAmount, maxHp, victim, victimResult);

            // Killer
            sendKillerGainHpMessage(hpLoseAmount, maxHp, killer, killerResult);
        }

        // Save player data to disk asynchronously
        savePlayerData(victim, lifestealGroup, playerDataManager);
        if (killer != null) savePlayerData(killer, lifestealGroup, playerDataManager);
    }

    private static void savePlayerData(Player player, LifestealGroup lifestealGroup, PlayerDataManager playerDataManager) {
        AttributeInstance playerMaxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (playerMaxHp == null) {
            return;
        }

        // Get player group data object or create new one
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        PlayerGroupData playerGroupData = playerData.getGroupData(lifestealGroup.getName());
        if (playerGroupData == null) {
            playerGroupData = new PlayerGroupData(lifestealGroup.getName(), HeartsUtil.doubleHpToIntHp(player.getHealth()), HeartsUtil.doubleHpToIntHp(playerMaxHp.getValue()));
            playerData.addGroupData(playerGroupData);
        }

        // Write to player data object
        PlayerDataUtil.saveCurrentStateToPlayerGroupData(player, playerGroupData, playerMaxHp);

        // Write to disk asynchronously
        playerDataManager.savePlayerDataAsync(playerData);
    }

    private static void sendKillerGainHpMessage(int hpLoseAmount, int maxHp, Player killer, HeartChangeResult killerResult) {
        TextComponent message;

        switch (killerResult) {
            case MAX_HP_REACHED:
                message = Component.text("You reached the max amount of hp (%d)!".formatted(maxHp), NamedTextColor.RED);
                break;
            case SUCCESS:
                message = Component.text("You gained %d hp!".formatted(hpLoseAmount), NamedTextColor.GREEN);
                break;
            default:
                return;
        }

        killer.sendMessage(message);
    }

    private static void sendVictimLoseHpMessage(int hpLoseAmount, int maxHp, Player victim, HeartChangeResult victimResult) {
        TextComponent message;

        switch (victimResult) {
            case ZERO_HP_REACHED:
                message = Component.text("You lost all your hearts! You can't rejoin this world anymore.", NamedTextColor.RED);
                break;
            case MIN_HP_REACHED:
                message = Component.text("You reached the min amount of hp (%d)!".formatted(maxHp), NamedTextColor.RED);
                break;
            case SUCCESS:
                message = Component.text("You lost %d hp!".formatted(hpLoseAmount), NamedTextColor.GREEN);
                break;
            default:
                return;
        }

        victim.sendMessage(message);
    }
}
