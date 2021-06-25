package me.rubenicos.mc.picospacos.util;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataConverterRegistry;
import net.minecraft.util.datafix.fixes.DataConverterTypes;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class ItemUtils {

    public byte[] serializeItem(ItemStack item) {
        Preconditions.checkNotNull(item, "null cannot be serialized");
        Preconditions.checkArgument(item.getType() != Material.AIR, "air cannot be serialized");

        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        NBTTagCompound compound = (item instanceof CraftItemStack ? ((CraftItemStack) item).getHandle() : CraftItemStack.asNMSCopy(item)).save(new NBTTagCompound());
        compound.setInt("DataVersion", getDataVersion());
        try {
            net.minecraft.nbt.NBTCompressedStreamTools.writeNBT(
                    compound,
                    outputStream
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return outputStream.toByteArray();
    }


    public ItemStack deserializeItem(byte[] data) {
        Preconditions.checkNotNull(data, "null cannot be deserialized");
        Preconditions.checkArgument(data.length > 0, "cannot deserialize nothing");

        try {
            NBTTagCompound compound = net.minecraft.nbt.NBTCompressedStreamTools.readNBT(
                    new java.io.ByteArrayInputStream(data)
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
}
