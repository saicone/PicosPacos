package me.rubenicos.mc.picospacos.core.paco.rule;

import com.google.common.collect.Multimap;
import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.Settings;
import me.rubenicos.mc.picospacos.util.ItemUtils;
import me.rubenicos.mc.picospacos.util.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TagType {

    public static TagType of(Settings settings, String key, String name) {
        String string = "<" + PicosPacos.SETTINGS.getString("Hook.PlayerholderAPI.rule", "allowPapi") + ">";
        boolean allowpapi = name.contains("<" + PicosPacos.SETTINGS.getString("Hook.PlayerholderAPI.rule", "allowPapi") + ">");
        String[] split = name.replace(string, "").split(":");
        switch (split[0].toLowerCase()) {
            case "material":
            case "mat":
                return new Tag$Material(settings.getString(key + "." + name), allowpapi, (split.length > 1 ? split[1] : "equal"));
            case "durability":
                return new Tag$Durability();
            case "amount":
            case "size":
                return new Tag$Amount(settings.getString(key + "." + name), allowpapi, (split.length > 1 ? split[1] : "equal"));
            case "name":
            case "displayname":
                return new Tag$Name();
            case "lore":
            case "description":
                return new Tag$Lore();
            case "custommodeldata":
            case "modeldata":
                return new Tag$customModelData();
            case "enchantments":
            case "enchants":
                return new Tag$Enchantments();
            case "flags":
                return new Tag$Flags();
            case "attributemodifier":
            case "attribute":
                return new Tag$Attribute();
            case "nbt":
                return new Tag$Nbt();
            default:
                return null;
        }
    }

    private final boolean color;
    private final boolean papi;
    ComparatorType comparator;

    public TagType(boolean color, boolean papi, String comparator) {
        this.color = color;
        this.papi = papi;
        this.comparator = ComparatorType.of(comparator);
    }

    String format(String s, Player p) {
        if (papi) {
            return Locale.parsePlaceholders(p, s, color);
        } else if (color) {
            return Locale.color(s);
        } else {
            return s;
        }
    }

    List<String> format(List<String> l, Player p) {
        if (papi) {
            return Locale.parsePlaceholders(p, l, color);
        } else if (color) {
            return Locale.color(l);
        } else {
            return l;
        }
    }

    public abstract boolean valid(ItemStack item, Player player);

    private static final class Tag$Material extends TagType {
        private final String mat;

        public Tag$Material(String mat, boolean color, boolean papi, String comparator) {
            super(color, papi, comparator);
            this.mat = mat;
        }

        @Override
        public boolean valid(ItemStack item, Player player) {
            return comparator.compareString(format(mat, player), item.getType().toString());
        }
    }

    private static final class Tag$Durability extends TagType {
        private final Object[] d;

        public Tag$Durability(String durability, boolean color, boolean papi, String comparator) {
            super(color, papi, comparator);
            d = TextUtils.rangeShort(durability);
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean valid(ItemStack item, Player player) {
            return comparator.compareShort(item.getDurability(), d[0] instanceof String ? TextUtils.asShort(format((String) d[0], player), (short) 0) : (short) d[0], d[1] instanceof String ? TextUtils.asShort(format((String) d[1], player), (short) 0) : (short) d[1]);
        }
    }

    private static final class Tag$Amount extends TagType {
        private final Object[] a;

        public Tag$Amount(String amount, boolean color, boolean papi, String comparator) {
            super(color, papi, comparator);
            a = TextUtils.rangeInt(amount);
        }

        @Override
        public boolean valid(ItemStack item, Player player) {
            return comparator.compareInt(item.getAmount(), a[0] instanceof String ? TextUtils.asInt(format((String) a[0], player), 0) : (int) a[0], a[1] instanceof String ? TextUtils.asInt(format((String) a[1], player), 0) : (int) a[1]);
        }
    }

    private static final class Tag$Name extends TagType {
        private final String name;

        public Tag$Name(String name, boolean color, boolean papi, String comparator) {
            super(color, papi, comparator);
            this.name = Locale.color(name);
        }

        @SuppressWarnings("all")
        @Override
        public boolean valid(ItemStack item, Player player) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                return comparator.compareString(format(name, player), item.getItemMeta().getDisplayName());
            } else {
                return false;
            }
        }
    }

    private static final class Tag$Lore extends TagType {
        private final List<String> lore;
        private final boolean all;

        public Tag$Lore(List<String> lore, boolean color, boolean all, boolean papi, String comparator) {
            super(color, papi, comparator);
            this.lore = Locale.color(lore);
            this.all = all;
        }

        @SuppressWarnings("all")
        @Override
        public boolean valid(ItemStack item, Player player) {
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                return (all ? comparator.compareListAll(format(lore, player), item.getItemMeta().getLore()) : comparator.compareList(format(lore, player), item.getItemMeta().getLore()));
            }
            return false;
        }
    }

    private static final class Tag$customModelData extends TagType {
        private final Object[] m;

        public Tag$customModelData(String modeldata, boolean color, boolean papi, String comparator) {
            super(color, papi, comparator);
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

    private static final class Tag$Enchantments extends TagType {
        private final Set<Map.Entry<String, Object[]>> enchantments;
        private final boolean all;
        private final ComparatorType lvlComparator;

        public Tag$Enchantments(Map<String, Object[]> enchantments, boolean color, boolean all, boolean papi, String comparator, ComparatorType lvlComparator) {
            super(color, papi, comparator);
            this.enchantments = enchantments.entrySet();
            this.all = all;
            this.lvlComparator = lvlComparator;
        }

        @Override
        public boolean valid(ItemStack item, Player player) {
            if (!item.getEnchantments().isEmpty()) {
                Set<Map.Entry<String, Integer>> enchants = ItemUtils.enchantsToSet(item.getEnchantments());
                for (Map.Entry<String, Integer> enchant : enchants) {
                    if (valid(enchant.getKey(), enchant.getValue(), player) && !all) {
                        return true;
                    } else if (all) {
                        return false;
                    }
                }
            }
            return false;
        }

        private boolean valid(String enchant, int level, Player player) {
            for (Map.Entry<String, Object[]> entry : enchantments) {
                if (comparator.compareString(format(entry.getKey(), player), enchant) &&
                    lvlComparator.compareInt(level, entry.getValue()[0] instanceof String ? TextUtils.asInt(format((String) entry.getValue()[0], player), 0) : (int) entry.getValue()[0], entry.getValue()[1] instanceof String ? TextUtils.asInt(format((String) entry.getValue()[1], player), 0) : (int) entry.getValue()[1])) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class Tag$Flags extends TagType {
        private final List<String> flags;
        private final boolean all;

        public Tag$Flags(List<String> flags, boolean all, boolean color, boolean papi, String comparator) {
            super(color, papi, comparator);
            this.flags = Locale.color(flags);
            this.all = all;
        }

        @SuppressWarnings("all")
        @Override
            public boolean valid(ItemStack item, Player player) {
            if (item.hasItemMeta() && !item.getItemMeta().getItemFlags().isEmpty()) {
                return (all ? comparator.compareListAll(format(flags, player), ItemUtils.flagsToList(item.getItemMeta().getItemFlags())) : comparator.compareList(format(flags, player), ItemUtils.flagsToList(item.getItemMeta().getItemFlags())));
            }
            return false;
        }
    }

    private static final class Tag$Attribute extends TagType {
        private final Multimap<String, String> attributes;

        public Tag$Attribute(Multimap<String, String> attributes, boolean color, boolean papi, String comparator) {
            super(color, papi, comparator);
            this.attributes = attributes;
        }

        @SuppressWarnings("all")
        @Override
        public boolean valid(ItemStack item, Player player) {
            if (item.hasItemMeta() && item.getItemMeta().hasAttributeModifiers()) {

            }
            return false;
        }
    }

    private static final class Tag$Nbt extends TagType {

    }
}
