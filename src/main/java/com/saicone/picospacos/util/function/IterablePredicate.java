package com.saicone.picospacos.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class IterablePredicate<E> extends AnyPredicate<Iterable<E>> {

    @NotNull
    private static <E> List<AnyPredicate<E>> build(@NotNull Iterable<?> iterable, @NotNull Function<Object, AnyPredicate<E>> supplier) {
        final List<AnyPredicate<E>> result = new ArrayList<>();
        for (Object element : iterable) {
            final AnyPredicate<E> predicate = supplier.apply(element);
            if (predicate != null) {
                result.add(predicate);
            }
        }
        return result;
    }

    @NotNull
    public static <E> IterablePredicate<E> any(@NotNull List<AnyPredicate<E>> list) {
        return new Any<>(list);
    }

    @NotNull
    public static <E> IterablePredicate<E> any(@NotNull Iterable<?> iterable, @NotNull Function<Object, AnyPredicate<E>> supplier) {
        return any(build(iterable, supplier));
    }

    @NotNull
    public static <E> IterablePredicate<E> contains(@NotNull List<AnyPredicate<E>> list) {
        return new Contains<>(list);
    }

    @NotNull
    public static <E> IterablePredicate<E> contains(@NotNull Iterable<?> iterable, @NotNull Function<Object, AnyPredicate<E>> supplier) {
        return contains(build(iterable, supplier));
    }

    @NotNull
    public static <E> IterablePredicate<E> exact(@NotNull List<AnyPredicate<E>> list) {
        return new Exact<>(list);
    }

    @NotNull
    public static <E> IterablePredicate<E> exact(@NotNull Iterable<?> iterable, @NotNull Function<Object, AnyPredicate<E>> supplier) {
        return exact(build(iterable, supplier));
    }

    protected final List<AnyPredicate<E>> list;

    public IterablePredicate(@NotNull List<AnyPredicate<E>> list) {
        this.list = list;
    }

    @Override
    public abstract boolean testAny(@NotNull Iterable<E> iterable);

    @Override
    public abstract boolean testAny(@NotNull Iterable<E> iterable, @NotNull Function<Object, Object> parser);

    public abstract <T> boolean testIterable(@NotNull Iterable<T> iterable, @NotNull Function<T, E> mapper);

    public abstract <T> boolean testIterable(@NotNull Iterable<T> iterable, @NotNull Function<T, E> mapper, @NotNull Function<Object, Object> parser);

    @Override
    public boolean testAnyElement(@NotNull Predicate<Object> predicate) {
        for (AnyPredicate<E> element : this.list) {
            if (element.testAnyElement(predicate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected @NotNull Object getRaw() {
        return list;
    }

    @Override
    protected boolean compareAny(@NotNull Iterable<E> base, @NotNull Iterable<E> actual) {
        throw new IllegalStateException();
    }

    @Override
    public @NotNull Iterable<E> getBase() {
        throw new IllegalStateException();
    }

    @Override
    public @NotNull Iterable<E> getBase(@NotNull Function<Object, Object> parser) {
        throw new IllegalStateException();
    }

    @NotNull
    public <T> AnyPredicate<Iterable<T>> mapping(@NotNull Function<T, E> mapper) {
        return new AnyPredicate.Delegate<>(this) {
            @Override
            public boolean testAny(@NotNull Iterable<T> iterable) {
                return IterablePredicate.this.testIterable(iterable, mapper);
            }

            @Override
            public boolean testAny(@NotNull Iterable<T> iterable, @NotNull Function<Object, Object> parser) {
                return IterablePredicate.this.testIterable(iterable, mapper, parser);
            }
        };
    }

    private static class Any<E> extends IterablePredicate<E> {

        public Any(@NotNull List<AnyPredicate<E>> list) {
            super(list);
        }

        private boolean testElement(@NotNull E element) {
            for (AnyPredicate<E> predicate : this.list) {
                if (predicate.testAny(element)) {
                    return true;
                }
            }
            return false;
        }

        private boolean testElement(@NotNull E element, @NotNull Function<Object, Object> parser) {
            for (AnyPredicate<E> predicate : this.list) {
                if (predicate.testAny(element, parser)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean testAny(@NotNull Iterable<E> iterable) {
            for (E element : iterable) {
                if (testElement(element)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean testAny(@NotNull Iterable<E> iterable, @NotNull Function<Object, Object> parser) {
            for (E element : iterable) {
                if (testElement(element, parser)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <T> boolean testIterable(@NotNull Iterable<T> iterable, @NotNull Function<T, E> mapper) {
            for (T element : iterable) {
                if (testElement(mapper.apply(element))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <T> boolean testIterable(@NotNull Iterable<T> iterable, @NotNull Function<T, E> mapper, @NotNull Function<Object, Object> parser) {
            for (T element : iterable) {
                if (testElement(mapper.apply(element), parser)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class Contains<E> extends IterablePredicate<E> {

        public Contains(@NotNull List<AnyPredicate<E>> list) {
            super(list);
        }

        @Override
        public boolean testAny(@NotNull Iterable<E> iterable) {
            int index = 0;
            for (E element : iterable) {
                if (index < this.list.size()) {
                    if (this.list.get(index).testAny(element)) {
                        index++;
                    } else {
                        index = 0;
                    }
                } else {
                    break;
                }
            }
            return index >= this.list.size();
        }

        @Override
        public boolean testAny(@NotNull Iterable<E> iterable, @NotNull Function<Object, Object> parser) {
            int index = 0;
            for (E element : iterable) {
                if (index < this.list.size()) {
                    if (this.list.get(index).testAny(element, parser)) {
                        index++;
                    } else {
                        index = 0;
                    }
                } else {
                    break;
                }
            }
            return index >= this.list.size();
        }

        @Override
        public <T> boolean testIterable(@NotNull Iterable<T> iterable, @NotNull Function<T, E> mapper) {
            int index = 0;
            for (T element : iterable) {
                if (index < this.list.size()) {
                    if (this.list.get(index).testAny(mapper.apply(element))) {
                        index++;
                    } else {
                        index = 0;
                    }
                } else {
                    break;
                }
            }
            return index >= this.list.size();
        }

        @Override
        public <T> boolean testIterable(@NotNull Iterable<T> iterable, @NotNull Function<T, E> mapper, @NotNull Function<Object, Object> parser) {
            int index = 0;
            for (T element : iterable) {
                if (index < this.list.size()) {
                    if (this.list.get(index).testAny(mapper.apply(element), parser)) {
                        index++;
                    } else {
                        index = 0;
                    }
                } else {
                    break;
                }
            }
            return index >= this.list.size();
        }
    }

    private static class Exact<E> extends IterablePredicate<E> {

        public Exact(@NotNull List<AnyPredicate<E>> list) {
            super(list);
        }

        @Override
        public boolean testAny(@NotNull Iterable<E> iterable) {
            if (iterable instanceof Collection && this.list.size() != ((Collection<E>) iterable).size()) {
                return false;
            }
            int index = 0;
            for (E element : iterable) {
                if (index < this.list.size() && this.list.get(index).testAny(element)) {
                    index++;
                } else {
                    return false;
                }
            }
            return index >= this.list.size();
        }

        @Override
        public boolean testAny(@NotNull Iterable<E> iterable, @NotNull Function<Object, Object> parser) {
            if (iterable instanceof Collection && this.list.size() != ((Collection<E>) iterable).size()) {
                return false;
            }
            int index = 0;
            for (E element : iterable) {
                if (index < this.list.size() && this.list.get(index).testAny(element, parser)) {
                    index++;
                } else {
                    return false;
                }
            }
            return index >= this.list.size();
        }

        @Override
        public <T> boolean testIterable(@NotNull Iterable<T> iterable, @NotNull Function<T, E> mapper) {
            if (iterable instanceof Collection && this.list.size() != ((Collection<T>) iterable).size()) {
                return false;
            }
            int index = 0;
            for (T element : iterable) {
                if (index < this.list.size() && this.list.get(index).testAny(mapper.apply(element))) {
                    index++;
                } else {
                    return false;
                }
            }
            return index >= this.list.size();
        }

        @Override
        public <T> boolean testIterable(@NotNull Iterable<T> iterable, @NotNull Function<T, E> mapper, @NotNull Function<Object, Object> parser) {
            if (iterable instanceof Collection && this.list.size() != ((Collection<T>) iterable).size()) {
                return false;
            }
            int index = 0;
            for (T element : iterable) {
                if (index < this.list.size() && this.list.get(index).testAny(mapper.apply(element), parser)) {
                    index++;
                } else {
                    return false;
                }
            }
            return index >= this.list.size();
        }
    }
}
