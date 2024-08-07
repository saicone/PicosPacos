package com.saicone.picospacos.core.paco.rule.tag;

import com.saicone.picospacos.core.paco.rule.TagType;
import com.saicone.picospacos.util.Strings;
import com.saicone.types.Types;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class AmountTag extends TagType {
    private final Object[] a;

    public AmountTag(String amount, boolean papi, String comparator) {
        super(papi, comparator);
        a = Strings.rangeInt(amount);
    }

    @Override
    public boolean valid(ItemStack item, Player player) {
        return comparator.compareInt(item.getAmount(), a[0] instanceof String ? Types.INTEGER.parse(format((String) a[0], player), 0) : (int) a[0], a[1] instanceof String ? Types.INTEGER.parse(format((String) a[1], player), 0) : (int) a[1]);
    }
}
