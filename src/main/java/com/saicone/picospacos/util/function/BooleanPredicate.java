package com.saicone.picospacos.util.function;

import com.saicone.types.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BooleanPredicate extends AnyPredicate<Boolean> {

    @NotNull
    public static AnyPredicate<Boolean> valueOf(@NotNull Object o) {
        return valueOf(o, false);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static AnyPredicate<Boolean> valueOf(@NotNull Object o, boolean all) {
        if (o instanceof Iterable<?>) {
            return valueOf((Iterable<Object>) o, all);
        }
        return new BooleanPredicate(o);
    }

    @NotNull
    public static AnyPredicate<Boolean> valueOf(@NotNull Iterable<Object> iterable) {
        return valueOf(iterable, false);
    }

    @NotNull
    public static AnyPredicate<Boolean> valueOf(@NotNull Iterable<Object> iterable, boolean all) {
        final Set<AnyPredicate<Boolean>> set = new HashSet<>();
        for (Object element : iterable) {
            set.add(valueOf(element, true));
        }
        if (set.isEmpty()) {
            return AnyPredicate.empty();
        }

        if (set.size() == 1) {
            return set.iterator().next();
        }
        return new AnyPredicate.Multiple<>(set, all);
    }

    private final Object value;

    public BooleanPredicate(@NotNull Object value) {
        this.value = value;
    }

    @Override
    protected @NotNull Object getRaw() {
        return value;
    }

    @Override
    protected boolean compareAny(@NotNull Boolean base, @NotNull Boolean actual) {
        return base == actual;
    }

    @Override
    protected Boolean parse(@NotNull Object object) {
        return Types.BOOLEAN.parse(object);
    }
}
