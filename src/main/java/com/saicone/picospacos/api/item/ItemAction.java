package com.saicone.picospacos.api.item;

import com.saicone.picospacos.module.settings.BukkitSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public interface ItemAction {

    @NotNull
    static ItemAction empty() {
        return new ItemAction() {
            @Override
            public boolean isEmpty() {
                return true;
            }
        };
    }

    default boolean isEmpty() {
        return false;
    }

    default void execute(@NotNull ItemHolder holder) {
        // empty default method
    }

    default boolean test(@NotNull ItemHolder holder) {
        return true;
    }

    @NotNull
    default ActionResult apply(@NotNull ItemHolder holder) {
        if (!test(holder)) {
            return ActionResult.BREAK;
        }
        execute(holder);
        return ActionResult.DONE;
    }

    class Builder<T extends ItemAction> {

        private final String id;
        private final Pattern pattern;

        private String defaultKey = "value";
        private BuilderFunction<T> accept;

        public Builder(@NotNull String id) {
            this(id, (Pattern) null);
        }

        public Builder(@NotNull String id, @NotNull @Language("RegExp") String pattern) {
            this(id, Pattern.compile(pattern));
        }

        public Builder(@NotNull String id, @Nullable Pattern pattern) {
            this.id = id;
            this.pattern = pattern;
        }

        public boolean matches(@NotNull String key) {
            return key.equalsIgnoreCase(id) || (pattern != null && pattern.matcher(key).matches());
        }

        @NotNull
        public String id() {
            return id;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<T> defaultKey(@NotNull String defaultKey) {
            this.defaultKey = defaultKey;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<T> provides(@NotNull Supplier<T> supplier) {
            return accept((id, config) -> supplier.get());
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<T> accept(@NotNull Function<BukkitSettings, T> function) {
            return accept((id, config) -> function.apply(config));
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<T> accept(@NotNull BuilderFunction<T> function) {
            this.accept = function;
            return this;
        }

        @NotNull
        public T build(@NotNull String id, @Nullable Object object) throws IllegalArgumentException {
            final BukkitSettings config;
            if (object instanceof BukkitSettings) {
                config = (BukkitSettings) object;
            } else if (object instanceof ConfigurationSection || object instanceof Map) {
                config = BukkitSettings.of(object);
            } else {
                config = new BukkitSettings();
                config.set(defaultKey, object);
            }
            try {
                return accept.apply(id, config);
            } catch (Throwable t) {
                throw new IllegalArgumentException("Cannot convert the provided object to required action type", t);
            }
        }
    }

    @FunctionalInterface
    interface BuilderFunction<T extends  ItemAction> extends BiFunction<String, BukkitSettings, T> {

        @Override
        T apply(String key, BukkitSettings config);
    }
}
