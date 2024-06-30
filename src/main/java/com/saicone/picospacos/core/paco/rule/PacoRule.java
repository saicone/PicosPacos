package com.saicone.picospacos.core.paco.rule;

import com.saicone.picospacos.PicosPacos;
import com.saicone.rtag.item.ItemObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class PacoRule {

    private final String id;
    private final List<RuleType> rules;
    private final List<TagType> tags;
    private final List<String> commands;

    public PacoRule(String id, List<RuleType> rules, List<TagType> tags, List<String> commands) {
        this.id = id;
        this.rules = rules;
        this.tags = tags;
        this.commands = commands.isEmpty() ? null : commands;
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
        if (commands != null) {
            final String itemStr = ItemObject.save(ItemObject.asNMSCopy(item)).toString();
            Bukkit.getScheduler().runTask(PicosPacos.get(), () -> {
                for (String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("{player}", player.getName())
                            .replace("{rule}", this.id)
                            .replace("{item}", itemStr)
                    );
                }
            });
        }
        return true;
    }
}
