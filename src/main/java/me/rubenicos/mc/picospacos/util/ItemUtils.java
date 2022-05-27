package me.rubenicos.mc.picospacos.util;

import com.saicone.rtag.RtagItem;
import com.saicone.rtag.item.ItemTagStream;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemUtils {

    public static boolean itemExists(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    public static String itemsToBase64(List<ItemStack> items) {
        return ItemTagStream.INSTANCE.listToBase64(items);
    }

    public static List<ItemStack> itemsFromBase64(String data) {
        return ItemTagStream.INSTANCE.listFromBase64(data);
    }

    public static List<String> flagsToList(Set<ItemFlag> flags) {
        List<String> list = new ArrayList<>();
        flags.forEach(flag -> list.add(flag.name()));
        return list;
    }

    public static Set<Map.Entry<String, Integer>> enchantsToSet(Map<Enchantment, Integer> enchants) {
        Map<String, Integer> map = new HashMap<>();
        enchants.forEach((enchant, level) -> map.put(enchant.toString(), level));
        return map.entrySet();
    }

    public static boolean nbtEquals(ItemStack item, String text, Object... path) {
        RtagItem tag = new RtagItem(item);
        String s = tag.get(path);
        return Objects.equals(s, text);
    }

    public static boolean nbtEqualsIgnoreCase(ItemStack item, String text, Object... path) {
        RtagItem tag = new RtagItem(item);
        String s = tag.get(path);
        return s != null && s.equalsIgnoreCase(text);
    }

    public static boolean nbtContains(ItemStack item, String text, Object... path) {
        RtagItem tag = new RtagItem(item);
        String s = tag.get(path);
        return s != null && s.contains(text);
    }

    public static boolean nbtRegex(ItemStack item, String text, String[] path) {
        RtagItem tag = new RtagItem(item);
        String s = tag.get(path);
        return s != null && TextUtils.regexMatch(text, s);
    }
}
