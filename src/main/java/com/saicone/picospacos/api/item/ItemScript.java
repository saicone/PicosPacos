package com.saicone.picospacos.api.item;

import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ItemScript {

    private static final long DEFAULT_DELAY = 0L;
    private static final EventPriority DEFAULT_PRIORITY = EventPriority.NORMAL;

    private final String id;
    private final ItemPredicate predicate;
    private final Map<ScriptEvent, Long> delays;
    private final Map<ScriptEvent, EventPriority> priorities;
    private final Map<ScriptEvent, ItemAction> actions;

    public ItemScript(@NotNull String id, @NotNull ItemPredicate predicate, @NotNull Map<ScriptEvent, Long> delays, @NotNull Map<ScriptEvent, EventPriority> priorities, @NotNull Map<ScriptEvent, ItemAction> actions) {
        this.id = id;
        this.predicate = predicate;
        this.delays = delays;
        this.priorities = priorities;
        this.actions = actions;
    }

    @NotNull
    public String id() {
        return id;
    }

    @NotNull
    public ItemPredicate predicate() {
        return predicate;
    }

    public long delay(@NotNull ScriptEvent event) {
        return delays.getOrDefault(event, DEFAULT_DELAY);
    }

    @NotNull
    public Map<ScriptEvent, Long> delays() {
        return delays;
    }

    @NotNull
    public EventPriority priority(@NotNull ScriptEvent event) {
        return priorities.getOrDefault(event, DEFAULT_PRIORITY);
    }

    @NotNull
    public Map<ScriptEvent, EventPriority> priorities() {
        return priorities;
    }

    @NotNull
    public Map<ScriptEvent, ItemAction> actions() {
        return actions;
    }
}
