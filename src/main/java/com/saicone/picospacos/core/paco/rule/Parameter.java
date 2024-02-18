package com.saicone.picospacos.core.paco.rule;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.core.paco.rule.comparator.*;
import com.saicone.picospacos.core.paco.rule.tag.*;
import me.rubenicos.mc.picospacos.core.paco.rule.comparator.*;
import me.rubenicos.mc.picospacos.core.paco.rule.tag.*;
import com.saicone.picospacos.module.Settings;
import com.saicone.picospacos.util.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parameter {
    private static final Map<String, ComparatorType> comparators = Map.of(
            "equal", new EqualComparator(),
            "contains", new ContainsComparator(),
            "regex", new RegexComparator(),
            "more", new MoreComparator(),
            "less", new LessComparator(),
            "beetween", new BetweenComparator()
    );

    public static ComparatorType comparatorOf(String name) {
        return comparators.getOrDefault(name, comparators.get("equal"));
    }

    public static TagType tagOf(Settings settings, String path, String name) {
        String string = "<" + PicosPacos.getSettings().getString("Hook.PlayerholderAPI.rule", "allowPapi") + ">";
        boolean papi = name.contains(string);
        String[] split = name.replace(string, "").split(":");
        String comparator = split.length > 1 ? split[1].toLowerCase() : "equal";
        switch (split[0].toLowerCase()) {
            case "material":
            case "mat":
                return new MaterialTag(settings.getString(path), papi, comparator);
            case "durability":
                return new DurabilityTag(settings.getString(path), papi, comparator);
            case "amount":
            case "size":
                return new AmountTag(settings.getString(path), papi, comparator);
            case "name":
            case "displayname":
                return new NameTag(settings.getString(path), papi, comparator);
            case "lore":
            case "description":
                return new LoreTag(settings.getStringList(path), split[split.length - 1].equalsIgnoreCase("all"), papi, comparator);
            case "custommodeldata":
            case "modeldata":
                return new CustomModelDataTag(settings.getString(path), papi, comparator);
            case "enchantments":
            case "enchants":
                Map<String, Object[]> enchants = new HashMap<>();
                settings.getStringList(path).forEach(enchant -> {
                    String[] s = enchant.split("=", 2);
                    if (s.length == 2) {
                        enchants.put(s[0], TextUtils.rangeInt(s[1]));
                    }
                });
                if (enchants.isEmpty()) {
                    return null;
                }
                boolean all = split[split.length - 1].equalsIgnoreCase("all");
                return new EnchantmentsTag(enchants, all, papi, comparator, comparatorOf(split.length > 2 ? (all ? (split.length > 3 ? split[3] : "equal") : split[2]) : "equal"));
            case "flags":
                return new FlagsTag(settings.getStringList(path), split[split.length - 1].equalsIgnoreCase("all"), papi, comparator);
            case "nbt":
                return new NbtTag(null, split[split.length - 1].equalsIgnoreCase("all"), papi, comparator);
            default:
                return null;
        }
    }

    public static PacoRule ruleOf(Settings settings, String id, List<RuleType> rules) {
        List<TagType> tags = new ArrayList<>();
        settings.getKeys(id).forEach(key -> {
            TagType tag = tagOf(settings, id + "." + key, key);
            if (tag != null) {
                tags.add(tag);
            }
        });
        if (!tags.isEmpty()) {
            return new PacoRule(id, rules, tags);
        } else {
            return null;
        }
    }
}
