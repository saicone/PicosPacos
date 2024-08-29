package com.saicone.picospacos.api;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.object.PlayerData;
import com.saicone.picospacos.module.hook.PlayerProvider;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PicosPacosAPI {

    @NotNull
    public static PlayerData getPlayerData(@NotNull OfflinePlayer player) {
        return PicosPacos.get().getDatabase().getPlayerData(player);
    }

    @NotNull
    public static PlayerData getPlayerData(@NotNull String name) {
        if (name.contains("-")) {
            return getPlayerData(UUID.fromString(name));
        }
        return PicosPacos.get().getDatabase().getPlayerData(name, PlayerProvider.getUniqueId(name));
    }

    @NotNull
    public static PlayerData getPlayerData(@NotNull UUID uniqueId) {
        return PicosPacos.get().getDatabase().getPlayerData(PlayerProvider.getName(uniqueId), uniqueId);
    }

    @NotNull
    public static CompletableFuture<PlayerData> getPlayerDataAsync(@NotNull OfflinePlayer player) {
        return PicosPacos.get().getDatabase().getPlayerDataAsync(player);
    }

    @NotNull
    public static CompletableFuture<PlayerData> getPlayerDataAsync(@NotNull String name) {
        if (name.contains("-")) {
            return getPlayerDataAsync(UUID.fromString(name));
        }
        return PicosPacos.get().getDatabase().getPlayerDataAsync(name, PlayerProvider.getUniqueId(name));
    }

    @NotNull
    public static CompletableFuture<PlayerData> getPlayerDataAsync(@NotNull UUID uniqueId) {
        return PicosPacos.get().getDatabase().getPlayerDataAsync(PlayerProvider.getName(uniqueId), uniqueId);
    }

    public static void editPlayerData(@NotNull String name, @NotNull Consumer<PlayerData> consumer) {
        if (name.contains("-")) {
            editPlayerData(UUID.fromString(name), consumer);
        }
        PicosPacos.get().getDatabase().editPlayerData(name, PlayerProvider.getUniqueId(name), consumer);
    }

    public static void editPlayerData(@NotNull UUID uniqueId, @NotNull Consumer<PlayerData> consumer) {
        PicosPacos.get().getDatabase().editPlayerData(PlayerProvider.getName(uniqueId), uniqueId, consumer);
    }
}
