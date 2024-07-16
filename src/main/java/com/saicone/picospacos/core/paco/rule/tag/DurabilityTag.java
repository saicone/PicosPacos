package com.saicone.picospacos.core.paco.rule.tag;

import com.saicone.picospacos.core.paco.rule.TagType;
import com.saicone.picospacos.util.TextUtils;
import com.saicone.types.Types;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class DurabilityTag extends TagType {
    private final Object[] d;

    public DurabilityTag(String durability, boolean papi, String comparator) {
        super(papi, comparator);
        d = TextUtils.rangeShort(durability);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean valid(ItemStack item, Player player) {
        return comparator.compareShort(item.getDurability(), d[0] instanceof String ? Types.SHORT.parse(format((String) d[0], player), (short) 0) : (short) d[0], d[1] instanceof String ? Types.SHORT.parse(format((String) d[1], player), (short) 0) : (short) d[1]);
    }
}
