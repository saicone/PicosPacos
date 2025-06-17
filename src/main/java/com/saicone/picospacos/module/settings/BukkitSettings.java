/*
 * This file is part of PixelBuy, licensed under the MIT License
 *
 * Copyright (c) 2024 Rubenicos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.saicone.picospacos.module.settings;

import com.saicone.types.IterableType;
import com.saicone.types.ValueType;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BukkitSettings extends YamlConfiguration {

    private static final MethodHandle MAP;

    static {
        MethodHandle map = null;
        try {
            final var field = MemorySection.class.getDeclaredField("map");
            field.setAccessible(true);
            map = MethodHandles.lookup().unreflectGetter(field);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        MAP = map;
    }

    private final ConfigurationSection delegate;

    @Nullable
    @Contract("!null -> !null")
    public static BukkitSettings of(@Nullable Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof BukkitSettings) {
            return (BukkitSettings) object;
        } else if (object instanceof ConfigurationSection) {
            return new BukkitSettings((ConfigurationSection) object);
        } else if (object instanceof Map) {
            final BukkitSettings settings = new BukkitSettings();
            settings.set((Map<?, ?>) object);
            return settings;
        } else {
            throw new IllegalArgumentException("The object type '" + object.getClass().getName() + "' cannot be converted to BukkitSettings instance");
        }
    }

    public BukkitSettings() {
        this(null);
    }

    public BukkitSettings(@Nullable ConfigurationSection delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    Map<String, ?> internal() {
        try {
            return (Map<String, ?>) MAP.invoke(getMemorySection());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @NotNull
    public ConfigurationSection getDelegate() {
        return delegate;
    }

    @NotNull
    @Override
    public Set<String> getKeys(boolean deep) {
        return delegate == null ? super.getKeys(deep) : delegate.getKeys(deep);
    }

    @NotNull
    public Set<String> getKeys(@NotNull Predicate<String> cut) {
        return getKeys(true).stream().map(path -> {
            final StringJoiner joiner = new StringJoiner(".");
            for (String key : path.split("\\.")) {
                joiner.add(key);
                if (cut.test(key)) {
                    break;
                }
            }
            return joiner.toString();
        }).collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Map<String, Object> getValues(boolean deep) {
        return delegate == null ? super.getValues(deep) : delegate.getValues(deep);
    }

    @Nullable
    @Override
    public Configuration getRoot() {
        return delegate == null ? super.getRoot() : delegate.getRoot();
    }

    @Nullable
    @Override
    public ConfigurationSection getParent() {
        return delegate == null ? super.getParent() : delegate.getParent();
    }

    @NotNull
    @Override
    public String getName() {
        return delegate == null ? super.getName() : delegate.getName();
    }

    @NotNull
    @Override
    public String getCurrentPath() {
        return delegate == null ? super.getCurrentPath() : delegate.getCurrentPath();
    }

    @Nullable
    @Override
    public Configuration getDefaults() {
        if (delegate instanceof MemoryConfiguration) {
            return ((MemoryConfiguration) delegate).getDefaults();
        }
        return super.getDefaults();
    }

    @Nullable
    @Override
    public Object get(@NotNull String path, @Nullable Object def) {
        return delegate == null ? super.get(path, def) : delegate.get(path, def);
    }

    @Nullable
    public Object get(@NotNull String... path) {
        return getIf(String::equals, path);
    }

    @Nullable
    public Object get(@NotNull Function<BukkitSettings, Object> getter) {
        final Object object = getter.apply(this);
        return object instanceof ValueType ? ((ValueType<?>) object).getValue() : object;
    }

    @Nullable
    protected Object getIf(@NotNull Predicate<String> condition) {
        for (var entry : internal().entrySet()) {
            if (condition.test(entry.getKey())) {
                return get(entry.getKey());
            }
        }
        return null;
    }

    @Nullable
    protected Object getIf(@NotNull BiPredicate<String, String> condition, @NotNull String... path) {
        Object object = getMemorySection();
        for (String key : path) {
            if (object instanceof MemorySection) {
                object = getIfType((MemorySection) object, condition, key);
                continue;
            }
            return null;
        }
        return object;
    }

    @Nullable
    protected <T> Object getIf(@NotNull Function<String, T> keyConversion, @NotNull BiPredicate<String, T> condition, @NotNull String... path) {
        Object object = getMemorySection();
        for (String key : path) {
            if (object instanceof MemorySection) {
                object = getIfType((MemorySection) object, condition, keyConversion.apply(key));
                continue;
            }
            return null;
        }
        return object;
    }

    @Nullable
    protected <T> Object getIfType(@NotNull BiPredicate<String, T> condition, @NotNull T type) {
        return getIfType(getMemorySection(), condition, type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> Object getIfType(@NotNull MemorySection section, @NotNull BiPredicate<String, T> condition, @NotNull T type) {
        try {
            for (var entry : ((Map<String, ?>) MAP.invoke(section)).entrySet()) {
                if (condition.test(entry.getKey(), type)) {
                    return section.get(entry.getKey());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    @NotNull
    protected <T> Map.Entry<String[], Object> getEntryIf(@NotNull Function<String, T> keyConversion, @NotNull BiPredicate<String, T> condition, @NotNull String... path) {
        final List<String> list = new ArrayList<>();
        Object object = getMemorySection();
        for (String key : path) {
            list.add(key);
            if (object instanceof MemorySection) {
                object = getIfType((MemorySection) object, condition, keyConversion.apply(key));
                continue;
            }
            return new AbstractMap.SimpleEntry<>(list.toArray(new String[0]), null);
        }
        return new AbstractMap.SimpleEntry<>(list.toArray(new String[0]), object);
    }

    @NotNull
    public MemorySection getMemorySection() {
        return delegate instanceof MemorySection ? (MemorySection) delegate : this;
    }

    @NotNull
    public ConfigurationSection getConfigurationSection() {
        return delegate == null ? this : delegate;
    }

    @Nullable
    @Override
    public BukkitSettings getConfigurationSection(@NotNull String path) {
        return of(super.getConfigurationSection(path));
    }

    @Nullable
    public BukkitSettings getConfigurationSection(@NotNull Function<BukkitSettings, Object> getter) {
        final Object object = get(getter);
        return object instanceof ConfigurationSection ? of(object) : null;
    }

    @Nullable
    public <T extends ConfigurationSection> T getConfigurationSection(@NotNull String path, @NotNull Function<ConfigurationSection, T> function) {
        final ConfigurationSection section = super.getConfigurationSection(path);
        return section == null ? null : function.apply(section);
    }

    @Nullable
    public <T extends ConfigurationSection> T getConfigurationSection(@NotNull Function<BukkitSettings, Object> getter, @NotNull Function<ConfigurationSection, T> function) {
        final Object object = get(getter);
        return object instanceof ConfigurationSection ? function.apply((ConfigurationSection) object) : null;
    }

    @NotNull
    public List<String> getComments(@NotNull String path) {
        return delegate == null ? super.getComments(path) : delegate.getComments(path);
    }

    @NotNull
    public List<String> getInlineComments(@NotNull String path) {
        return delegate == null ? super.getInlineComments(path) : delegate.getInlineComments(path);
    }

    @NotNull
    public ValueType<Object> getAny(@NotNull String key) {
        return ValueType.of(get(key));
    }

    @NotNull
    public ValueType<Object> getAny(@NotNull String... path) {
        return ValueType.of(get(path));
    }

    @NotNull
    public ValueType<Object> getIgnoreCase(@NotNull String key) {
        return ValueType.of(getIf(s -> s.equalsIgnoreCase(key)));
    }

    @NotNull
    public ValueType<Object> getIgnoreCase(@NotNull String... path) {
        return ValueType.of(getIf(String::equalsIgnoreCase, path));
    }

    @NotNull
    public ValueType<Object> getRegex(@NotNull @Language("RegExp") String regex) {
        final Pattern pattern = Pattern.compile(regex);
        return ValueType.of(getIf(s -> pattern.matcher(s).matches()));
    }

    @NotNull
    public ValueType<Object> getRegex(@NotNull @Language("RegExp") String... regexPath) {
        return ValueType.of(getIf(Pattern::compile, (s, pattern) -> pattern.matcher(s).matches(), regexPath));
    }

    @NotNull
    public Map.Entry<String[], Object> getRegexEntry(@NotNull @Language("RegExp") String... regexPath) {
        return getEntryIf(Pattern::compile, (s, pattern) -> pattern.matcher(s).matches(), regexPath);
    }

    public void set(@NotNull ConfigurationSection section) {
        set(section, true);
    }

    public void set(@NotNull ConfigurationSection section, boolean copy) {
        for (String path : section.getKeys(true)) {
            final Object value = section.get(path);
            if (copy && value instanceof List) {
                set(path, new ArrayList<>((List<?>) value));
            } else {
                set(path, value);
            }
        }
    }

    public void set(@NotNull Map<?, ?> map) {
        set(map, true);
    }

    public void set(@NotNull Map<?, ?> map, boolean copy) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            final String key = String.valueOf(entry.getKey());
            final Object value = entry.getValue();
            if (value instanceof Map) {
                createSection(key, (Map<?, ?>) value);
            } else if (copy && value instanceof List) {
                set(key, new ArrayList<>((List<?>) value));
            } else {
                set(key, value);
            }
        }
    }

    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        if (delegate == null) {
            super.set(path, value);
        } else {
            delegate.set(path, value);
        }
    }

    public void setComments(@NotNull String path, @Nullable List<String> comments) {
        if (delegate == null) {
            super.setComments(path, comments);
        } else {
            delegate.setComments(path, comments);
        }
    }

    public void setInlineComments(@NotNull String path, @Nullable List<String> comments) {
        if (delegate == null) {
            super.setInlineComments(path, comments);
        } else {
            delegate.setInlineComments(path, comments);
        }
    }

    public void merge(@NotNull ConfigurationSection section) {
        merge(section, getConfigurationSection());
    }

    @SuppressWarnings("unchecked")
    public static void merge(@NotNull ConfigurationSection from, @NotNull ConfigurationSection to) {
        for (String key : from.getKeys(false)) {
            final Object value = from.get(key);
            if (to.contains(key)) {
                final Object currentValue = to.get(key);
                if (currentValue instanceof ConfigurationSection) {
                    if (value instanceof ConfigurationSection) {
                        merge((ConfigurationSection) value, (ConfigurationSection) currentValue);
                    }
                } else if (value != null && currentValue instanceof List) {
                    for (Object o : IterableType.of(value)) {
                        try {
                            ((List<Object>) currentValue).add(o);
                        } catch (Throwable ignored) { }
                    }
                }
                continue;
            }
            if (value instanceof ConfigurationSection) {
                merge((ConfigurationSection) value, to.createSection(key));
            } else if (value instanceof List) {
                to.set(key, new ArrayList<>((List<?>) value));
            } else {
                to.set(key, value);
            }
        }
    }

    @NotNull
    @Contract("_ -> new")
    public BukkitSettings parse(@NotNull Function<String, String> function) {
        return parse(getConfigurationSection(), new BukkitSettings(), function);
    }

    @NotNull
    @Contract("_ -> new")
    public BukkitSettings parse(@NotNull BiFunction<String, String, String> function) {
        return parse(getConfigurationSection(), new BukkitSettings(), function);
    }

    @NotNull
    public static <T extends ConfigurationSection> T parse(@NotNull ConfigurationSection from, @NotNull T to, @NotNull Function<String, String> function) {
        return parse(from, to, (path, s) -> function.apply(s));
    }

    @NotNull
    public static <T extends ConfigurationSection> T parse(@NotNull ConfigurationSection from, @NotNull T to, @NotNull BiFunction<String, String, String> function) {
        for (String path : from.getKeys(true)) {
            to.set(path, parse(path, from.get(path), function));
        }
        return to;
    }

    @Nullable
    private static Object parse(@NotNull String path, @Nullable Object object, @NotNull BiFunction<String, String, String> function) {
        if (object instanceof String) {
            return function.apply(path, (String) object);
        } else if (object instanceof Iterable) {
            final List<Object> list = new ArrayList<>();
            int i = 0;
            for (Object o : IterableType.of(object)) {
                list.add(parse(path + "[" + i + "]", o, function));
                i++;
            }
            return list;
        } else if (object instanceof Map) {
            final Map<String, Object> map = new HashMap<>();
            for (var entry : ((Map<?, ?>) object).entrySet()) {
                final String key = String.valueOf(entry.getKey());
                map.put(key, parse(path + "." + key, entry.getValue(), function));
            }
            return map;
        } else if (object instanceof ConfigurationSection) {
            return parse((ConfigurationSection) object, new BukkitSettings(), function);
        } else {
            return object;
        }
    }

    @NotNull
    public Map<String, Object> asMap() {
        return asMap(getConfigurationSection());
    }

    @NotNull
    public static Map<String, Object> asMap(@NotNull ConfigurationSection section) {
        final Map<String, Object> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            final Object value = section.get(key);
            if (value instanceof ConfigurationSection) {
                map.put(key, asMap((ConfigurationSection) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }
}
