package com.saicone.picospacos.util.function;

import com.saicone.types.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AnyPredicate<T> implements Predicate<T> {

    private static final AnyPredicate<?> EMPTY = new AnyPredicate<>() {
        @Override
        protected @NotNull Object getRaw() {
            return null;
        }

        @Override
        protected boolean compareAny(@NotNull Object base, @NotNull Object actual) {
            return false;
        }
    };
    private static final AnyPredicate<?> EXIST_TRUE = new AnyPredicate<>() {
        @Override
        protected @NotNull Object getRaw() {
            return null;
        }

        @Override
        public boolean test(@Nullable Object t) {
            return t != null;
        }

        @Override
        public boolean test(@Nullable Object t, @NotNull Function<Object, Object> parser) {
            return t != null;
        }

        @Override
        protected boolean compareAny(@NotNull Object base, @NotNull Object actual) {
            return true;
        }
    };
    private static final AnyPredicate<?> EXIST_FALSE = new AnyPredicate<>() {
        @Override
        protected @NotNull Object getRaw() {
            return null;
        }

        @Override
        public boolean test(@Nullable Object t) {
            return t == null;
        }

        @Override
        public boolean test(@Nullable Object t, @NotNull Function<Object, Object> parser) {
            return t == null;
        }

        @Override
        protected boolean compareAny(@NotNull Object base, @NotNull Object actual) {
            return true;
        }
    };

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> AnyPredicate<T> empty() {
        return (AnyPredicate<T>) EMPTY;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> AnyPredicate<T> exist(@Nullable Object exist) {
        final boolean check = Types.BOOLEAN.parseOrDefault(exist, true);
        return check ? (AnyPredicate<T>) EXIST_TRUE : (AnyPredicate<T>) EXIST_FALSE;
    }

    @Override
    public boolean test(@Nullable T t) {
        return t != null && testAny(t);
    }

    public boolean test(@Nullable T t, @NotNull Function<Object, Object> parser) {
        return t != null && testAny(t, parser);
    }

    public boolean testAny(@NotNull T t) {
        return compareAny(getBase(), t);
    }

    public boolean testAny(@NotNull T t, @NotNull Function<Object, Object> parser) {
        return compareAny(getBase(parser), t);
    }

    public boolean testAnyElement(@NotNull Predicate<Object> predicate) {
        return predicate.test(getRaw());
    }

    @NotNull
    protected abstract Object getRaw();

    protected abstract boolean compareAny(@NotNull T base, @NotNull T actual);

    @NotNull
    @SuppressWarnings("unchecked")
    public T getBase() {
        return (T) getRaw();
    }

    @NotNull
    public T getBase(@NotNull Function<Object, Object> parser) {
        return parse(parser.apply(getRaw()));
    }

    @SuppressWarnings("unchecked")
    protected T parse(@NotNull Object object) {
        return (T) object;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!getClass().isInstance(o)) return false;
        if (!super.equals(o)) return false;

        AnyPredicate<?> that = (AnyPredicate<?>) o;
        return getRaw().equals(that.getRaw());
    }

    @Override
    public int hashCode() {
        return getRaw().hashCode();
    }

    public static class Delegate<T> extends AnyPredicate<T> {

        private final AnyPredicate<?> delegate;

        public Delegate(AnyPredicate<?> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean testAnyElement(@NotNull Predicate<Object> predicate) {
            return delegate.testAnyElement(predicate);
        }

        @Override
        protected @NotNull Object getRaw() {
            return delegate.getRaw();
        }

        @Override
        protected boolean compareAny(@NotNull T base, @NotNull T actual) {
            throw new IllegalStateException();
        }
    }

    public static class Multiple<T> extends AnyPredicate<T> {

        private final Set<AnyPredicate<T>> set;
        private final boolean all;

        public Multiple(@NotNull Set<AnyPredicate<T>> set, boolean all) {
            this.set = set;
            this.all = all;
        }

        @Override
        public boolean testAny(@NotNull T t) {
            for (AnyPredicate<T> element : this.set) {
                if (!element.testAny(t)) {
                    if (all) {
                        return false;
                    }
                } else if (!all) {
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean testAny(@NotNull T t, @NotNull Function<Object, Object> parser) {
            for (AnyPredicate<T> element : this.set) {
                if (!element.testAny(t, parser)) {
                    if (all) {
                        return false;
                    }
                } else if (!all) {
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean testAnyElement(@NotNull Predicate<Object> predicate) {
            for (AnyPredicate<T> element : this.set) {
                if (element.testAnyElement(predicate)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected @NotNull Object getRaw() {
            return set;
        }

        @Override
        protected boolean compareAny(@NotNull T base, @NotNull T actual) {
            throw new IllegalStateException();
        }

        @Override
        public @NotNull T getBase() {
            throw new IllegalStateException();
        }

        @Override
        public @NotNull T getBase(@NotNull Function<Object, Object> parser) {
            throw new IllegalStateException();
        }
    }
}
