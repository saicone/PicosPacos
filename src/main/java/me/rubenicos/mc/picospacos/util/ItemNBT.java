package me.rubenicos.mc.picospacos.util;

import net.minecraft.nbt.*;
import org.bukkit.inventory.ItemStack;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.util.*;

/**
 * Item NBT utils for simple methods
 * @author Rubenicos
 * @version 0.2
 */
public class ItemNBT {

    /**
     * Get ItemStack NBTBase by provided path and convert into String. <br>
     * If NBTBase is instance of NBTListField only the fields who not are
     * compound or list be returned into a string list.
     * @param item ItemStack to view
     * @param path Tag path
     * @return List of item tag content
     */
    public List<String> of(ItemStack item, String... path) {
        net.minecraft.world.item.ItemStack stack = null;
        try {
            stack = (net.minecraft.world.item.ItemStack) LookupUtils.get("asNMSCopy").invoke(item);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if (stack == null || !stack.hasTag()) return Collections.emptyList();

        Object compound = stack.getTag();
        for (String s : path) {
            if (compound instanceof NBTTagCompound) {
                compound = ((NBTTagCompound) compound).get(s);
            } else if (compound instanceof NBTTagList) {
                int i;
                try {
                    i = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return Collections.emptyList();
                }
                compound = ((NBTTagList) compound).get(i);
            } else {
                if (compound == null) {
                    return Collections.emptyList();
                } else {
                    return Collections.singletonList(compound.toString());
                }
            }
        }
        if (compound instanceof NBTTagList) {
            List<String> list = new ArrayList<>();
            int i = 0;
            while (i <= ((NBTTagList) compound).size()) {
                NBTBase base = ((NBTTagList) compound).get(i);
                if (!(base instanceof NBTTagCompound) && !(base instanceof NBTTagList)) {
                    list.add(base.toString());
                }
                i++;
            }
            return list;
        }
        return Collections.emptyList();
    }

    public void writeNBT(ItemStack item, DataOutput dataOutput) throws Throwable {
        net.minecraft.world.item.ItemStack stack = null;
        try {
            stack = (net.minecraft.world.item.ItemStack) LookupUtils.get("asNMSCopy").invoke(item);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if (stack != null && stack.getTag() != null) {
            NBTCompressedStreamTools.a(stack.getTag(), dataOutput);
        }
    }

    public ItemStack readNBT(DataInputStream dataInput) throws Throwable {
        NBTTagCompound compound = NBTCompressedStreamTools.a((DataInput) dataInput);
        return (ItemStack) LookupUtils.get("asBukkitCopy").invoke(net.minecraft.world.item.ItemStack.a(compound));
    }

    // Class instance for old server versions
    private static final class ItemNBT$Old extends ItemNBT {

        private final Class<?> tagCompound;
        private final Class<?> tagList;

        public ItemNBT$Old() {
            Class<?> c1 = null;
            Class<?> c2 = null;
            try {
                c1 = Class.forName("net.minecraft.server." + Server.version + ".NBTTagCompound");
                LookupUtils.addMethod("get", c1, "get", Class.forName("net.minecraft.server." + Server.version + ".NBTBase"), String.class);

                c2 = Class.forName("net.minecraft.server." + Server.version + ".NBTTagList");
                LookupUtils.addField("list", c2, "list");

                Class<?> itemStack = Class.forName("net.minecraft.server." + Server.version + ".ItemStack");
                LookupUtils.addConstructor("stack", itemStack, void.class, c1);
                LookupUtils.addMethod("hasTag", itemStack, "hasTag", boolean.class);
                LookupUtils.addMethod("getTag", itemStack, "getTag", c1);

                Class<?> streamTools = Class.forName("net.minecraft.server." + Server.version + ".NBTCompressedStreamTools");
                LookupUtils.addStaticMethod("write", streamTools, "a", void.class, c1, DataOutput.class);
                LookupUtils.addStaticMethod("read", streamTools, "a", c1, Server.verNumber >= 13 ? DataInput.class : DataInputStream.class);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            tagCompound = c1;
            tagList = c2;
        }

        @Override
        public List<String> of(ItemStack item, String... path) {
            try {
                Object stack = LookupUtils.get("asNMSCopy").invoke(item);
                if (stack == null || LookupUtils.get("hasTag").invokeExact(stack).equals(false)) return Collections.emptyList();

                Object compound = LookupUtils.get("getTag").invokeExact(stack);
                for (String s : path) {
                    if (tagCompound.isInstance(compound)) {
                        compound = LookupUtils.get("get").invokeExact(stack, s);
                    } else if (tagList.isInstance(compound)) {
                        int i;
                        try {
                            i = Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            return Collections.emptyList();
                        }
                        compound = ((List<?>) LookupUtils.get("list").invokeExact(compound)).get(i);
                    } else {
                        if (compound == null) {
                            return Collections.emptyList();
                        } else {
                            return Collections.singletonList(compound.toString());
                        }
                    }
                }
                if (tagList.isInstance(compound)) {
                    List<String> list = new ArrayList<>();
                    ((List<?>) LookupUtils.get("list").invokeExact(compound)).forEach(base -> {
                        if (!tagCompound.isInstance(base) && !tagList.isInstance(base)) {
                            list.add(base.toString());
                        }
                    });
                    return list;
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return Collections.emptyList();
        }

        @Override
        public void writeNBT(ItemStack item, DataOutput dataOutput) throws Throwable {
            Object stack = LookupUtils.get("asNMSCopy").invoke(item);

            if (stack == null) return;

            Object base = LookupUtils.get("getTag").invokeExact(stack);
            if (base != null) {
                LookupUtils.get("write").invoke(base, dataOutput);
            }
        }

        @Override
        public ItemStack readNBT(DataInputStream dataInput) throws Throwable {
            Object compound = LookupUtils.get("read").invoke(dataInput);
            return (ItemStack) LookupUtils.get("asBukkitCopy").invoke(LookupUtils.get("stack").invoke(compound));
        }
    }

    // Static class to obtain current ItemNBT methods
    public static final class Instance {

        // Instance of ItemNBT depending on server version
        private static final ItemNBT instance;

        public static ItemNBT get() {
            return instance;
        }

        static {
            if (Server.verNumber >= 17) {
                instance = new ItemNBT();
            } else {
                instance = new ItemNBT$Old();
            }
        }
    }
}
