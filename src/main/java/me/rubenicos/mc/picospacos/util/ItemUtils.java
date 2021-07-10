package me.rubenicos.mc.picospacos.util;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataConverterRegistry;
import net.minecraft.util.datafix.fixes.DataConverterTypes;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class ItemUtils {

    public String serialize(ItemStack it) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(out)))) {
            ItemNBT.Instance.get().writeNBT(it, dataOut);
            return Base64Coder.encodeLines(out.toByteArray());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    public ItemStack deserializeItem(byte[] data) {
        Preconditions.checkNotNull(data, "null cannot be deserialized");
        Preconditions.checkArgument(data.length > 0, "cannot deserialize nothing");

        try {
            NBTTagCompound compound = net.minecraft.nbt.NBTCompressedStreamTools.readNBT(
                    new ByteArrayInputStream(data)
            );
            int dataVersion = compound.getInt("DataVersion");

            Preconditions.checkArgument(dataVersion <= getDataVersion(), "Newer version! Server downgrades are not supported!");
            Dynamic<NBTBase> converted = DataConverterRegistry.getDataFixer().update(DataConverterTypes.ITEM_STACK, new Dynamic<NBTBase>(DynamicOpsNBT.a, compound), dataVersion, getDataVersion());
            return CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.fromCompound((NBTTagCompound) converted.getValue()));
        } catch (IOException ex) {
            com.destroystokyo.paper.util.SneakyThrow.sneaky(ex);
            throw new RuntimeException();
        }
    }

    public boolean nbtEquals(ItemStack item, String text, String[] path) {
        for (String s : ItemNBT.Instance.get().of(item, path)) {
            if (s.equals(text)) return true;
        }
        return false;
    }

    public boolean nbtEqualsIgnoreCase(ItemStack item, String text, String[] path) {
        for (String s : ItemNBT.Instance.get().of(item, path)) {
            if (s.equalsIgnoreCase(text)) return true;
        }
        return false;
    }

    public boolean nbtContains(ItemStack item, String text, String[] path) {
        for (String s : ItemNBT.Instance.get().of(item, path)) {
            if (s.contains(text)) return true;
        }
        return false;
    }

    public boolean nbtRegex(ItemStack item, String text, String[] path) {
        for (String s : ItemNBT.Instance.get().of(item, path)) {
            if (TextUtils.regexMatch(text, s)) return true;
        }
        return false;
    }
}
