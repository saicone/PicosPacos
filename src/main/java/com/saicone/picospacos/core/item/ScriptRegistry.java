package com.saicone.picospacos.core.item;

import com.google.common.base.Enums;
import com.google.common.base.Suppliers;
import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.api.item.ItemPredicate;
import com.saicone.picospacos.api.item.ItemScript;
import com.saicone.picospacos.api.item.ScriptEvent;
import com.saicone.picospacos.core.item.executor.ItemClickExecutor;
import com.saicone.picospacos.core.item.executor.ItemDropExecutor;
import com.saicone.picospacos.core.item.executor.ItemPickupExecutor;
import com.saicone.picospacos.core.item.executor.PlayerDeathExecutor;
import com.saicone.picospacos.module.settings.BukkitSettings;
import com.saicone.picospacos.util.SimpleMultimap;
import com.saicone.types.Types;
import org.bukkit.Bukkit;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ScriptRegistry implements Listener {

    private static final Supplier<File> SCRIPTS_FOLDER = Suppliers.memoize(() -> new File(PicosPacos.get().getDataFolder(), "scripts"));

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
        if (!SCRIPTS_FOLDER.get().exists()) {
            SCRIPTS_FOLDER.get().mkdirs();
            PicosPacos.get().saveResource("scripts/default.yml", false);
        }
        loadScripts(SCRIPTS_FOLDER.get());
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
        if (!scripts.isEmpty()) {
            PicosPacos.log(3, event.name() + ":");
        }
        for (ScriptExecutor<?> scriptExecutor : eventExecutors.get(event)) {
            registerListeners(priority, scripts, scriptExecutor);
        }
    }

    private void registerListeners(@NotNull ScriptEvent event, @NotNull ScriptExecutor<?> scriptExecutor) {
        if (!scripts.isEmpty()) {
            PicosPacos.log(3, event.name() + ":");
        }
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
        PicosPacos.log(3, "  - Priority " + priority.name() + " with " + scripts.size() + " scripts");
        scriptExecutor.handlerList().register(listener);
    }

    public void loadScripts(@NotNull File file) {
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files != null) {
                for (File children : files) {
                    loadScripts(children);
                }
            }
            return;
        }
        final String suffix = suffix(file);
        if (!suffix.equalsIgnoreCase("yml") && !suffix.equalsIgnoreCase("yaml")) {
            return;
        }
        final BukkitSettings settings = BukkitSettings.of(file);
        for (String key : settings.getKeys(false)) {
            String path = path(file);
            path = path.substring(0, path.length() - suffix.length() - 1);
            final String id = path + ":" + key;
            final BukkitSettings config = settings.getConfigurationSection(key);
            if (config != null) {
                readScript(id, config).ifPresent(script -> {
                    scripts.put(id, script);
                });
            }
        }
    }

    @NotNull
    public Optional<ItemScript> readScript(@NotNull String id, @NotNull BukkitSettings config) {
        if (!config.getIgnoreCase("enabled").asBoolean(true)) {
            return Optional.empty();
        }
        final Set<ScriptEvent> when = new HashSet<>();
        for (String s : config.getIgnoreCase("when").asList(Types.STRING)) {
            ScriptEvent.of(s).ifPresent(when::add);
        }

        final EventPriority priority = Enums.getIfPresent(EventPriority.class, config.getIgnoreCase("priority").asString("NORMAL").toUpperCase()).orNull();
        if (priority == null) {
            PicosPacos.log(2, "Cannot load script " + id + ", the event priority '' doesn't exist, available values: " + join(", ", EventPriority.values()));
            return Optional.empty();
        }

        final long delay = config.getIgnoreCase("delay").asLong(0L);

        final BukkitSettings item = config.getConfigurationSection(settings -> settings.getIgnoreCase("item"));
        if (item == null) {
            PicosPacos.log(2, "Cannot load script " + id + ", there is no 'item' configured to detect");
            return Optional.empty();
        }
        final Optional<ItemPredicate> predicate = ItemFields.predicate(item);
        if (predicate.isEmpty()) {
            PicosPacos.log(2, "Cannot load script " + id + ", the item predicate is empty");
            return Optional.empty();
        }

        final Optional<ItemAction> execution = PicosPacos.get().actionRegistry().readAction(config.getRegex("(?i)run|actions?").getValue());
        if (execution.isEmpty()) {
            PicosPacos.log(2, "Cannot load script " + id + ", the item action is empty");
            return Optional.empty();
        }

        return Optional.of(new ItemScript(id, when, priority, delay, predicate.get(), execution.get()));
    }

    @NotNull
    private static String suffix(@NotNull File file) {
        final String name = file.getName();
        final int index = name.lastIndexOf('.');
        if (index >= 0) {
            return name.substring(index + 1);
        }
        return "";
    }

    @NotNull
    private static String path(@Nullable File file) {
        if (file == null) return "";
        final File parent = file.getParentFile();
        if (parent == null) {
            return file.getName();
        }
        try {
            if (parent.getCanonicalPath().equals(SCRIPTS_FOLDER.get().getCanonicalPath())) {
                return file.getName();
            }
        } catch (IOException e) {
            PicosPacos.logException(2, e);
        }
        return path(parent) + "/" + file.getName();
    }

    @NotNull
    private static <E extends Enum<E>> String join(@NotNull String delimiter, @NotNull E[] values) {
        final StringJoiner joiner = new StringJoiner(delimiter);
        for (@NotNull E value : values) {
            joiner.add(value.name());
        }
        return joiner.toString();
    }

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
                        if (script.delay() > 0) {
                            later(() -> execute(holder, script, (E) event), (long) (script.delay() * 0.02));
                        } else {
                            execute(holder, script, (E) event);
                        }
                    }
                } catch (Throwable t) {
                    throw new EventException(t);
                }
            }
        }

        private void execute(@NotNull ItemHolder holder, @NotNull ItemScript script, @NotNull E event) {
            holder.next(script);
            executor.iterate(event, item -> {
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

        private static void later(@NotNull Runnable runnable, long ticks) {
            if (Bukkit.isPrimaryThread()) {
                Bukkit.getScheduler().runTaskLater(PicosPacos.get(), runnable, ticks);
            } else {
                Bukkit.getScheduler().runTaskLaterAsynchronously(PicosPacos.get(), runnable, ticks);
            }
        }
    }
}
