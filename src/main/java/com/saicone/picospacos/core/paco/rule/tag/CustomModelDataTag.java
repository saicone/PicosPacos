package com.saicone.picospacos.core.paco.rule.tag;

import com.saicone.picospacos.core.paco.rule.TagType;
import com.saicone.picospacos.util.Strings;
import com.saicone.types.Types;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class CustomModelDataTag extends TagType {
    private final Object[] m;

    public CustomModelDataTag(String modeldata, boolean papi, String comparator) {
        super(papi, comparator);
        m = Strings.rangeInt(modeldata);
    }

    @SuppressWarnings("all")
    @Override
    public boolean valid(ItemStack item, Player player) {
        if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
            return comparator.compareInt(item.getItemMeta().getCustomModelData(), m[0] instanceof String ? Types.INTEGER.parse(format((String) m[0], player), 0) : (int) m[0], m[1] instanceof String ? Types.INTEGER.parse(format((String) m[1], player), 0) : (int) m[1]);
        } else {
            return false;
        }
    }
}
