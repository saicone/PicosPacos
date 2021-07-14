package me.rubenicos.mc.picospacos.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ItemUtils {

    public static String itemArrayToBase64(ItemStack[] items) {
        String data = "";
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                if (item != null) {
                    dataOutput.writeObject(itemToBytes(item));
                } else {
                    dataOutput.writeObject(null);
                }
            }

            data = Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static ItemStack[] itemArrayFromBase64(String data) {
        ItemStack[] items = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data)); BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            ItemStack[] stacks = new ItemStack[dataInput.readInt()];

            for (int Index = 0; Index < stacks.length; Index++) {
                byte[] stack = (byte[]) dataInput.readObject();

                if (stack != null) {
                    stacks[Index] = itemFromBytes(stack);
                } else {
                    stacks[Index] = null;
                }
            }

            items = stacks;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return items;
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

    private static ItemStack itemFromBytes(byte[] data) {
        ItemStack item = null;
        try (DataInputStream dataIn = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(data))))) {
            item = ItemNBT.Instance.get().readNBT(dataIn);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return item;
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
