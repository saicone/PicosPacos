package me.rubenicos.mc.picospacos.core.paco.rule;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.Settings;
import me.rubenicos.mc.picospacos.util.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public abstract class TagType {

    public static TagType of(Settings settings, String key, String name) {
        String string = "<" + PicosPacos.SETTINGS.getString("Hook.PlayerholderAPI.rule", "allowPapi") + ">";
        boolean allowpapi = name.contains("<" + PicosPacos.SETTINGS.getString("Hook.PlayerholderAPI.rule", "allowPapi") + ">");
        String[] split = name.replace(string, "").split(":");
        switch (split[0].toLowerCase()) {
            case "material":
            case "mat":
                return new Tag$Material(settings.getString(key + "." + name), allowpapi, (split.length > 1 ? split[1] : "equal"));
            case "amount":
            case "size":
                return new Tag$Amount(settings.getString(key + "." + name), allowpapi, (split.length > 1 ? split[1] : "equal"));
            case "name":
            case "displayname":
                return new Tag$Name();
            case "lore":
            case "description":
                return new Tag$Lore();
            case "enchantments":
            case "enchants":
                return new Tag$Enchantments();
            case "custommodeldata":
            case "modeldata":
                return new Tag$customModelData();
            case "nbt":
                return new Tag$Nbt();
            default:
                return null;
        }
    }

    boolean papi;
    ComparatorType comparator;

    public TagType(boolean papi, String comparator) {
        this.papi = papi;
        this.comparator = ComparatorType.of(comparator);
    }

    String format(String s, Player p) {
        if (papi) {
            return Locale.parsePlaceholders(p, s);
        } else {
            return s;
        }
    }

    public abstract boolean valid(ItemStack item, Player player);

    private static final class Tag$Material extends TagType {
        private final String mat;

        public Tag$Material(String mat, boolean papi, String comparator) {
            super(papi, comparator);
            this.mat = mat;
        }

        @Override
        public boolean valid(ItemStack item, Player player) {
            return comparator.compare(format(mat, player), item.getType().toString());
        }
    }

    private static final class Tag$Amount extends TagType {
        private final Object d1;
        private final Object d2;

        public Tag$Amount(String amount, boolean papi, String comparator) {
            super(papi, comparator);
            String[] s = amount.split("\\|");
            Object d1;
            Object d2;
            try {
                d1 = Double.parseDouble(s[0]);
            } catch (NullPointerException | NumberFormatException e) {
                d1 = s[0];
            }
            try {
                d2 = Double.parseDouble(s.length > 1 ? s[1] : "0");
            } catch (NullPointerException | NumberFormatException e) {
                d2 = s[0];
            }
            this.d1 = d1;
            this.d2 = d2;
        }

        @Override
        public boolean valid(ItemStack item, Player player) {
            return comparator.compareDouble(item.getAmount(), d1 instanceof String ? TextUtils.asDouble(format((String) d1, player), 0) : (Double) d1, d2 instanceof String ? TextUtils.asDouble(format((String) d2, player), 0) : (Double) d2);
        }
    }

    private static final class Tag$Name extends TagType {

    }

    private static final class Tag$Lore extends TagType {

    }

    private static final class Tag$Enchantments extends TagType {

    }

    private static final class Tag$customModelData extends TagType {

    }

    private static final class Tag$Nbt extends TagType {

    }
}
