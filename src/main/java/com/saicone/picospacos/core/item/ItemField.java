package com.saicone.picospacos.core.item;

import com.saicone.picospacos.util.function.AnyPredicate;
import com.saicone.picospacos.util.function.IterablePredicate;
import com.saicone.picospacos.util.function.MapPredicate;
import com.saicone.types.IterableType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ItemField<T> {

    @Nullable
    T get(@NotNull ItemStack item);

    void set(@NotNull ItemStack item, T t);

    @SuppressWarnings("unchecked")
    default boolean test(@NotNull ItemStack item, @NotNull AnyPredicate<?> predicate) {
        return ((AnyPredicate<Object>) predicate).test(get(item));
    }

    @SuppressWarnings("unchecked")
    default boolean test(@NotNull ItemStack item, @NotNull AnyPredicate<?> predicate, @NotNull Function<Object, Object> parser) {
        return ((AnyPredicate<Object>) predicate).test(get(item), parser);
    }

    @NotNull
    AnyPredicate<?> predicate(@NotNull Object object, @NotNull String... args);

    interface Iterable<E, T extends java.lang.Iterable<E>> extends ItemField<T> {

        @Override
        default @NotNull IterablePredicate<?> predicate(@NotNull Object object, @NotNull String... args) {
            if (args.length > 0) {
                return IterablePredicate.valueOf(args[0], IterableType.of(object), this::elementPredicate);
            }
            return IterablePredicate.exact(IterableType.of(object), this::elementPredicate);
        }

        @NotNull
        AnyPredicate<?> elementPredicate(@NotNull Object object);
    }

    interface MappedIterable<E, T extends java.lang.Iterable<E>> extends Iterable<E, T> {

        @NotNull
        Function<E, Object> mapper();

        @Override
        default boolean test(@NotNull ItemStack item, @NotNull AnyPredicate<?> predicate) {
            if (predicate instanceof IterablePredicate) {
                return test(item, (IterablePredicate<?>) predicate);
            }
            return Iterable.super.test(item, predicate);
        }

        @Override
        default boolean test(@NotNull ItemStack item, @NotNull AnyPredicate<?> predicate, @NotNull Function<Object, Object> parser) {
            if (predicate instanceof IterablePredicate) {
                return test(item, (IterablePredicate<?>) predicate, parser);
            }
            return Iterable.super.test(item, predicate, parser);
        }

        @SuppressWarnings("unchecked")
        default boolean test(@NotNull ItemStack item, @NotNull IterablePredicate<?> predicate) {
            final T value = get(item);
            if (value == null) {
                return false;
            }
            return ((IterablePredicate<Object>) predicate).testIterable(value, mapper());
        }

        @SuppressWarnings("unchecked")
        default boolean test(@NotNull ItemStack item, @NotNull IterablePredicate<?> predicate, @NotNull Function<Object, Object> parser) {
            final T value = get(item);
            if (value == null) {
                return false;
            }
            return ((IterablePredicate<Object>) predicate).testIterable(value, mapper(), parser);
        }
    }

    interface Map<K, V, T extends java.util.Map<K, V>> extends ItemField<T> {

        @Override
        default @NotNull MapPredicate<?, ?> predicate(@NotNull Object object, @NotNull String... args) {
            if (args.length > 0) {
                return MapPredicate.valueOf(args[0], (java.util.Map<?, ?>) object, this::keyPredicate, this::valuePredicate);
            }
            return MapPredicate.exact((java.util.Map<?, ?>) object, this::keyPredicate, this::valuePredicate);
        }

        @NotNull
        AnyPredicate<?> keyPredicate(@NotNull Object object);

        @NotNull
        AnyPredicate<?> valuePredicate(@NotNull Object object);
    }

    interface MappedMap<K, V, T extends java.util.Map<K, V>> extends Map<K, V, T> {

        @NotNull
        Function<K, Object> keyMapper();

        @NotNull
        Function<V, Object> valueMapper();

        @Override
        default boolean test(@NotNull ItemStack item, @NotNull AnyPredicate<?> predicate) {
            if (predicate instanceof MapPredicate) {
                return test(item, (MapPredicate<?, ?>) predicate);
            }
            return Map.super.test(item, predicate);
        }

        @Override
        default boolean test(@NotNull ItemStack item, @NotNull AnyPredicate<?> predicate, @NotNull Function<Object, Object> parser) {
            if (predicate instanceof MapPredicate) {
                return test(item, (MapPredicate<?, ?>) predicate, parser);
            }
            return Map.super.test(item, predicate, parser);
        }

        @SuppressWarnings("unchecked")
        default boolean test(@NotNull ItemStack item, @NotNull MapPredicate<?, ?> predicate) {
            final T value = get(item);
            if (value == null) {
                return false;
            }
            return ((MapPredicate<Object, Object>) predicate).testMap(value, keyMapper(), valueMapper());
        }

        @SuppressWarnings("unchecked")
        default boolean test(@NotNull ItemStack item, @NotNull MapPredicate<?, ?> predicate, @NotNull Function<Object, Object> parser) {
            final T value = get(item);
            if (value == null) {
                return false;
            }
            return ((MapPredicate<Object, Object>) predicate).testMap(value, keyMapper(), valueMapper(), parser);
        }
    }
}
