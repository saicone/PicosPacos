package me.rubenicos.mc.picospacos.core.data;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.object.PlayerData;
import me.rubenicos.mc.picospacos.module.Locale;
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
        Bukkit.getScheduler().runTaskAsynchronously(PicosPacos.get(), () -> players.values().forEach(this::save));
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
                    Bukkit.getScheduler().runTaskAsynchronously(PicosPacos.get(), () -> {
                        PlayerData data = instance.loadPlayer(e.getPlayer());
                        if (!data.getItems().isEmpty()) {
                            if (PicosPacos.SETTINGS.getInt("Config.Join-Delay") > 0) {
                                Bukkit.getScheduler().runTaskLaterAsynchronously(PicosPacos.get(), () -> {
                                    if (e.getPlayer().isOnline()) {
                                        e.getPlayer().getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
                                        data.getItems().clear();
                                        data.setEdited(true);
                                    }
                                }, PicosPacos.SETTINGS.getInt("Config.Join-Delay") * 20L);
                            } else {
                                if (e.getPlayer().isOnline()) {
                                    e.getPlayer().getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
                                    data.getItems().clear();
                                    data.setEdited(true);
                                }
                            }
                        }
                    });
                }

                @SuppressWarnings("unused")
                @EventHandler
                public void onQuit(PlayerQuitEvent e) {
                    Bukkit.getScheduler().runTaskAsynchronously(PicosPacos.get(), () -> instance.savePlayer(e.getPlayer()));
                }
            }, PicosPacos.get());
            loaded = true;
        }

        String type = PicosPacos.SETTINGS.getString("Database.Type").toUpperCase();
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

    public static Map<String, Class<? extends Database>> getTypes() {
        return types;
    }

    public static String getCurrent() {
        return current;
    }
}
