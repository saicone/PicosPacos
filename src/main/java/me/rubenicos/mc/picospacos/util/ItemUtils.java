package me.rubenicos.mc.picospacos.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ItemUtils {

    public static boolean itemExists(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    public static String itemsToBase64(List<ItemStack> items) {
        items = items.stream().filter(ItemUtils::itemExists).collect(Collectors.toList());
        String data = "";
        if (items.isEmpty()) return data;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(items.size());
            for (ItemStack item : items) {
                dataOutput.writeObject(itemToBytes(item));
            }
            data = Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private static byte[] itemToBytes(ItemStack it) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(out)))) {
            ItemNBT.Instance.get().writeNBT(it, dataOut);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static List<ItemStack> itemsFromBase64(String data) {
        List<ItemStack> items = new ArrayList<>();
        if (data.isBlank()) return items;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data)); BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            int size = dataInput.readInt();
            for (int i = 0; i < size; i++) {
                byte[] stack = (byte[]) dataInput.readObject();
                if (stack != null) {
                    ItemStack item = itemFromBytes(stack);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    private static ItemStack itemFromBytes(byte[] data) {
        ItemStack item = null;
        try (DataInputStream dataIn = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(data))))) {
            item = ItemNBT.Instance.get().readNBT(dataIn);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return item;
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

    public static boolean nbtEquals(ItemStack item, String text, String[] path) {
        for (String s : ItemNBT.Instance.get().of(item, path)) {
            if (s.equals(text)) return true;
        }
        return false;
    }

    public static boolean nbtEqualsIgnoreCase(ItemStack item, String text, String[] path) {
        for (String s : ItemNBT.Instance.get().of(item, path)) {
            if (s.equalsIgnoreCase(text)) return true;
        }
        return false;
    }

    public static boolean nbtContains(ItemStack item, String text, String[] path) {
        for (String s : ItemNBT.Instance.get().of(item, path)) {
            if (s.contains(text)) return true;
        }
        return false;
    }

    public static boolean nbtRegex(ItemStack item, String text, String[] path) {
        for (String s : ItemNBT.Instance.get().of(item, path)) {
            if (TextUtils.regexMatch(text, s)) return true;
        }
        return false;
    }
}
