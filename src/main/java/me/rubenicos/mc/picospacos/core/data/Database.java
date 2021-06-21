package me.rubenicos.mc.picospacos.core.data;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.object.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public abstract class Database {

    private final Map<String, PlayerData> players = new HashMap<>();

    boolean init() {
        return true;
    }

    abstract void enable();

    abstract void disable();

    abstract void save(Player player, PlayerData data);

    abstract PlayerData get(Player player);

    public PlayerData loadPlayer(Player player) {
        PlayerData data = get(player);
        if (data == null) {
            data = new PlayerData(player);
        }
        players.put(player.getName(), data);
        return data;
    }

    public void savePlayer(Player player) {
        PlayerData data = players.get(player.getName());
        if (data != null && data.hasEdited()) {
            save(player, players.get(player.getName()));
        }
    }

    public PlayerData getPlayer(Player player) {
        return players.getOrDefault(player.getName(), loadPlayer(player));
    }

    private static Database instance;
    private static String current = "";

    private static final Map<String, Class<? extends Database>> types = new HashMap<>();

    static {
        types.put("JSON", DatabaseJson.class);
        types.put("SQL", DatabaseSql.class);
        types.put("SQLITE", DatabaseSqlite.class);
    }

    public static Database get() {
        return instance;
    }

    public static void reload() {
        String type = PicosPacos.SETTINGS.getString("Database.Type").toUpperCase();
        if (type.equals(current)) {
            return;
        } else {
            current = type;
        }

        if (instance != null) {
            instance.disable();
        }

        try {
            if (types.containsKey(type)) {
                instance = types.get(type).getDeclaredConstructor().newInstance();
            } else {
                Bukkit.getLogger().warning("The " + current + " database type does'nt exist, using SQLITE type instead...");
                instance = new DatabaseSqlite();
            }
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Failed to initialize " + current + " database type, using SQLITE type instead...");
            instance = new DatabaseSqlite();
        }
        if (!instance.init()) {
            instance = null;
            Bukkit.getLogger().severe("Failed to setup " + current + " database type, using JSON type instead...");
            instance = new DatabaseJson();
        }
        instance.enable();
    }

    public static Map<String, Class<? extends Database>> getTypes() {
        return types;
    }

    public static String getCurrent() {
        return current;
    }
}
