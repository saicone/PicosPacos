package com.saicone.picospacos.core.data;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.object.PlayerData;
import com.saicone.picospacos.module.data.DataClient;
import com.saicone.picospacos.module.data.DataMethod;
import com.saicone.picospacos.module.data.client.JsonClient;
import com.saicone.picospacos.module.data.client.MySQLClient;
import com.saicone.picospacos.module.data.client.SqliteClient;
import com.saicone.picospacos.module.hook.PlayerProvider;
import com.saicone.picospacos.module.settings.BukkitSettings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class Database implements Listener {

    private final Executor executor;

    private final Map<UUID, PlayerData> players = new HashMap<>();
    private DataMethod method = DataMethod.UUID;
    private DataClient client;

    public Database(@NotNull Plugin plugin) {
        this.executor = (runnable) -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        async(() -> loadPlayerData(event.getPlayer().getName(), event.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        async(() -> savePlayerData(event.getPlayer().getUniqueId()));
    }

    public void onLoad() {
        this.method = PicosPacos.settings().getIgnoreCase("database", "method").asString("UUID").equalsIgnoreCase("NAME") ? DataMethod.NAME : DataMethod.UUID;
        final String type = PicosPacos.settings().getIgnoreCase("database", "type").asString("SQLITE").toUpperCase();
        switch (type) {
            case "JSON":
                this.client = new JsonClient();
                break;
            case "SQLITE":
                this.client = new SqliteClient();
                break;
            case "MYSQL":
                this.client = new MySQLClient();
                break;
            default:
                PicosPacos.log(2, "The database type '" + type + "' doesn't exist");
                return;
        }
        final BukkitSettings config = PicosPacos.settings().getConfigurationSection(settings -> {
            if (type.equalsIgnoreCase("JSON")) {
                return settings.getRegex("(?i)database", "(?i)json|file");
            } else {
                return settings.getRegex("(?i)database", "(?i)sql|" + type);
            }
        });
        this.client.onLoad(config != null ? config : new BukkitSettings());
    }

    public void onEnable() {
        this.client.onEnable();
    }

    public void onDisable() {
        saveAllPlayers();
        this.players.clear();
        this.client.onDisable();
    }

    public void onReload() {
        onDisable();
        onLoad();
        onEnable();
    }

    @NotNull
    protected synchronized PlayerData loadPlayerData(@NotNull String name, @NotNull UUID uniqueId) {
        PlayerData data = this.client.loadPlayerData(this.method, name, uniqueId);
        if (data == null) {
            data = new PlayerData(name, uniqueId, 0);
        }
        final PlayerData existing = this.players.get(uniqueId);
        if (existing == null) {
            this.players.put(uniqueId, data);
        } else {
            existing.load(data);
            data = existing;
        }
        return data;
    }

    @NotNull
    public PlayerData getPlayerData(@NotNull OfflinePlayer player) {
        return getPlayerData(player.getName() != null ? player.getName() : PlayerProvider.getName(player.getUniqueId()), player.getUniqueId());
    }

    @NotNull
    public PlayerData getPlayerData(@NotNull String name, @NotNull UUID uniqueId) {
        PlayerData data = this.players.get(uniqueId);
        if (data == null) {
            data = new PlayerData(name, uniqueId, 0);
            async(() -> loadPlayerData(name, uniqueId));
            this.players.put(uniqueId, data);
        }
        return data;
    }

    @NotNull
    public CompletableFuture<PlayerData> getPlayerDataAsync(@NotNull OfflinePlayer player) {
        return getPlayerDataAsync(player.getName() != null ? player.getName() : PlayerProvider.getName(player.getUniqueId()), player.getUniqueId());
    }

    @NotNull
    public CompletableFuture<PlayerData> getPlayerDataAsync(@NotNull String name, @NotNull UUID uniqueId) {
        final PlayerData data = this.players.get(uniqueId);
        if (data != null) {
            return CompletableFuture.completedFuture(data);
        } else {
            return CompletableFuture.supplyAsync(() -> loadPlayerData(name, uniqueId), this.executor);
        }
    }

    public void savePlayerData(@NotNull UUID uniqueId) {
        final PlayerData data = this.players.get(uniqueId);
        if (data != null && data.isEdited()) {
            this.client.savePlayerData(this.method, data);
            this.players.remove(uniqueId);
        }
    }

    public void editPlayerData(@NotNull String name, @NotNull UUID uniqueId, @NotNull Consumer<PlayerData> consumer) {
        if (this.players.containsKey(uniqueId)) {
            consumer.accept(this.players.get(uniqueId));
        } else {
            async(() -> {
                final PlayerData data = loadPlayerData(name, uniqueId);
                consumer.accept(data);
                savePlayerData(uniqueId);
            });
        }
    }

    public void saveAllPlayers() {
        for (Map.Entry<UUID, PlayerData> entry : this.players.entrySet()) {
            this.client.savePlayerData(this.method, entry.getValue());
        }
    }

    private void async(@NotNull Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            executor.execute(runnable);
        } else {
            runnable.run();
        }
    }
}
