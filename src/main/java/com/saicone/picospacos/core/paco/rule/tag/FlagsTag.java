package com.saicone.picospacos.core.paco.rule.tag;

import com.saicone.picospacos.core.paco.rule.TagType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class FlagsTag extends TagType {
    private final List<String> flags;
    private final boolean all;

    public FlagsTag(List<String> flags, boolean all, boolean papi, String comparator) {
        super(papi, comparator);
        this.flags = flags;
        this.all = all;
    }

    @SuppressWarnings("all")
    @Override
    public boolean valid(ItemStack item, Player player) {
        if (item.hasItemMeta() && !item.getItemMeta().getItemFlags().isEmpty()) {
            return (all ? comparator.compareListAll(format(flags, player), flags(item.getItemMeta().getItemFlags())) : comparator.compareList(format(flags, player), flags(item.getItemMeta().getItemFlags())));
        }
        return false;
    }

    private static Set<String> flags(Set<ItemFlag> flags) {
        return flags.stream().map(String::valueOf).collect(Collectors.toSet());
    }
}
