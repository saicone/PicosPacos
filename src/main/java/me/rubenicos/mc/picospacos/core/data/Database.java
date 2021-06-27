package me.rubenicos.mc.picospacos.core.data;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.object.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Database {

    private final Map<UUID, PlayerData> players = new HashMap<>();

    boolean init() {
        return true;
    }

    abstract void enable();

    abstract void disable();

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
        return data;
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

    public PlayerData getPlayer(Player player) {
        return players.getOrDefault(player.getUniqueId(), loadPlayer(player));
    }

    public PlayerData getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    private static Database instance;
    private static boolean loaded = false;
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
        if (!loaded) {
            PicosPacos.get().getServer().getPluginManager().registerEvents(new Listener() {
                @SuppressWarnings("unused")
                @EventHandler
                public void onJoin(PlayerJoinEvent e) {
                    PlayerData data = instance.loadPlayer(e.getPlayer());
                    if (!data.getItems().isEmpty()) {
                        if (PicosPacos.SETTINGS.getInt("Config.Join-Delay") > 0) {
                            Bukkit.getScheduler().runTaskLaterAsynchronously(PicosPacos.get(), () -> {
                                if (e.getPlayer().isOnline()) {
                                    e.getPlayer().getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
                                    data.getItems().clear();
                                }
                            }, PicosPacos.SETTINGS.getInt("Config.Join-Delay") * 20L);
                        } else {
                            if (e.getPlayer().isOnline()) {
                                e.getPlayer().getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
                                data.getItems().clear();
                            }
                        }
                    }
                }

                @SuppressWarnings("unused")
                @EventHandler
                public void onQuit(PlayerQuitEvent e) {
                    instance.savePlayer(e.getPlayer());
                }
            }, PicosPacos.get());
            loaded = true;
        }

        String type = PicosPacos.SETTINGS.getString("Database.Type").toUpperCase();
        if (type.equals(current)) {
            return;
        } else {
            current = type;
        }

        unload();

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
        Bukkit.getOnlinePlayers().forEach(player -> instance.loadPlayer(player));
    }

    public static void unload() {
        if (instance != null) {
            instance.saveAll();
            instance.disable();
        }
    }

    public static Map<String, Class<? extends Database>> getTypes() {
        return types;
    }

    public static String getCurrent() {
        return current;
    }
}
