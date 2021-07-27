package me.rubenicos.mc.picospacos.module;

import me.clip.placeholderapi.PlaceholderAPI;
import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.util.LookupUtils;
import me.rubenicos.mc.picospacos.util.Server;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Locale class only compatible with +1.12 servers
 * @author Rubenicos
 * @version 2.3.0
 */
public class Locale {

    // Options depending server type and version
    private static final boolean useSpigot;
    private static final boolean useRGB;
    private static final boolean newPacket;
    public static boolean allowPAPI = false;

    // Reflected ChatMessageType
    private static Object ACTIONBAR_TYPE;

    // Locale options
    private static Settings lang;
    private static int logLevel = -1;

    static {
        useSpigot = Server.isSpigot;
        useRGB = Server.verNumber >= 16;
        newPacket = Server.verNumber >= 14;
        if (!useSpigot) {
            try {
                ACTIONBAR_TYPE = LookupUtils.get("chatType").invoke((byte) 2);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reload locale options. <br>
     * If you change the language in settings the plugin will try to
     * update the selected file if they exists on plugin jar, otherwise
     * the updater will use en_US language to put the changes.
     */
    public static void reload() {
        String path = "lang/" + PicosPacos.getSettings().getString("Locale.Language") + ".yml";
        if (lang == null) {
            lang = new Settings(PicosPacos.get(), path, "lang/en_US.yml", false, true);
            lang.reload();
        }

        if (!lang.getPath().equals(path)) {
            lang = new Settings(PicosPacos.get(), path, "lang/en_US.yml", false, true);
            if (lang.reload()) {
                sendToConsole("");
            } else {
                PicosPacos.get().getLogger().severe("Cannot reload " + path + " file! Check console.");
            }
        }
        logLevel = PicosPacos.getSettings().getInt("Locale.LogLevel");
    }

    /**
     * Log message from path to {@link #sendToConsole(String, String...)} with certain
     * importance level, depending on plugin settings the message will logged or not.
     * @param level Importance level.
     * @param path Message path defined on language file.
     * @param args Arguments to replace via {@link #replaceArgs(String, String...)}.
     */
    public static void log(int level, String path, String... args) {
        if (level >= logLevel) {
            text(Bukkit.getConsoleSender(), replaceArgs(lang.getStringList(path), args));
        }
    }

    /**
     * Sends message from path to server console.
     * @param path Message path defined on language file.
     * @param args Arguments to replace via {@link #replaceArgs(String, String...)}.
     */
    public static void sendToConsole(String path, String... args) {
        sendTo(Bukkit.getConsoleSender(), path, args);
    }

    /**
     * Sends message from path to CommandSender.
     * @param user Plugin user to send the message.
     * @param path Message path defined on language file.
     * @param args Arguments to replace via {@link #replaceArgs(String, String...)}.
     */
    public static void sendTo(CommandSender user, String path, String... args) {
        if (lang.isSection(path)) {
            if (user instanceof Player) {
                String type = lang.getString(path + ".type").toLowerCase();
                if (type.equals("title")) {
                    sendTitle((Player) user, lang.getString(path + ".title"), lang.getString(path + ".subtitle"), lang.getInt(path + ".fadeIn", 10), lang.getInt(path + ".stay", 70), lang.getInt(path + ".fadeOut", 20), args);
                } else if (type.equals("actionbar")) {
                    sendActionbar((Player) user, lang.getString(path + ".text"), args);
                } else {
                    log(1, "Log.Locale.Invalid-Type", type, path);
                }
            } else {
                log(1, "Log.Locale.Unsupported-Sender", path);
            }
        } else {
            text(user, replaceArgs(lang.getStringList(path), args));
        }
    }

    /**
     * Sends message to CommandSender. <br>
     * Compatible with chat colors and RGB.
     * @param user Plugin user to send the message.
     * @param text Message to send.
     * @param args Arguments to replace via {@link #replaceArgs(String, String...)}.
     */
    public static void sendMessage(CommandSender user, String text, String... args) {
        user.sendMessage(replaceArgs(text, args));
    }

    /**
     * Broadcast message to all online players. <br>
     * Compatible with chat colors and RGB.
     * @param text Message to send.
     * @param args Arguments to replace via {@link #replaceArgs(String, String...)}.
     */
    public static void broadcast(String text, String... args) {
        String msg = replaceArgs(text, args);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(msg));
    }

    /**
     * Broadcast message from path to all online players.
     * @param path Message path defined on language file.
     * @param args Arguments to replace via {@link #replaceArgs(String, String...)}.
     */
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

    public static void sendActionbar(Player player, String path, String... args) {
        actionbar(player, replaceArgs(lang.getString(path), args));
    }

    public static void broadcastActionbar(String path, String... args) {
        String text = replaceArgs(lang.getString(path), args);
        Bukkit.getOnlinePlayers().forEach(player -> actionbar(player, text));
    }

    private static void actionbar(Player player, String text) {
        if (useSpigot) {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
        } else {
            try {
                packetActionbar(player, text);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void packetActionbar(Player player, String text) throws Throwable {
        Object packet;
        if (newPacket) {
            packet = LookupUtils.get("chatPacket").invoke(LookupUtils.get("chatComponent").invoke(text), ACTIONBAR_TYPE, player.getUniqueId());
        } else {
            packet = LookupUtils.get("chatPacket").invoke(LookupUtils.get("chatComponent").invoke(text), ACTIONBAR_TYPE);
        }

        if (packet != null) {
            Object con = LookupUtils.get("playerConnection").invoke(LookupUtils.get("playerHandle").invoke(player));
            if (con != null) {
                LookupUtils.get("sendPacket").invoke(con, packet);
            }
        }
    }

    public static String parsePlaceholders(Player player, String text) {
        return parsePlaceholders(player, text, false);
    }

    public static String parsePlaceholders(Player player, String text, boolean color) {
        String s = allowPAPI && text.contains("%") ? PlaceholderAPI.setPlaceholders(player, text) : text;
        return color ? color(s) : s;
    }

    public static List<String> parsePlaceholders(Player player, List<String> list) {
        return parsePlaceholders(player, list, false);
    }


    public static List<String> parsePlaceholders(Player player, List<String> list, boolean color) {
        if (allowPAPI) {
            List<String> list1 = new ArrayList<>();
            list.forEach(s -> list1.add(color ? color(PlaceholderAPI.setPlaceholders(player, s)) : PlaceholderAPI.setPlaceholders(player, s)));
            return list1;
        } else if (color) {
            List<String> list1 = new ArrayList<>();
            list.forEach(s -> list1.add(color(s)));
            return list1;
        } else {
            return list;
        }
    }

    public static List<String> color(List<String> list) {
        List<String> list1 = new ArrayList<>();
        list.forEach(s -> list1.add(color(s)));
        return list1;
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
            log(2, "Log.Locale.Invalid-Hex", color);
            return "<Invalid HEX>";
        }

        StringBuilder hex = new StringBuilder("§x");
        for (char c : color.toCharArray()) {
            hex.append("§").append(c);
        }

        return hex.toString();
    }

    public static List<String> replaceArgs(List<String> list, String... args) {
        if (args.length < 1 && list.isEmpty()) return list;
        List<String> l0 = new ArrayList<>();
        list.forEach(s -> l0.add(replaceArgs0(s, args)));
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
