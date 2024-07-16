package com.saicone.picospacos.module.data;

import com.saicone.picospacos.api.object.PlayerData;
import com.saicone.picospacos.module.settings.BukkitSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface DataClient {

    default void onLoad(@NotNull BukkitSettings config) {
        // empty default method
    }

    default void onEnable() {
        // empty default method
    }

    default void onDisable() {
        // empty default method
    }

    @Nullable
    PlayerData loadPlayerData(@NotNull DataMethod method, @NotNull String name, @NotNull UUID uniqueId);

    void savePlayerData(@NotNull DataMethod method, @NotNull PlayerData data);
}
