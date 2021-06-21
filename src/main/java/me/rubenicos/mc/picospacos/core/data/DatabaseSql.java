package me.rubenicos.mc.picospacos.core.data;

import me.rubenicos.mc.picospacos.api.object.PlayerData;
import org.bukkit.entity.Player;

public class DatabaseSql extends Database {

    @Override
    boolean init() {
        return true;
    }

    @Override
    void enable() {

    }

    @Override
    void disable() {

    }

    @Override
    void save(Player player, PlayerData data) {

    }

    @Override
    PlayerData get(Player player) {
        return null;
    }
}
