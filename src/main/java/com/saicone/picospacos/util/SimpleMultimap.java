package com.saicone.picospacos.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

public class SimpleMultimap<K, V, C extends Collection<V>> extends HashMap<K, C> {

    private final Supplier<C> constructor;

    public SimpleMultimap(@NotNull Supplier<C> constructor) {
        this.constructor = constructor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public C get(Object key) {
        C result = super.get(key);
        if (result == null) {
            result = constructor.get();
            put((K) key, result);
        }
        return result;
    }
}