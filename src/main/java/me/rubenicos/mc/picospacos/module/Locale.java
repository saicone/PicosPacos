package me.rubenicos.mc.picospacos.module;

import me.rubenicos.mc.picospacos.PicosPacos;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Locale {

    private static boolean useSpigot = false;
    private static boolean useNMS = false;
    private static boolean useRGB = false;

    private static Method chatComponent;
    private static Method messageType;
    private static Constructor<?> packetPlayOutChat;
    private static Method sendPacket;

    private static Method getHandle;
    private static Field playerConnection;

    private static Settings lang;
    private static int logLevel = -1;

    public static void init() {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        int verNumber = Integer.parseInt(version.split("_")[1]);
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            useSpigot = true;
        } catch (ClassNotFoundException ignored) {
            try {
                if (verNumber >= 17) {
                    useNMS = true;
                } else {
                    Class<?> iChatBaseComponent = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
                    chatComponent = iChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class);

                    Class<?> chatMessageType = Class.forName("net.minecraft.server." + version + ".ChatMessageType");
                    messageType = chatMessageType.getMethod("a", byte.class);
                    packetPlayOutChat = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat").getDeclaredConstructor(iChatBaseComponent, chatMessageType);

                    sendPacket = Class.forName("net.minecraft.server." + version + ".PlayerConnection").getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"));
                }

                getHandle = Class.forName("net.minecraft.server." + version + ".entity.CraftPlayer").getMethod("getHandle");
                playerConnection = Class.forName("net.minecraft.server." + version + ".EntityPlayer").getField("playerConnection");
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        if (verNumber >= 16) {
            useRGB = true;
        }
        lang = new Settings("lang/" + PicosPacos.SETTINGS.getString("Locale.Language") + ".yml", "lang/en_US.yml", false, true);
        lang.reload();
        reload();
    }

    public static void reload() {
        String path = "lang/" + PicosPacos.SETTINGS.getString("Locale.Language") + ".yml";
        if (!lang.getPath().equals(path)) {
            lang.setPath(path);
            lang.reloadDefault("lang/en_US.yml");
            if (lang.reload()) {
                sendToConsole("");
            } else {
                PicosPacos.get().getLogger().severe("Cannot reload " + path + " file! Check console.");
            }
        }
        logLevel = PicosPacos.SETTINGS.getInt("Locale.LogLevel");
    }

    public static void log(int level, String path, String... args) {
        if (level <= logLevel) {
            sendToConsole(path, args);
        }
    }

    public static void sendToConsole(String path, String... args) {
        sendTo(Bukkit.getConsoleSender(), path, args);
    }

    public static void sendTo(CommandSender user, String path, String... args) {
        text(user, replaceArgs(lang.getStringList(path), args));
    }

    public static void sendMessage(CommandSender user, String text, String... args) {
        user.sendMessage(replaceArgs(text, args));
    }

    public static void broadcast(String text, String... args) {
        String msg = replaceArgs(text, args);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(msg));
    }

    public static void broadcastPath(String path, String... args) {
        List<String> msg = replaceArgs(lang.getStringList(path), args);
        Bukkit.getOnlinePlayers().forEach(player -> text(player, msg));
    }

    private static void text(CommandSender sender, List<String> list) {
        list.forEach(sender::sendMessage);
    }

    public static void sendTitle(Player player, String path, String... args) {
        player.sendTitle(replaceArgs(lang.getString(path + ".title"), args), replaceArgs(lang.getString(path + ".subtitle"), args), lang.getInt(path + ".fadeIn"), lang.getInt(path + ".stay"), lang.getInt(path + ".fadeOut"));
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut, String... args) {
        player.sendTitle(replaceArgs(title, args), replaceArgs(subtitle, args), fadeIn, stay, fadeOut);
    }

    public static void broadcastTitle(String path, String... args) {
        String title = replaceArgs(lang.getString(path + ".title"), args);
        String subtitle = replaceArgs(lang.getString(path + ".subtitle"), args);
        int fadeIn = lang.getInt(path + ".fadeIn");
        int stay = lang.getInt(path + ".stay");
        int fadeOut = lang.getInt(path + ".fadeOut");
        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(title, subtitle, fadeIn, stay, fadeOut));
    }

    public static void broadcastTile(String title, String subtitle, int fadeIn, int stay, int fadeOut, String... args) {
        String title0 = replaceArgs(title, args);
        String subtitle0 = replaceArgs(subtitle, args);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(title0, subtitle0, fadeIn, stay, fadeOut));
    }

    public void sendActionbar(Player player, String path, String... args) {
        actionbar(player, replaceArgs(lang.getString(path), args));
    }

    public void broadcastActionbar(String path, String... args) {
        String text = replaceArgs(lang.getString(path), args);
        Bukkit.getOnlinePlayers().forEach(player -> actionbar(player, text));
    }

    private static void actionbar(Player player, String text) {
        if (useSpigot) {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
        } else if (useNMS) {
            IChatMutableComponent actionbar = IChatBaseComponent.ChatSerializer.a("{\"text\" : \"" + text + "\"}");
            PacketPlayOutChat packet = new PacketPlayOutChat(actionbar, ChatMessageType.a((byte) 2), player.getUniqueId());
            try {
                PlayerConnection connection = (PlayerConnection) playerConnection.get(getHandle.invoke(player));
                connection.sendPacket(packet);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Object component = chatComponent.invoke(null, "{\"text\" : \"" + text + "\"}");
                Object packet = packetPlayOutChat.newInstance(component, messageType.invoke(null, (byte) 2));
                Object connection = playerConnection.get(getHandle.invoke(player));
                sendPacket.invoke(connection, packet);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static String color(String s) {
        if (useRGB && s.contains("&#")) {
            StringBuilder builder = new StringBuilder();
            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (i + 7 < chars.length && chars[i] == '&' && chars[i + 1] == '#') {
                    StringBuilder color = new StringBuilder();
                    for (int c = i + 2; c < chars.length && c < 7; c++) {
                        color.append(chars[c]);
                    }
                    if (color.length() == 6) {
                        builder.append(rgb(color.toString()));
                        i += 8;
                    }
                } else {
                    builder.append(chars[i]);
                }
            }
            return ChatColor.translateAlternateColorCodes('&', builder.toString());
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private static String rgb(String color) {
        try {
            Integer.parseInt(color, 16);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid HEX color: #" + color);
        }

        StringBuilder hex = new StringBuilder("§x");
        for (char c : color.toCharArray()) {
            hex.append("§").append(c);
        }

        return hex.toString();
    }

    public static List<String> replaceArgs(List<String> list, String... args) {
        if (args.length < 1) return list;
        List<String> l0 = new ArrayList<>();
        list.forEach(s -> list.add(replaceArgs0(s, args)));
        return l0;
    }

    public static String replaceArgs(String s, String... args) {
        if (args.length < 1) return s;
        return replaceArgs0(s, args);
    }

    private static String replaceArgs0(String s, String... args) {
        return color(MessageFormat.format(s, (Object[]) args));
    }
}
