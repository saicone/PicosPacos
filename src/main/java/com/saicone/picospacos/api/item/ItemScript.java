package com.saicone.picospacos.api.item;

import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ItemScript {

    private final String id;
    private final Set<ScriptEvent> when;
    private final EventPriority priority;
    private final long delay;
    private final ItemPredicate predicate;
    private final ItemAction execution;

    public ItemScript(@NotNull String id, @NotNull Set<ScriptEvent> when, @NotNull EventPriority priority, long delay, @NotNull ItemPredicate predicate, @NotNull ItemAction execution) {
        this.id = id;
        this.when = when;
        this.priority = priority;
        this.delay = delay;
        this.predicate = predicate;
        this.execution = execution;
    }

    @NotNull
    public String id() {
        return id;
    }

    @NotNull
    public Set<ScriptEvent> when() {
        return when;
    }

    @NotNull
    public EventPriority priority() {
        return priority;
    }

    public long delay() {
        return delay;
    }

    @NotNull
    public ItemPredicate predicate() {
        return predicate;
    }

    @NotNull
    public ItemAction execution() {
        return execution;
    }
}
