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
import com.saicone.picospacos.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
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
                registerListeners(event, priority, scripts.values().stream().filter(itemScript -> itemScript.actions().containsKey(event) && itemScript.priority(event).equals(priority)).collect(Collectors.toList()));
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
            registerListeners(event, priority, scripts, scriptExecutor);
        }
    }

    private void registerListeners(@NotNull ScriptEvent event, @NotNull ScriptExecutor<?> scriptExecutor) {
        if (!scripts.isEmpty()) {
            PicosPacos.log(3, event.name() + ":");
        }
        for (EventPriority priority : EventPriority.values()) {
            registerListeners(event, priority, scripts.values().stream().filter(itemScript -> itemScript.actions().containsKey(event) && itemScript.priority(event).equals(priority)).collect(Collectors.toList()), scriptExecutor);
        }
    }

    private void registerListeners(@NotNull ScriptEvent event, @NotNull EventPriority priority, @NotNull List<ItemScript> scripts, @NotNull ScriptExecutor<?> scriptExecutor) {
        if (scripts.isEmpty()) {
            return;
        }
        final ListenerExecutor<?> listenerExecutor = new ListenerExecutor<>(event, scriptExecutor, scripts);
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

        final BukkitSettings when = config.getConfigurationSection(settings -> settings.getIgnoreCase("when"));
        if (when == null) {
            PicosPacos.log(2, "Cannot load script " + id + ", there is no 'when' configuration");
            return Optional.empty();
        }
        final Map<ScriptEvent, Long> delays = new HashMap<>();
        final Map<ScriptEvent, EventPriority> priorities = new HashMap<>();
        final Map<ScriptEvent, ItemAction> actions = new HashMap<>();
        for (String key : when.getKeys(false)) {
            final Object value = when.get(key);

            final Long delay;
            final EventPriority priority;
            final Optional<ItemAction> execution;
            if (value instanceof ConfigurationSection) {
                final BukkitSettings section = BukkitSettings.of(value);

                delay = Strings.time(section.getIgnoreCase("delay").asString(), TimeUnit.MILLISECONDS);

                priority = Enums.getIfPresent(EventPriority.class, section.getIgnoreCase("priority").asString("NORMAL").toUpperCase()).orNull();
                if (priority == null) {
                    PicosPacos.log(2, "The script " + id + " use an invalid priority '" + section.getIgnoreCase("priority").asString("NORMAL") + "', available values: " + join(", ", EventPriority.values()));
                }

                execution = PicosPacos.get().actionRegistry().readAction(section.getRegex("(?i)run|execute|actions?").getValue());
            } else {
                delay = null;
                priority = null;
                execution = PicosPacos.get().actionRegistry().readAction(value);
            }

            if (execution.isEmpty()) {
                PicosPacos.log(2, "The execution '" + key + "' inside script " + id + " is empty");
            } else {
                for (String s : key.split(",")) {
                    ScriptEvent.of(s.trim()).ifPresentOrElse(event -> {
                        if (delay != null) {
                            delays.put(event, delay);
                        }

                        if (priority != null) {
                            priorities.put(event, priority);
                        }

                        actions.put(event, execution.get());
                    }, () -> {
                        PicosPacos.log(2, "The script " + id + " use an invalid event '" + s.trim() + "', available values: " + join(", ", ScriptEvent.VALUES));
                    });
                }
            }
        }

        if (actions.isEmpty()) {
            PicosPacos.log(2, "Cannot load script " + id + ", it doesn't have actions");
            return Optional.empty();
        }

        return Optional.of(new ItemScript(id, predicate.get(), delays, priorities, actions));
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

        private final ScriptEvent event;
        private final ScriptExecutor<E> executor;
        private final List<ItemScript> scripts;

        private ListenerExecutor(@NotNull ScriptEvent event, @NotNull ScriptExecutor<E> executor, @NotNull List<ItemScript> scripts) {
            this.event = event;
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
                        final long delay = script.delay(this.event);
                        if (delay > 0) {
                            later(() -> execute(holder, script, (E) event), (long) (delay * 0.02));
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
            holder.next(script, event);
            executor.iterate(event, item -> {
                holder.next(item);
                if (script.predicate().test(holder)) {
                    script.actions().get(this.event).apply(holder);
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
