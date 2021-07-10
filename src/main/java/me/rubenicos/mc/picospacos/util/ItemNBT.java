package me.rubenicos.mc.picospacos.util;

import net.minecraft.nbt.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.DataOutput;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Item NBT utils for simple methods
 * @author Rubenicos
 * @version 0.2
 */
public abstract class ItemNBT {

    final MethodHandle asNMSCopy;

    public ItemNBT(MethodHandle method) {
        asNMSCopy = method;
    }

    /**
     * Get ItemStack NBTBase by provided path and convert into String. <br>
     * If NBTBase is instance of NBTListField only the fields who not are
     * compound or list be returned into a string list.
     * @param item ItemStack to view
     * @param path Tag path
     * @return List of item tag content
     */
    public abstract List<String> of(ItemStack item, String... path);

    public abstract void writeNBT(ItemStack item, DataOutput dataOutput) throws Throwable;

    // Class for +1.17 server, avoid reflection by using remapped classes
    private static final class a extends ItemNBT {

        public a(MethodHandle method) {
            super(method);
        }

        @Override
        public List<String> of(ItemStack item, String... path) {
            net.minecraft.world.item.ItemStack stack = null;
            try {
                stack = (net.minecraft.world.item.ItemStack) asNMSCopy.invoke(item);
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

        @Override
        public void writeNBT(ItemStack item, DataOutput dataOutput) throws Throwable {
            net.minecraft.world.item.ItemStack stack = null;
            try {
                stack = (net.minecraft.world.item.ItemStack) asNMSCopy.invoke(item);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            if (stack != null && stack.getTag() != null) {
                NBTCompressedStreamTools.a(stack.getTag(), dataOutput);
            }
        }
    }

    // Class for old server versions
    private static final class b extends ItemNBT {

        private final Class<?> tagCompound;
        private final Class<?> tagList;
        private final MethodHandle hasTag;
        private final MethodHandle getTag;
        private final MethodHandle get;
        private final MethodHandle listField;
        private final MethodHandle write;

        public b(MethodHandle method, MethodHandles.Lookup lookup, String version) {
            super(method);
            Class<?> c1 = null;
            Class<?> c2 = null;
            MethodHandle m1 = null;
            MethodHandle m2 = null;
            MethodHandle m3 = null;
            MethodHandle m4 = null;
            MethodHandle m5 = null;
            try {
                c1 = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
                c2 = Class.forName("net.minecraft.server." + version + ".NBTTagList");
                Class<?> itemStack = Class.forName("net.minecraft.server." + version + ".ItemStack");
                m1 = lookup.findVirtual(itemStack, "hasTag", MethodType.methodType(Boolean.class));
                m2 = lookup.findVirtual(itemStack, "getTag", MethodType.methodType(c1));
                Class<?> nbtBase = Class.forName("net.minecraft.server." + version + ".NBTBase");
                m3 = lookup.findVirtual(c1, "get", MethodType.methodType(nbtBase, String.class));
                m4 = lookup.findGetter(c2, "list", List.class);
                m5 = lookup.findStatic(Class.forName("net.minecraft.server." + version + ".NBTCompressedStreamTools"), "a", MethodType.methodType(void.class, nbtBase, DataOutput.class));
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            tagCompound = c1;
            tagList = c2;
            hasTag = m1;
            getTag = m2;
            get = m3;
            listField = m4;
            write = m5;
        }

        @Override
        public List<String> of(ItemStack item, String... path) {
            try {
                Object stack = asNMSCopy.invoke(item);
                if (stack == null || hasTag.invokeExact(stack).equals(false)) return Collections.emptyList();

                Object compound = getTag.invokeExact(stack);
                for (String s : path) {
                    if (tagCompound.isInstance(compound)) {
                        compound = get.invokeExact(stack, s);
                    } else if (tagList.isInstance(compound)) {
                        int i;
                        try {
                            i = Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            return Collections.emptyList();
                        }
                        compound = ((List<?>) listField.invokeExact(compound)).get(i);
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
                    ((List<?>) listField.invokeExact(compound)).forEach(base -> {
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
            Object stack = asNMSCopy.invoke(item);

            if (stack == null) return;

            Object base = getTag.invokeExact(stack);
            if (base != null) {
                write.invoke(base, dataOutput);
            }
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
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            int verNumber = Integer.parseInt(version.split("_")[1]);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle method = null;
            try {
                method = lookup.findStatic(Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack"), "asNMSCopy", MethodType.methodType((verNumber >= 17 ? net.minecraft.world.item.ItemStack.class : Class.forName("net.minecraft.server." + version + ".ItemStack")), ItemStack.class));
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            if (verNumber >= 17) {
                instance = new a(method);
            } else {
                instance = new b(method, lookup, version);
            }
        }
    }
}
