package com.saicone.picospacos.core.paco.rule;

import com.saicone.picospacos.module.Locale;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class TagType {

    private final boolean papi;
    protected ComparatorType comparator;

    public TagType(boolean papi, String comparator) {
        this.papi = papi;
        this.comparator = Parameter.comparatorOf(comparator);
    }

    protected String format(String s, Player p) {
        if (papi) {
            return Locale.parsePlaceholders(p, s);
        } else {
            return s;
        }
    }

    protected List<String> format(List<String> l, Player p) {
        if (papi) {
            return Locale.parsePlaceholders(p, l);
        } else {
            return l;
        }
    }

    public abstract boolean valid(ItemStack item, Player player);
}
