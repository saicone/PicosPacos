package com.saicone.picospacos.api.item;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum ScriptEvent {

    PLAYER_JOIN,
    PLAYER_LEAVE,
    PLAYER_RESPAWN,
    PLAYER_DIES,
    PLAYER_WORLD,
    ITEM_DROP,
    ITEM_PICKUP,
    ITEM_CLICK;

    public static final ScriptEvent[] VALUES = values();

    @NotNull
    public static Optional<ScriptEvent> of(@NotNull String s) {
        for (ScriptEvent value : VALUES) {
            if (value.name().equalsIgnoreCase(s)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
