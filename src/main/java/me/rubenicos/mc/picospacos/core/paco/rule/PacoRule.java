package me.rubenicos.mc.picospacos.core.paco.rule;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class PacoRule {

    private final String name;
    private final List<RuleType> rules;
    private final List<TagType> tags;

    public PacoRule(String name, List<RuleType> rules, List<TagType> tags) {
        this.name = name;
        this.rules = rules;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public boolean containsRule(RuleType rule) {
        return rules.contains(rule);
    }

    public boolean match(ItemStack item, Player player) {
        for (TagType tag : tags) {
            if (!tag.valid(item, player)) {
                return false;
            }
        }
        return true;
    }
}
