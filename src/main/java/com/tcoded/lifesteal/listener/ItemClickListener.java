package com.tcoded.lifesteal.listener;

import com.tcoded.lifesteal.Lifesteal;
import com.tcoded.lifesteal.util.HeartChangeResult;
import com.tcoded.lifesteal.util.HeartsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

public class ItemClickListener implements Listener {

    private final Lifesteal plugin;

    public ItemClickListener(Lifesteal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        // Check if the event is cancelled
        if (event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        // Check holding something in the hand
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        // Check that the player is in a lifesteal world
        Player player = event.getPlayer();
        if (this.plugin.getLifestealGroupByWorldName(player.getWorld().getName()) == null) {
            return;
        }

        // Get keys to check
        String key = item.getType().getKey().getKey();
        String bonusItem = plugin.getBonusItemForRegen();

        // Other config values
        int maxBonusAmount = plugin.getMaxHpForBonusItem();
        int bonusAmount = 1;

        // Check if the item is the bonus item
        if (key.equals(bonusItem)) {
            HeartChangeResult result = HeartsUtil.gainHearts(player, bonusAmount, maxBonusAmount);

            removeOneItem(event, item);

            boolean reachedMax = result == HeartChangeResult.MAX_HP_REACHED;
            String message = reachedMax ?
                    "You reached the max amount of hearts for bonus items (%d)!".formatted(maxBonusAmount) :
                    "You gained %d heart!".formatted(bonusAmount);
            NamedTextColor color = reachedMax ?
                    NamedTextColor.RED :
                    NamedTextColor.GREEN;

            TextComponent text = Component.text(message, color);
            player.sendMessage(text);
        }
    }

    private void removeOneItem(PlayerInteractEvent event, ItemStack item) {
        int amount = item.getAmount();
        if (amount == 1) {
            PlayerInventory inventory = event.getPlayer().getInventory();

            // Find the item in the inventory and remove it
            @Nullable ItemStack[] contents = inventory.getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack slotItem = contents[i];
                if (slotItem == null) {
                    continue;
                }
                if (slotItem.equals(item)) {
                    inventory.setItem(i, null);
                    break;
                }
            }
        } else {
            item.setAmount(amount - 1);
        }
    }

}
