package com.saicone.picospacos.util.function;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public abstract class StringPredicate extends AnyPredicate<String> {

    public static final StringPredicate EMPTY = new StringPredicate() {
        @Override
        protected @NotNull Object getRaw() {
            return "";
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return actual.isEmpty();
        }
    };

    public static final StringPredicate BLANK = new StringPredicate() {
        @Override
        protected @NotNull Object getRaw() {
            return "";
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return actual.isBlank();
        }
    };

    public static final StringPredicate TRUE = new StringPredicate() {
        @Override
        protected @NotNull Object getRaw() {
            return "";
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return true;
        }
    };

    public static final StringPredicate FALSE = new StringPredicate() {
        @Override
        protected @NotNull Object getRaw() {
            return "";
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return false;
        }
    };

    @NotNull
    public static AnyPredicate<String> valueOf(@NotNull Object o) {
        return valueOf(o, false);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static AnyPredicate<String> valueOf(@NotNull Object o, boolean all) {
        if (o instanceof Iterable<?>) {
            return valueOf((Iterable<Object>) o, all);
        }
        return valueOf(String.valueOf(o));
    }

    @NotNull
    public static AnyPredicate<String> valueOf(@NotNull Iterable<Object> iterable) {
        return valueOf(iterable, false);
    }

    @NotNull
    public static AnyPredicate<String> valueOf(@NotNull Iterable<Object> iterable, boolean all) {
        final Set<AnyPredicate<String>> set = new HashSet<>();
        final EqualsSet equalsSet = new EqualsSet(all);
        final EqualsIgnoreCaseSet ignoreCaseSet = new EqualsIgnoreCaseSet(all);
        for (Object element : iterable) {
            final AnyPredicate<String> predicate = valueOf(element, true);
            if (predicate instanceof EqualsIgnoreCase) {
                ignoreCaseSet.add(((EqualsIgnoreCase) predicate).getValue());
            } else if (predicate instanceof Equals) {
                equalsSet.add(((Equals) predicate).getValue());
            } else {
                set.add(predicate);
            }
        }
        if (set.isEmpty() && equalsSet.isEmpty() && ignoreCaseSet.isEmpty()) {
            return EMPTY;
        }

        if (set.isEmpty()) {
            if (equalsSet.isEmpty()) {
                return ignoreCaseSet;
            } else if (ignoreCaseSet.isEmpty()) {
                return equalsSet;
            } else {
                set.add(equalsSet);
                set.add(ignoreCaseSet);
            }
        }
        if (!equalsSet.isEmpty()) {
            set.add(equalsSet.single());
        }
        if (!ignoreCaseSet.isEmpty()) {
            set.add(ignoreCaseSet.single());
        }

        if (set.size() == 1) {
            return set.iterator().next();
        }
        return new AnyPredicate.Multiple<>(set, all);
    }

    @NotNull
    public static StringPredicate valueOf(@NotNull String s) {
        final String[] split = s.split("[;:=]", 2);
        if (split.length > 1) {
            return valueOf(split[0], split[1]).orElseGet(() -> equal(s));
        }
        return equal(s);
    }

    @NotNull
    public static Optional<StringPredicate> valueOf(@NotNull String type, @NotNull String value) {
        switch (type
                .replace('-', '\0')
                .replace('_', '\0')
                .replace(' ', '\0')
                .toLowerCase()) {
            case "equals":
            case "same":
            case "exact":
                return Optional.of(equal(value));
            case "equalsignorecase":
            case "ignorecase":
            case "case":
                return Optional.of(equalsIgnoreCase(value));
            case "matches":
            case "regexp":
            case "regex":
            case "r":
            case "pattern":
            case "expression":
                return Optional.of(matches(value));
            case "contains":
            case "has":
                return Optional.of(contains(value));
            case "start":
            case "starts":
            case "startwith":
            case "startswith":
                return Optional.of(startsWith(value));
            case "end":
            case "ends":
            case "endwith":
            case "endswith":
                return Optional.of(endsWith(value));
            default:
                return Optional.empty();
        }
    }

    @NotNull
    public static StringPredicate equal(@NotNull String... s) {
        if (s.length < 1) {
            return EMPTY;
        } else if (s.length == 1) {
            if (s[0].isEmpty()) {
                return EMPTY;
            } else if (s[0].isBlank()) {
                return BLANK;
            }
            return new Equals(s[0]);
        }
        return new EqualsSet(Set.of(s), false);
    }

    @NotNull
    public static StringPredicate equal(@NotNull Collection<String> s, boolean all) {
        return new EqualsSet(new HashSet<>(s), all);
    }

    @NotNull
    public static StringPredicate equalsIgnoreCase(@NotNull String... s) {
        if (s.length < 1) {
            return EMPTY;
        } else if (s.length == 1) {
            if (s[0].isEmpty()) {
                return EMPTY;
            } else if (s[0].isBlank()) {
                return BLANK;
            }
            return new EqualsIgnoreCase(s[0]);
        }
        return new EqualsIgnoreCaseSet(Set.of(s), false);
    }

    @NotNull
    public static StringPredicate equalsIgnoreCase(@NotNull Collection<String> s, boolean all) {
        return new EqualsIgnoreCaseSet(new HashSet<>(s), all);
    }

    @NotNull
    public static StringPredicate matches(@NotNull @Language("RegExp") String regex) {
        return matches(Pattern.compile(regex));
    }

    @NotNull
    public static StringPredicate matches(@NotNull Pattern pattern) {
        return new Matches(pattern);
    }

    @NotNull
    public static StringPredicate contains(@NotNull String s) {
        return new Contains(s);
    }

    @NotNull
    public static StringPredicate startsWith(@NotNull String s) {
        return new StartsWith(s);
    }

    @NotNull
    public static StringPredicate endsWith(@NotNull String s) {
        return new EndsWith(s);
    }

    @Override
    protected String parse(@NotNull Object object) {
        return String.valueOf(object);
    }

    private static abstract class Simple extends StringPredicate {

        private final String value;

        public Simple(@NotNull String value) {
            this.value = value;
        }

        @NotNull
        public String getValue() {
            return value;
        }

        @Override
        protected @NotNull Object getRaw() {
            return value;
        }
    }

    private static abstract class Multiple extends StringPredicate {

        protected final Set<String> set;
        private final boolean all;

        public Multiple(boolean all) {
            this(new HashSet<>(), all);
        }

        public Multiple(@NotNull Set<String> set, boolean all) {
            this.set = set;
            this.all = all;
        }

        public boolean isEmpty() {
            return this.set.isEmpty();
        }

        public void add(@NotNull String value) {
            this.set.add(value);
        }

        @Override
        public abstract boolean testAny(@NotNull String s);

        @Override
        public boolean testAny(@NotNull String s, @NotNull Function<Object, Object> parser) {
            for (String element : this.set) {
                if (!compareAny(element, s)) {
                    if (all) {
                        return false;
                    }
                } else if (!all) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean testAnyElement(@NotNull Predicate<Object> predicate) {
            for (String element : this.set) {
                if (predicate.test(element)) {
                    return true;
                }
            }
            return false;
        }

        @NotNull
        public Set<String> getValue() {
            return set;
        }

        @Override
        protected @NotNull Object getRaw() {
            return set;
        }

        @NotNull
        public StringPredicate single() {
            if (set.size() == 1) {
                return single(set.iterator().next());
            }
            return this;
        }

        @NotNull
        protected abstract StringPredicate single(@NotNull String value);
    }

    static class Equals extends Simple {

        public Equals(@NotNull String value) {
            super(value);
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return base.equals(actual);
        }
    }

    private static class EqualsSet extends Multiple {

        public EqualsSet(boolean all) {
            super(all);
        }

        public EqualsSet(@NotNull Set<String> set, boolean all) {
            super(set, all);
        }

        @Override
        public boolean testAny(@NotNull String s) {
            return this.set.contains(s);
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return base.equals(actual);
        }

        @Override
        protected @NotNull StringPredicate single(@NotNull String value) {
            return new Equals(value);
        }
    }

    private static class EqualsIgnoreCase extends Simple {

        public EqualsIgnoreCase(@NotNull String value) {
            super(value);
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return base.equalsIgnoreCase(actual);
        }
    }

    private static class EqualsIgnoreCaseSet extends Multiple {

        public EqualsIgnoreCaseSet(boolean all) {
            super(all);
        }

        public EqualsIgnoreCaseSet(@NotNull Set<String> set, boolean all) {
            super(set, all);
        }

        @Override
        public boolean testAny(@NotNull String s) {
            return this.set.contains(s.toLowerCase());
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return base.equalsIgnoreCase(actual);
        }

        @Override
        protected @NotNull StringPredicate single(@NotNull String value) {
            return new EqualsIgnoreCase(value);
        }
    }

    private static class Matches extends StringPredicate {

        private final Pattern pattern;

        public Matches(@NotNull Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean testAny(@NotNull String s) {
            return pattern.matcher(s).matches();
        }

        @Override
        public boolean testAny(@NotNull String s, @NotNull Function<Object, Object> parser) {
            return testAny(s);
        }

        @Override
        protected @NotNull Object getRaw() {
            return pattern;
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            throw new IllegalStateException();
        }
    }

    private static class Contains extends Simple {

        public Contains(@NotNull String value) {
            super(value);
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return actual.contains(base);
        }
    }

    private static class StartsWith extends Equals {

        public StartsWith(@NotNull String value) {
            super(value);
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return actual.startsWith(base);
        }
    }

    private static class EndsWith extends Equals {

        public EndsWith(@NotNull String value) {
            super(value);
        }

        @Override
        protected boolean compareAny(@NotNull String base, @NotNull String actual) {
            return actual.endsWith(base);
        }
    }
}
