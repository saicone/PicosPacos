package com.saicone.picospacos.core.paco.rule.tag;

import com.saicone.picospacos.core.paco.rule.TagType;
import com.saicone.picospacos.util.MStrings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class LoreTag extends TagType {
    private final List<String> lore;
    private final boolean all;

    public LoreTag(List<String> lore, boolean all, boolean papi, String comparator) {
        super(papi, comparator);
        this.lore = MStrings.color(lore);
        this.all = all;
    }

    @SuppressWarnings("all")
    @Override
    public boolean valid(ItemStack item, Player player) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            return (all ? comparator.compareListAll(format(lore, player), item.getItemMeta().getLore()) : comparator.compareList(format(lore, player), item.getItemMeta().getLore()));
        }
        return false;
    }
}
