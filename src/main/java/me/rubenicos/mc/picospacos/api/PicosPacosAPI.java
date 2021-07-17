package me.rubenicos.mc.picospacos.api;

import me.rubenicos.mc.picospacos.api.object.PlayerData;
import me.rubenicos.mc.picospacos.core.data.Database;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PicosPacosAPI {

    public static Database getDatabase() {
        return Database.Instance.get();
    }

    public static void registerDataType(String name, Class<? extends Database> type) {
        Database.Instance.addType(name.toUpperCase(), type);
    }

    public static void unregisterDataType(String name) {
        Database.Instance.removeType(name.toUpperCase());
    }

    @Nullable
    public static PlayerData getPlayer(Player player) {
        return getDatabase().getPlayer(player);
    }

    @NotNull
    public static PlayerData getPlayerOrLoad(Player player) {
        return getDatabase().getPlayer(player, true);
    }

    @Nullable
    public static PlayerData getPlayer(UUID player) {
        return getDatabase().getPlayer(player);
    }

    @NotNull
    public static PlayerData getPlayerOrLoad(UUID player) {
        return getDatabase().getPlayer(player, true);
    }

    @NotNull
    public static PlayerData loadPlayer(Player player) {
        return getDatabase().loadPlayer(player);
    }

    public static void savePlayer(Player player) {
        getDatabase().savePlayer(player);
    }
}
