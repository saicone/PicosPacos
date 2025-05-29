package com.saicone.picospacos.util.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class MapPredicate<K, V> extends AnyPredicate<Map<K, V>> {

    @NotNull
    private static <K, V> Map<Object, AnyPredicate<V>> build(@NotNull Map<?, ?> map, @NotNull Function<Object, AnyPredicate<K>> keySupplier, @NotNull Function<Object, AnyPredicate<V>> valueSupplier) {
        final Map<Object, AnyPredicate<V>> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = keySupplier.apply(entry.getKey());
            if (key == null) {
                continue;
            } else if (key instanceof StringPredicate.Equals || key instanceof NumberPredicate.Equal) {
                key = ((AnyPredicate<?>) key).getRaw();
            }
            final AnyPredicate<V> valuePredicate = valueSupplier.apply(entry.getValue());
            if (valuePredicate != null) {
                result.put(key, valuePredicate);
            }
        }
        return result;
    }

    @NotNull
    public static <K, V> MapPredicate<K, V> any(@NotNull Map<Object, AnyPredicate<V>> map) {
        return new Any<>(map);
    }

    @NotNull
    public static <K, V> MapPredicate<K, V> any(@NotNull Map<?, ?> map, @NotNull Function<Object, AnyPredicate<K>> keySupplier, @NotNull Function<Object, AnyPredicate<V>> valueSupplier) {
        return any(build(map, keySupplier, valueSupplier));
    }

    @NotNull
    public static <K, V> MapPredicate<K, V> contains(@NotNull Map<Object, AnyPredicate<V>> map) {
        return new Contains<>(map);
    }

    @NotNull
    public static <K, V> MapPredicate<K, V> contains(@NotNull Map<?, ?> map, @NotNull Function<Object, AnyPredicate<K>> keySupplier, @NotNull Function<Object, AnyPredicate<V>> valueSupplier) {
        return contains(build(map, keySupplier, valueSupplier));
    }

    @NotNull
    public static <K, V> MapPredicate<K, V> exact(@NotNull Map<Object, AnyPredicate<V>> map) {
        return new Exact<>(map);
    }

    @NotNull
    public static <K, V> MapPredicate<K, V> exact(@NotNull Map<?, ?> map, @NotNull Function<Object, AnyPredicate<K>> keySupplier, @NotNull Function<Object, AnyPredicate<V>> valueSupplier) {
        return exact(build(map, keySupplier, valueSupplier));
    }

    protected final Map<Object, AnyPredicate<V>> map;

    public MapPredicate(@NotNull Map<Object, AnyPredicate<V>> map) {
        this.map = map;
    }

    @Override
    public abstract boolean testAny(@NotNull Map<K, V> map);

    @Override
    public abstract boolean testAny(@NotNull Map<K, V> map, @NotNull Function<Object, Object> parser);

    public abstract <A, B> boolean testMap(@NotNull Map<A, B> map, @NotNull Function<A, K> keyMapper, @NotNull Function<B, V> valueMapper);

    public abstract <A, B> boolean testMap(@NotNull Map<A, B> map, @NotNull Function<A, K> keyMapper, @NotNull Function<B, V> valueMapper, @NotNull Function<Object, Object> parser);

    @Override
    @SuppressWarnings("unchecked")
    public boolean testAnyElement(@NotNull Predicate<Object> predicate) {
        for (Map.Entry<Object, AnyPredicate<V>> entry : map.entrySet()) {
            if (entry.getKey() instanceof AnyPredicate) {
                if (((AnyPredicate<K>) entry.getKey()).testAnyElement(predicate)) {
                    return true;
                }
            } else if (predicate.test(entry.getKey())) {
                return true;
            }
            if (entry.getValue().testAnyElement(predicate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected @NotNull Object getRaw() {
        return map;
    }

    @Override
    protected boolean compareAny(@NotNull Map<K, V> base, @NotNull Map<K, V> actual) {
        throw new IllegalStateException();
    }

    @Override
    public @NotNull Map<K, V> getBase() {
        throw new IllegalStateException();
    }

    @Override
    public @NotNull Map<K, V> getBase(@NotNull Function<Object, Object> parser) {
        throw new IllegalStateException();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected V getValue(@NotNull Map<K, V> map, @NotNull Object key) {
        if (key instanceof AnyPredicate) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (((AnyPredicate<K>) key).testAny(entry.getKey())) {
                    return entry.getValue();
                }
            }
            return null;
        } else {
            return map.get(key);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected V getValue(@NotNull Map<K, V> map, @NotNull Object key, @NotNull Function<Object, Object> parser) {
        if (key instanceof AnyPredicate) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (((AnyPredicate<K>) key).testAny(entry.getKey(), parser)) {
                    return entry.getValue();
                }
            }
            return null;
        } else {
            return map.get(key);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <A, B> B getValue(@NotNull Map<A, B> map, @NotNull Function<A, K> keyMapper, @NotNull Object key) {
        if (key instanceof AnyPredicate) {
            for (Map.Entry<A, B> entry : map.entrySet()) {
                if (((AnyPredicate<K>) key).testAny(keyMapper.apply(entry.getKey()))) {
                    return entry.getValue();
                }
            }
        } else {
            for (Map.Entry<A, B> entry : map.entrySet()) {
                if (key.equals(keyMapper.apply(entry.getKey()))) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <A, B> B getValue(@NotNull Map<A, B> map, @NotNull Function<A, K> keyMapper, @NotNull Object key, @NotNull Function<Object, Object> parser) {
        if (key instanceof AnyPredicate) {
            for (Map.Entry<A, B> entry : map.entrySet()) {
                if (((AnyPredicate<K>) key).testAny(keyMapper.apply(entry.getKey()), parser)) {
                    return entry.getValue();
                }
            }
        } else {
            for (Map.Entry<A, B> entry : map.entrySet()) {
                if (key.equals(keyMapper.apply(entry.getKey()))) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private static class Any<K, V> extends MapPredicate<K, V> {

        public Any(@NotNull Map<Object, AnyPredicate<V>> map) {
            super(map);
        }

        @Override
        public boolean testAny(@NotNull Map<K, V> map) {
            for (Map.Entry<Object, AnyPredicate<V>> entry : this.map.entrySet()) {
                final V value = getValue(map, entry.getKey());
                if (value != null && entry.getValue().testAny(value)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean testAny(@NotNull Map<K, V> map, @NotNull Function<Object, Object> parser) {
            for (Map.Entry<Object, AnyPredicate<V>> entry : this.map.entrySet()) {
                final V value = getValue(map, entry.getKey(), parser);
                if (value != null && entry.getValue().testAny(value, parser)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <A, B> boolean testMap(@NotNull Map<A, B> map, @NotNull Function<A, K> keyMapper, @NotNull Function<B, V> valueMapper) {
            for (Map.Entry<Object, AnyPredicate<V>> entry : this.map.entrySet()) {
                final B value = getValue(map, keyMapper, entry.getKey());
                if (value != null && entry.getValue().testAny(valueMapper.apply(value))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <A, B> boolean testMap(@NotNull Map<A, B> map, @NotNull Function<A, K> keyMapper, @NotNull Function<B, V> valueMapper, @NotNull Function<Object, Object> parser) {
            for (Map.Entry<Object, AnyPredicate<V>> entry : this.map.entrySet()) {
                final B value = getValue(map, keyMapper, entry.getKey(), parser);
                if (value != null && entry.getValue().testAny(valueMapper.apply(value), parser)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class Contains<K, V> extends MapPredicate<K, V> {

        public Contains(@NotNull Map<Object, AnyPredicate<V>> map) {
            super(map);
        }

        @Override
        public boolean testAny(@NotNull Map<K, V> map) {
            for (Map.Entry<Object, AnyPredicate<V>> entry : this.map.entrySet()) {
                final V value = getValue(map, entry.getKey());
                if (value == null || !entry.getValue().testAny(value)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean testAny(@NotNull Map<K, V> map, @NotNull Function<Object, Object> parser) {
            for (Map.Entry<Object, AnyPredicate<V>> entry : this.map.entrySet()) {
                final V value = getValue(map, entry.getKey(), parser);
                if (value == null || !entry.getValue().testAny(value, parser)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public <A, B> boolean testMap(@NotNull Map<A, B> map, @NotNull Function<A, K> keyMapper, @NotNull Function<B, V> valueMapper) {
            for (Map.Entry<Object, AnyPredicate<V>> entry : this.map.entrySet()) {
                final B value = getValue(map, keyMapper, entry.getKey());
                if (value == null || !entry.getValue().testAny(valueMapper.apply(value))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public <A, B> boolean testMap(@NotNull Map<A, B> map, @NotNull Function<A, K> keyMapper, @NotNull Function<B, V> valueMapper, @NotNull Function<Object, Object> parser) {
            for (Map.Entry<Object, AnyPredicate<V>> entry : this.map.entrySet()) {
                final B value = getValue(map, keyMapper, entry.getKey(), parser);
                if (value == null || !entry.getValue().testAny(valueMapper.apply(value), parser)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class Exact<K, V> extends Contains<K, V> {

        public Exact(@NotNull Map<Object, AnyPredicate<V>> map) {
            super(map);
        }

        @Override
        public boolean testAny(@NotNull Map<K, V> map) {
            if (this.map.size() != map.size()) {
                return false;
            }
            return super.testAny(map);
        }

        @Override
        public boolean testAny(@NotNull Map<K, V> map, @NotNull Function<Object, Object> parser) {
            if (this.map.size() != map.size()) {
                return false;
            }
            return super.testAny(map, parser);
        }

        @Override
        public <A, B> boolean testMap(@NotNull Map<A, B> map, @NotNull Function<A, K> keyMapper, @NotNull Function<B, V> valueMapper) {
            if (this.map.size() != map.size()) {
                return false;
            }
            return super.testMap(map, keyMapper, valueMapper);
        }

        @Override
        public <A, B> boolean testMap(@NotNull Map<A, B> map, @NotNull Function<A, K> keyMapper, @NotNull Function<B, V> valueMapper, @NotNull Function<Object, Object> parser) {
            if (this.map.size() != map.size()) {
                return false;
            }
            return super.testMap(map, keyMapper, valueMapper, parser);
        }
    }
}
