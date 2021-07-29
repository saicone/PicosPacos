package me.rubenicos.mc.picospacos.core.data;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.object.PlayerData;
import me.rubenicos.mc.picospacos.module.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Database {

    private final Map<UUID, PlayerData> players = new HashMap<>();

    boolean useID = true;

    boolean init() {
        return true;
    }

    void enable() {
        useID = PicosPacos.getSettings().getString("Database.Method").equalsIgnoreCase("UUID");
    }

    void disable() { }

    abstract void save(PlayerData data);

    abstract PlayerData get(String name, String uuid);

    public PlayerData loadPlayer(Player player) {
        return loadPlayer(player.getName(), player.getUniqueId());
    }

    public PlayerData loadPlayer(String name, UUID uuid) {
        PlayerData data = get(name, uuid.toString());
        if (data == null) {
            data = new PlayerData(name, uuid.toString(), 0);
        }
        players.put(uuid, data);
        return players.get(uuid);
    }

    public void savePlayer(Player player) {
        PlayerData data = players.get(player.getUniqueId());
        if (data != null && data.hasEdited()) {
            save(data);
        }
    }

    void saveAll() {
        players.values().forEach(this::save);
    }

    void saveAllAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(PicosPacos.get(), () -> players.values().forEach(this::save));
    }

    public PlayerData getPlayer(Player player) {
        return getPlayer(player.getUniqueId(), false);
    }

    public PlayerData getPlayer(Player player, boolean load) {
        return getPlayer(player.getUniqueId(), load);
    }

    public PlayerData getPlayer(UUID uuid) {
        return getPlayer(uuid, false);
    }

    public PlayerData getPlayer(UUID uuid, boolean load) {
        if (players.containsKey(uuid)) {
            return players.get(uuid);
        } else if (load) {
            return loadPlayer(Bukkit.getOfflinePlayer(uuid).getName(), uuid);
        } else {
            return null;
        }
    }

    public static class Instance {
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
            String type = PicosPacos.getSettings().getString("Database.Type").toUpperCase();
            if (type.equals(current)) {
                if (instance.init()) {
                    instance.enable();
                } else {
                    Locale.log(1, "");
                }
                return;
            } else {
                current = type;
            }

            unload();

            try {
                if (types.containsKey(type)) {
                    instance = types.get(type).getDeclaredConstructor().newInstance();
                } else {
                    Locale.log(1, "The {0} database type does'nt exist, using SQLITE type instead...", current);
                    instance = new DatabaseSqlite();
                }
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
                Locale.log(1, "Failed to initialize {0} database type, using SQLITE type instead...", current);
                instance = new DatabaseSqlite();
            }
            if (!instance.init()) {
                instance = null;
                Locale.log(1, "Failed to setup {0} database type, using JSON type instead...", current);
                instance = new DatabaseJson();
            }
            instance.enable();
            Bukkit.getScheduler().runTaskAsynchronously(PicosPacos.get(), () -> Bukkit.getOnlinePlayers().forEach(player -> instance.loadPlayer(player)));
        }

        public static void unload() {
            if (instance != null) {
                instance.saveAll();
                instance.disable();
            }
        }

        public static void addType(String name, Class<? extends Database> type) {
            if (!types.containsKey(name)) types.put(name, type);
        }

        public static void removeType(String name) {
            if (!name.equalsIgnoreCase("JSON") && !name.equalsIgnoreCase("SQL") && !name.equalsIgnoreCase("SQLITE")) {
                types.remove(name);
            }
        }

        public static String getCurrent() {
            return current;
        }
    }
}
