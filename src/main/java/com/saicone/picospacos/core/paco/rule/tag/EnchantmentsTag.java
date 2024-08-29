package com.saicone.picospacos.core.paco.rule.tag;

import com.saicone.picospacos.core.paco.rule.ComparatorType;
import com.saicone.picospacos.core.paco.rule.TagType;
import com.saicone.types.Types;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;

public final class EnchantmentsTag extends TagType {
    private final Set<Map.Entry<String, Object[]>> enchantments;
    private final boolean all;
    private final ComparatorType lvlComparator;

    public EnchantmentsTag(Map<String, Object[]> enchantments, boolean all, boolean papi, String comparator, ComparatorType lvlComparator) {
        super(papi, comparator);
        this.enchantments = enchantments.entrySet();
        this.all = all;
        this.lvlComparator = lvlComparator;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean valid(ItemStack item, Player player) {
        if (!item.getEnchantments().isEmpty()) {
            for (Map.Entry<Enchantment, Integer> enchant : item.getEnchantments().entrySet()) {
                if (valid(enchant.getKey().getName(), enchant.getValue(), player)) {
                    if (!all) {
                        return true;
                    }
                } else if (all) {
                    return false;
                }
            }
            return all;
        }
        return false;
    }

    private boolean valid(String enchant, int level, Player player) {
        for (Map.Entry<String, Object[]> entry : enchantments) {
            if (comparator.compareString(format(entry.getKey(), player), enchant) &&
                    lvlComparator.compareInt(level, entry.getValue()[0] instanceof String ? Types.INTEGER.parse(format((String) entry.getValue()[0], player), 0) : (int) entry.getValue()[0], entry.getValue()[1] instanceof String ? Types.INTEGER.parse(format((String) entry.getValue()[1], player), 0) : (int) entry.getValue()[1])) {
                return true;
            }
        }
        return false;
    }
}
