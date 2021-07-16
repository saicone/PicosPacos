package me.rubenicos.mc.picospacos.core.paco.rule.tag;

import me.rubenicos.mc.picospacos.core.paco.rule.TagType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class MaterialTag extends TagType {
    private final String mat;

    public MaterialTag(String mat, boolean papi, String comparator) {
        super(papi, comparator);
        this.mat = mat;
    }

    @Override
    public boolean valid(ItemStack item, Player player) {
        return comparator.compareString(format(mat, player), item.getType().toString());
    }
}
