package me.rubenicos.mc.picospacos.util;

import org.bukkit.Bukkit;

public class Server {

    public static final String version;
    public static final int verNumber;
    public static final boolean isSpigot;
    public static final boolean isPaper;

    static {
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        verNumber = Integer.parseInt(version.split("_")[1]);
        boolean spigot = false;
        boolean paper = false;
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            spigot = true;
        } catch (ClassNotFoundException ignored) { }
        try {
            Class.forName("com.destroystokyo.paper.Title");
            paper = true;
        } catch (ClassNotFoundException ignored) { }
        isSpigot = spigot;
        isPaper = paper;
    }
}
