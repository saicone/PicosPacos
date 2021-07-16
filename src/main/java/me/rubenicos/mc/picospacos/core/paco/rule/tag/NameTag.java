package me.rubenicos.mc.picospacos.core.paco.rule.tag;

import me.rubenicos.mc.picospacos.core.paco.rule.TagType;
import me.rubenicos.mc.picospacos.module.Locale;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class NameTag extends TagType {
    private final String name;

    public NameTag(String name, boolean papi, String comparator) {
        super(papi, comparator);
        this.name = Locale.color(name);
    }

    @SuppressWarnings("all")
    @Override
    public boolean valid(ItemStack item, Player player) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return comparator.compareString(format(name, player), item.getItemMeta().getDisplayName());
        } else {
            return false;
        }
    }
}
