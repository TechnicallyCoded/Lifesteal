package com.tcoded.lifesteal.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class HeartsUtil {


    public static HeartChangeResult gainHearts(Player killer, int hpLoseAmount, int maxHp) {
        AttributeInstance maxHpAttribute = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHpAttribute == null) return null; // impossible?

        int currentHp = doubleHpToIntHp(maxHpAttribute.getValue());
        int newHp = currentHp + hpLoseAmount;

        HeartChangeResult result = HeartChangeResult.SUCCESS;

        if (newHp < 0) {
            newHp = 0;
            result = HeartChangeResult.MIN_HP_REACHED;
        } else if (newHp > maxHp) {
            newHp = maxHp;
            result = HeartChangeResult.MAX_HP_REACHED;
        }

        if (maxHpAttribute != null) {
            maxHpAttribute.setBaseValue(newHp);
        }

        return result;
    }

    public static HeartChangeResult loseHearts(Player victim, int hpLoseAmount, int minHp, int maxHp) {

        System.out.println("HeartsUtil.loseHearts");
        AttributeInstance maxHpAttribute = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHpAttribute == null) return null; // impossible?

        int currentHp = doubleHpToIntHp(maxHpAttribute.getValue());
        System.out.println("currentHp = " + currentHp);
        System.out.println("hpLoseAmount = " + hpLoseAmount);
        int newHp = currentHp - hpLoseAmount;
        System.out.println("newHp = " + newHp);

        HeartChangeResult result = HeartChangeResult.SUCCESS;

        if (newHp <= 0) {
            newHp = 0;
            result = HeartChangeResult.ZERO_HP_REACHED;
        } else if (newHp < minHp) {
            newHp = minHp;
            result = HeartChangeResult.MIN_HP_REACHED;
        } else if (newHp > maxHp) {
            newHp = maxHp;
            result = HeartChangeResult.MAX_HP_REACHED;
        }

        // Can't set max hp attribute to 0, so we just return the result if under
        if (newHp > 0) {
            maxHpAttribute.setBaseValue(newHp);
            if (victim.getHealth() > newHp) {
                victim.setHealth(newHp);
            }
        }

        return result;
    }

    public static int doubleHpToIntHp(double hp) {
        // Ceil to not accidentally kill the player
        return (int) Math.ceil(hp);
    }

}
