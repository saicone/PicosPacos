package me.rubenicos.mc.picospacos.core.paco.rule.tag;

import me.rubenicos.mc.picospacos.core.paco.rule.TagType;
import me.rubenicos.mc.picospacos.util.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
            return (all ? comparator.compareListAll(format(flags, player), ItemUtils.flagsToList(item.getItemMeta().getItemFlags())) : comparator.compareList(format(flags, player), ItemUtils.flagsToList(item.getItemMeta().getItemFlags())));
        }
        return false;
    }
}
