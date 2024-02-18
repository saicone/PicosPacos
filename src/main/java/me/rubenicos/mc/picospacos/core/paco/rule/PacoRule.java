package me.rubenicos.mc.picospacos.core.paco.rule;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class PacoRule {

    private final String id;
    private final List<RuleType> rules;
    private final List<TagType> tags;

    public PacoRule(String id, List<RuleType> rules, List<TagType> tags) {
        this.id = id;
        this.rules = rules;
        this.tags = tags;
    }

    public String getId() {
        return id;
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
