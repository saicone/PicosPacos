package com.saicone.picospacos.util.function;

import com.saicone.types.Types;
import com.saicone.types.parser.NumberParser;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public abstract class NumberPredicate extends AnyPredicate<Number> {

    private static final Map<NumberParser<? extends Number>, BiFunction<? super Number, ? super Number, Integer>> COMPARATORS = new LinkedHashMap<>();

    static {
        comparator(NumberParser.BIG_DECIMAL, BigDecimal::compareTo);
        comparator(NumberParser.BIG_INTEGER, BigInteger::compareTo);
        comparator(NumberParser.DOUBLE, Double::compare);
        comparator(NumberParser.FLOAT, Float::compare);
        comparator(NumberParser.LONG, Long::compare);
        comparator(NumberParser.INTEGER, Integer::compare);
        comparator(NumberParser.SHORT, Short::compare);
        comparator(NumberParser.BYTE, Byte::compare);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> void comparator(@NotNull NumberParser<? super T> parser, @NotNull BiFunction<? super T, ? super T, Integer> function) {
        COMPARATORS.put(parser, (BiFunction<? super Number, ? super Number, Integer>) function);
    }

    public static int compare(@NotNull Number n1, @NotNull Number n2) {
        for (var entry : COMPARATORS.entrySet()) {
            if (entry.getKey().isInstance(n1)) {
                if (entry.getKey().isInstance(n2)) {
                    return entry.getValue().apply(n1, n2);
                } else {
                    return entry.getValue().apply(n1, entry.getKey().cast(n2));
                }
            } else if (entry.getKey().isInstance(n2)) {
                return entry.getValue().apply(entry.getKey().cast(n1), n2);
            }
        }
        throw new IllegalArgumentException("Cannot compare " + n1 + " with " + n2);
    }

    @NotNull
    public static AnyPredicate<Number> valueOf(@NotNull Object value) {
        return valueOf(value, false);
    }

    @NotNull
    public static AnyPredicate<Number> valueOf(@NotNull Object value, boolean all) {
        if (value instanceof Number) {
            return equal(value);
        } else if (value instanceof Iterable) {
            final Set<AnyPredicate<Number>> set = new HashSet<>();
            for (Object o : (Iterable<?>) value) {
                set.add(valueOf(o, true));
            }
            return new Multiple<>(set, all);
        }
        final String s = String.valueOf(value).trim();
        if (s.startsWith("<=")) {
            return lessThanOrEqual(parse(s.substring(2)));
        } else if (s.startsWith("<")) {
            return lessThan(parse(s.substring(1)));
        } else if (s.startsWith(">=")) {
            return moreThanOrEqual(parse(s.substring(2)));
        } else if (s.startsWith(">")) {
            return moreThan(parse(s.substring(1)));
        } else {
            return equal(parse(s));
        }
    }

    @NotNull
    private static Object parse(@NotNull String s) {
        try {
            return NumberParser.NUMBER.parse(s);
        } catch (NumberFormatException e) {
            return s;
        }
    }

    @NotNull
    public static NumberPredicate valueOf(@NotNull Object value, @NotNull Predicate<Integer> predicate) {
        return new NumberPredicate(value) {
            @Override
            protected boolean isValid(int result) {
                return predicate.test(result);
            }
        };
    }

    @NotNull
    public static NumberPredicate lessThan(@NotNull Object value) {
        return new LessThan(value);
    }

    @NotNull
    public static NumberPredicate lessThanOrEqual(@NotNull Object value) {
        return new LessThanOrEqual(value);
    }

    @NotNull
    public static NumberPredicate equal(@NotNull Object value) {
        return new Equal(value);
    }

    @NotNull
    public static NumberPredicate moreThan(@NotNull Object value) {
        return new MoreThan(value);
    }

    @NotNull
    public static NumberPredicate moreThanOrEqual(@NotNull Object value) {
        return new MoreThanOrEqual(value);
    }

    private final Object number;

    public NumberPredicate(@NotNull Object number) {
        this.number = number;
    }

    @Override
    protected @NotNull Object getRaw() {
        return number;
    }

    @Override
    protected boolean compareAny(@NotNull Number base, @NotNull Number actual) {
        return isValid(compare(actual, base));
    }

    protected abstract boolean isValid(int result);

    @Override
    protected Number parse(@NotNull Object object) {
        return Types.NUMBER.parse(object);
    }

    private static class LessThan extends NumberPredicate {

        public LessThan(@NotNull Object number) {
            super(number);
        }

        @Override
        protected boolean isValid(int result) {
            return result < 0;
        }
    }

    private static class LessThanOrEqual extends NumberPredicate {

        public LessThanOrEqual(@NotNull Object number) {
            super(number);
        }

        @Override
        protected boolean isValid(int result) {
            return result <= 0;
        }
    }

    static class Equal extends NumberPredicate {

        public Equal(@NotNull Object number) {
            super(number);
        }

        @Override
        protected boolean isValid(int result) {
            return result == 0;
        }
    }

    private static class MoreThan extends NumberPredicate {

        public MoreThan(@NotNull Object number) {
            super(number);
        }

        @Override
        protected boolean isValid(int result) {
            return result > 0;
        }
    }

    private static class MoreThanOrEqual extends NumberPredicate {

        public MoreThanOrEqual(@NotNull Object number) {
            super(number);
        }

        @Override
        protected boolean isValid(int result) {
            return result >= 0;
        }
    }
}
