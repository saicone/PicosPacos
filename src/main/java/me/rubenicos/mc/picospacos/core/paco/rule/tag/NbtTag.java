package me.rubenicos.mc.picospacos.core.paco.rule.tag;

import me.rubenicos.mc.picospacos.core.paco.rule.TagType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class NbtTag extends TagType {
    private final Map<String[], Object> nbt;
    private final boolean all;

    public NbtTag(Map<String[], Object> nbt, boolean all, boolean papi, String comparator) {
        super(papi, comparator);
        this.nbt = nbt;
        this.all = all;
    }

    @Override
    public boolean valid(ItemStack item, Player player) {
        return false;
    }
}
