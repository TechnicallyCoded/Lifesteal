package com.tcoded.lifesteal.listener;

import com.tcoded.lifesteal.Lifesteal;
import com.tcoded.lifesteal.util.HeartChangeResult;
import com.tcoded.lifesteal.util.HeartsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
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
        if (event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        String key = item.getType().getKey().getKey();
        String bonusItem = plugin.getBonusItemForRegen();
        int maxBonusAmount = plugin.getMaxHpForBonusItem();
        int bonusAmount = 1;

        if (key.equals(bonusItem)) {
            HeartChangeResult result = HeartsUtil.gainHearts(event.getPlayer(), bonusAmount, maxBonusAmount);

            removeOneItem(event, item);

            boolean reachedMax = result == HeartChangeResult.MAX_HP_REACHED;
            String message = reachedMax ?
                    "You reached the max amount of hearts for bonus items (%d)!".formatted(maxBonusAmount) :
                    "You gained %d heart!".formatted(bonusAmount);
            NamedTextColor color = reachedMax ?
                    NamedTextColor.RED :
                    NamedTextColor.GREEN;

            TextComponent text = Component.text(message, color);
            event.getPlayer().sendMessage(text);
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
