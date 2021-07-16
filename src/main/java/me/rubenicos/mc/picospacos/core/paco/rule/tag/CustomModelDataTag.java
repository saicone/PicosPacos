package me.rubenicos.mc.picospacos.core.paco.rule.tag;

import me.rubenicos.mc.picospacos.core.paco.rule.TagType;
import me.rubenicos.mc.picospacos.util.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class CustomModelDataTag extends TagType {
    private final Object[] m;

    public CustomModelDataTag(String modeldata, boolean papi, String comparator) {
        super(papi, comparator);
        m = TextUtils.rangeInt(modeldata);
    }

    @SuppressWarnings("all")
    @Override
    public boolean valid(ItemStack item, Player player) {
        if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
            return comparator.compareInt(item.getItemMeta().getCustomModelData(), m[0] instanceof String ? TextUtils.asInt(format((String) m[0], player), 0) : (int) m[0], m[1] instanceof String ? TextUtils.asInt(format((String) m[1], player), 0) : (int) m[1]);
        } else {
            return false;
        }
    }
}
