package me.rubenicos.mc.picospacos.util;

import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LookupUtils {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final Map<String, MethodHandle> methods = new HashMap<>();

    static {
        try {
            Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit." + Server.version + ".entity.CraftPlayer");
            Class<?> craft = Class.forName("org.bukkit.craftbukkit." + Server.version + ".inventory.CraftItemStack");
            Class<?> stack;
            if (Server.verNumber >= 17) {
                addConstructor("chatComponent", ChatComponentText.class, void.class, String.class);
                addConstructor("chatPacket", PacketPlayOutChat.class, void.class, IChatBaseComponent.class, ChatMessageType.class, UUID.class);
                addStaticMethod("chatType", ChatMessageType.class, "a", ChatMessageType.class, byte.class);

                addMethod("playerHandle", craftPlayer, "getHandle", EntityPlayer.class);
                addField("playerConnection", EntityPlayer.class, "b");
                addMethod("sendPacket", PlayerConnection.class, "sendPacket", void.class, Packet.class);

                stack = net.minecraft.world.item.ItemStack.class;
            } else {
                addConstructor("chatComponent", Class.forName("net.minecraft.server." + Server.version + ".ChatComponentText"), void.class, String.class);
                Class<?> chatMessageType = Class.forName("net.minecraft.server." + Server.version + ".ChatMessageType");
                Class<?> packetPlayOutChat = Class.forName("net.minecraft.server." + Server.version + ".PacketPlayOutChat");
                if (Server.verNumber < 14) {
                    addConstructor("chatPacket", packetPlayOutChat, void.class, Class.forName("net.minecraft.server." + Server.version + ".IChatBaseComponent"), chatMessageType);
                } else {
                    addConstructor("chatPacket", packetPlayOutChat, void.class, Class.forName("net.minecraft.server." + Server.version + ".IChatBaseComponent"), chatMessageType, UUID.class);
                }
                addStaticMethod("chatType", chatMessageType, "a", chatMessageType, byte.class);

                Class<?> entityPlayer = Class.forName("net.minecraft.server." + Server.version + ".EntityPlayer");
                addMethod("playerHandle", craftPlayer, "getHandle", entityPlayer);
                addField("playerConnection", entityPlayer, "playerConnection");
                addMethod("sendPacket", Class.forName("net.minecraft.server." + Server.version + ".PlayerConnection"), "sendPacket", void.class, Class.forName("net.minecraft.server." + Server.version + ".Packet"));

                stack = Class.forName("net.minecraft.server." + Server.version + ".ItemStack");
            }
            addStaticMethod("asNMSCopy", craft, "asNMSCopy", stack, ItemStack.class);
            addStaticMethod("asBukkitCopy", craft, "asBukkitCopy", ItemStack.class, stack);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static MethodHandle get(String name) {
        return methods.get(name);
    }

    public static void addMethod(String name, Class<?> clazz, String method, Class<?>... classes) throws NoSuchMethodException, IllegalAccessException {
        methods.put(name, lookup.findVirtual(clazz, method, typeFrom(classes)));
    }

    public static void addStaticMethod(String name, Class<?> clazz, String method, Class<?>... classes) throws NoSuchMethodException, IllegalAccessException {
        methods.put(name, lookup.findStatic(clazz, method, typeFrom(classes)));
    }

    public static void addField(String name, Class<?> clazz, String field) throws NoSuchFieldException, IllegalAccessException {
        methods.put(name, lookup.unreflectGetter(getField(clazz, field)));
    }

    public static Field getField(Class<?> clazz, String field) throws NoSuchFieldException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return f;
    }

    public static void addConstructor(String name, Class<?> clazz, Class<?>... classes) throws NoSuchMethodException, IllegalAccessException {
        methods.put(name, lookup.unreflectConstructor(getConstructor(clazz, classes)));
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... classes) throws NoSuchMethodException {
        Constructor<?> c = clazz.getDeclaredConstructor(classes);
        c.setAccessible(true);
        return c;
    }

    private static MethodType typeFrom(Class<?>... classes) {
        return classes.length > 2 ? MethodType.methodType(classes[0], classes[1], Arrays.copyOfRange(classes, 2, classes.length)) : classes.length > 1 ? MethodType.methodType(classes[0], classes[1]) : MethodType.methodType(classes[0]);
    }
}
