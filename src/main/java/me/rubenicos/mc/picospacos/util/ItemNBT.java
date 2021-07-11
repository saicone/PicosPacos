package me.rubenicos.mc.picospacos.util;

import net.minecraft.nbt.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;

/**
 * Item NBT utils for simple methods
 * @author Rubenicos
 * @version 0.2
 */
public class ItemNBT {

    final MethodHandles.Lookup lookup = MethodHandles.lookup();
    final Map<String, MethodHandle> methods = new HashMap<>();

    public ItemNBT() {
        try {
            Class<?> craft = Class.forName("org.bukkit.craftbukkit." + Instance.version + ".inventory.CraftItemStack");
            Class<?> stack = Class.forName("net.minecraft.server." + Instance.version + ".ItemStack");
            methods.put("asNMSCopy", lookup.findStatic(craft, "asNMSCopy", MethodType.methodType((Instance.verNumber >= 17 ? net.minecraft.world.item.ItemStack.class : stack), ItemStack.class)));
            methods.put("asCraftMirror", lookup.findStatic(craft, "asCraftMirror", MethodType.methodType(ItemStack.class, (Instance.verNumber >= 17 ? net.minecraft.world.item.ItemStack.class : stack))));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

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
            stack = (net.minecraft.world.item.ItemStack) methods.get("asNMSCopy").invoke(item);
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
            stack = (net.minecraft.world.item.ItemStack) methods.get("asNMSCopy").invoke(item);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if (stack != null && stack.getTag() != null) {
            NBTCompressedStreamTools.a(stack.getTag(), dataOutput);
        }
    }

    public ItemStack readNBT(DataInputStream dataInput) throws Throwable {
        NBTTagCompound compound = NBTCompressedStreamTools.a((DataInput) dataInput);
        return (ItemStack) methods.get("asCraftMirror").invoke(net.minecraft.world.item.ItemStack.a(compound));
    }

    // Class instance for old server versions
    private static final class ItemNBT$Old extends ItemNBT {

        private final Class<?> tagCompound;
        private final Class<?> tagList;

        public ItemNBT$Old() {
            super();
            Class<?> c1 = null;
            Class<?> c2 = null;
            try {
                c1 = Class.forName("net.minecraft.server." + Instance.version + ".NBTTagCompound");
                methods.put("get", lookup.findVirtual(c1, "get", MethodType.methodType(Class.forName("net.minecraft.server." + Instance.version + ".NBTBase"), String.class)));

                c2 = Class.forName("net.minecraft.server." + Instance.version + ".NBTTagList");
                methods.put("list", lookup.findGetter(c2, "list", List.class));

                Class<?> itemStack = Class.forName("net.minecraft.server." + Instance.version + ".ItemStack");
                methods.put("stack", lookup.findConstructor(itemStack, MethodType.methodType(void.class, c1)));
                methods.put("hasTag", lookup.findVirtual(itemStack, "hasTag", MethodType.methodType(Boolean.class)));
                methods.put("setTag", lookup.findVirtual(itemStack, "getTag", MethodType.methodType(c1)));

                Class<?> streamTools = Class.forName("net.minecraft.server." + Instance.version + ".NBTCompressedStreamTools");
                methods.put("write", lookup.findStatic(streamTools, "a", MethodType.methodType(void.class, c1, DataOutput.class)));
                methods.put("read", lookup.findStatic(streamTools, "a", MethodType.methodType(c1, (Instance.verNumber >= 13 ? DataInput.class : DataInputStream.class))));
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            tagCompound = c1;
            tagList = c2;
        }

        @Override
        public List<String> of(ItemStack item, String... path) {
            try {
                Object stack = methods.get("asNMSCopy").invoke(item);
                if (stack == null || methods.get("hasTag").invokeExact(stack).equals(false)) return Collections.emptyList();

                Object compound = methods.get("getTag").invokeExact(stack);
                for (String s : path) {
                    if (tagCompound.isInstance(compound)) {
                        compound = methods.get("get").invokeExact(stack, s);
                    } else if (tagList.isInstance(compound)) {
                        int i;
                        try {
                            i = Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            return Collections.emptyList();
                        }
                        compound = ((List<?>) methods.get("list").invokeExact(compound)).get(i);
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
                    ((List<?>) methods.get("list").invokeExact(compound)).forEach(base -> {
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
            Object stack = methods.get("asNMSCopy").invoke(item);

            if (stack == null) return;

            Object base = methods.get("getTag").invokeExact(stack);
            if (base != null) {
                methods.get("write").invoke(base, dataOutput);
            }
        }

        @Override
        public ItemStack readNBT(DataInputStream dataInput) throws Throwable {
            Object compound = methods.get("read").invoke(dataInput);
            return (ItemStack) methods.get("asCraftMirror").invoke(methods.get("stack").invoke(compound));
        }
    }

    // Static class to obtain current ItemNBT methods
    public static final class Instance {

        // Instance of ItemNBT depending on server version
        private static final ItemNBT instance;

        static final String version;
        static final int verNumber;

        public static ItemNBT get() {
            return instance;
        }

        static {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            verNumber = Integer.parseInt(version.split("_")[1]);

            if (verNumber >= 17) {
                instance = new ItemNBT();
            } else {
                instance = new ItemNBT$Old();
            }
        }
    }
}
