package com.saicone.picospacos.api.item;

import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ItemScript {

    private final String id;
    private final long delay;
    private final ItemPredicate predicate;
    private final Map<ScriptEvent, EventPriority> priorities;
    private final Map<ScriptEvent, ItemAction> actions;

    public ItemScript(@NotNull String id, long delay, @NotNull ItemPredicate predicate, @NotNull Map<ScriptEvent, EventPriority> priorities, @NotNull Map<ScriptEvent, ItemAction> actions) {
        this.id = id;
        this.delay = delay;
        this.predicate = predicate;
        this.priorities = priorities;
        this.actions = actions;
    }

    @NotNull
    public String id() {
        return id;
    }

    public long delay() {
        return delay;
    }

    @NotNull
    public ItemPredicate predicate() {
        return predicate;
    }

    @NotNull
    public EventPriority priority(@NotNull ScriptEvent event) {
        return priorities.getOrDefault(event, EventPriority.NORMAL);
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
