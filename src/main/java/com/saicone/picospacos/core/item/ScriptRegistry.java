package com.saicone.picospacos.core.item;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.api.item.ItemScript;
import com.saicone.picospacos.api.item.ScriptEvent;
import com.saicone.picospacos.core.item.executor.ItemClickExecutor;
import com.saicone.picospacos.core.item.executor.ItemDropExecutor;
import com.saicone.picospacos.core.item.executor.ItemPickupExecutor;
import com.saicone.picospacos.core.item.executor.PlayerDeathExecutor;
import com.saicone.picospacos.util.SimpleMultimap;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScriptRegistry implements Listener {

    private final SimpleMultimap<ScriptEvent, ScriptExecutor<?>, List<ScriptExecutor<?>>> eventExecutors = new SimpleMultimap<>(ArrayList::new);
    private final Map<String, ItemScript> scripts = new HashMap<>();

    public ScriptRegistry() {
        register(ScriptEvent.PLAYER_JOIN, ScriptExecutor.Player.valueOf(PlayerJoinEvent.class, PlayerJoinEvent::getHandlerList));
        register(ScriptEvent.PLAYER_LEAVE, ScriptExecutor.Player.valueOf(PlayerQuitEvent.class, PlayerQuitEvent::getHandlerList));
        register(ScriptEvent.PLAYER_RESPAWN, ScriptExecutor.Player.valueOf(PlayerRespawnEvent.class, PlayerRespawnEvent::getHandlerList));
        register(ScriptEvent.PLAYER_DIES, PlayerDeathExecutor.instance());
        register(ScriptEvent.PLAYER_WORLD, ScriptExecutor.Player.valueOf(PlayerChangedWorldEvent.class, PlayerChangedWorldEvent::getHandlerList));
        register(ScriptEvent.ITEM_DROP, ItemDropExecutor.instance());
        register(ScriptEvent.ITEM_PICKUP, ItemPickupExecutor.instance());
        register(ScriptEvent.ITEM_CLICK, ItemClickExecutor.instance());
    }

    public <T extends Event> void register(@NotNull ScriptEvent event, @NotNull ScriptExecutor<T> executor) {
        eventExecutors.get(event).add(executor);
        registerListeners(event, executor);
    }

    public <T extends Event> void replace(@NotNull ScriptEvent event, @NotNull ScriptExecutor<T> executor) {
        eventExecutors.get(event).removeIf(scriptExecutor -> {
            scriptExecutor.handlerList().unregister(this);
            return true;
        });
        eventExecutors.get(event).add(executor);
        registerListeners(event, executor);
    }

    public <T extends Event> void replace(@NotNull ScriptEvent event, @NotNull Class<? extends Event> eventType, @NotNull ScriptExecutor<T> executor) {
        eventExecutors.get(event).removeIf(scriptExecutor -> {
            if (scriptExecutor.eventType().equals(eventType)) {
                scriptExecutor.handlerList().unregister(this);
                return true;
            }
            return false;
        });
        eventExecutors.get(event).add(executor);
        registerListeners(event, executor);
    }

    public void load() {
        // Load scripts

        // Load listeners
        for (ScriptEvent event : ScriptEvent.VALUES) {
            for (EventPriority priority : EventPriority.values()) {
                registerListeners(event, priority, scripts.values().stream().filter(itemScript -> itemScript.when().contains(event) && itemScript.priority().equals(priority)).collect(Collectors.toList()));
            }
        }
    }

    public void disable() {
        HandlerList.unregisterAll(this);
    }

    public void reload() {
        disable();
        load();
    }

    private void registerListeners(@NotNull ScriptEvent event, @NotNull EventPriority priority, @NotNull List<ItemScript> scripts) {
        for (ScriptExecutor<?> scriptExecutor : eventExecutors.get(event)) {
            registerListeners(priority, scripts, scriptExecutor);
        }
    }

    private void registerListeners(@NotNull ScriptEvent event, @NotNull ScriptExecutor<?> scriptExecutor) {
        for (EventPriority priority : EventPriority.values()) {
            registerListeners(priority, scripts.values().stream().filter(itemScript -> itemScript.when().contains(event) && itemScript.priority().equals(priority)).collect(Collectors.toList()), scriptExecutor);
        }
    }

    private void registerListeners(@NotNull EventPriority priority, @NotNull List<ItemScript> scripts, @NotNull ScriptExecutor<?> scriptExecutor) {
        if (scripts.isEmpty()) {
            return;
        }
        final ListenerExecutor<?> listenerExecutor = new ListenerExecutor<>(scriptExecutor, scripts);
        final RegisteredListener listener = new RegisteredListener(this, listenerExecutor, priority, PicosPacos.get(), true);
        scriptExecutor.handlerList().register(listener);
    }

    @NotNull


    private static class ListenerExecutor<E extends Event> implements EventExecutor {

        private final ScriptExecutor<E> executor;
        private final List<ItemScript> scripts;

        private ListenerExecutor(@NotNull ScriptExecutor<E> executor, @NotNull List<ItemScript> scripts) {
            this.executor = executor;
            this.scripts = scripts;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
            if (listener instanceof ScriptRegistry && executor.eventType().isAssignableFrom(event.getClass())) {
                try {
                    final ItemHolder holder = executor.holder((E) event);
                    for (ItemScript script : scripts) {
                        holder.next(script.id());
                        executor.iterate((E) event, item -> {
                            holder.next(item);
                            if (script.predicate().test(holder)) {
                                script.execution().apply(holder);
                                if (holder.isCancelled() && event instanceof Cancellable) {
                                    ((Cancellable) event).setCancelled(true);
                                }
                            }
                            return holder.getEditedItem();
                        });
                    }
                } catch (Throwable t) {
                    throw new EventException(t);
                }
            }
        }
    }
}
