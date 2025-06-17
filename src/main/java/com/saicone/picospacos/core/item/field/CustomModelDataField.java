package com.saicone.picospacos.core.item.field;

import com.saicone.picospacos.core.item.ItemField;
import com.saicone.picospacos.util.function.AnyPredicate;
import com.saicone.picospacos.util.function.BooleanPredicate;
import com.saicone.picospacos.util.function.NumberPredicate;
import com.saicone.picospacos.util.function.StringPredicate;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class CustomModelDataField {

    @SuppressWarnings("deprecation")
    public static class Plain implements ItemField<Integer> {

        @Override
        public @NotNull AnyPredicate<?> predicate(@NotNull Object object, @NotNull String... args) {
            return NumberPredicate.valueOf(object);
        }

        @Override
        public @Nullable Integer get(@NotNull ItemStack item) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return null;
            }
            return meta.getCustomModelData();
        }

        @Override
        public void set(@NotNull ItemStack item, Integer data) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }
            meta.setCustomModelData(data);
            item.setItemMeta(meta);
        }
    }

    public static class Floats implements ItemField.Iterable<Float, List<Float>> {

        @Override
        public @NotNull AnyPredicate<?> elementPredicate(@NotNull Object object) {
            return NumberPredicate.valueOf(object);
        }

        @Override
        public @Nullable List<Float> get(@NotNull ItemStack item) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return null;
            }
            return meta.getCustomModelDataComponent().getFloats();
        }

        @Override
        public void set(@NotNull ItemStack item, List<Float> floats) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }
            meta.getCustomModelDataComponent().setFloats(floats);
            item.setItemMeta(meta);
        }
    }

    public static class Flags implements ItemField.Iterable<Boolean, List<Boolean>> {

        @Override
        public @NotNull AnyPredicate<?> elementPredicate(@NotNull Object object) {
            return BooleanPredicate.valueOf(object);
        }

        @Override
        public @Nullable List<Boolean> get(@NotNull ItemStack item) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return null;
            }
            return meta.getCustomModelDataComponent().getFlags();
        }

        @Override
        public void set(@NotNull ItemStack item, List<Boolean> flags) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }
            meta.getCustomModelDataComponent().setFlags(flags);
            item.setItemMeta(meta);
        }
    }

    public static class Strings implements ItemField.Iterable<String, List<String>> {

        @Override
        public @NotNull AnyPredicate<?> elementPredicate(@NotNull Object object) {
            return StringPredicate.valueOf(object);
        }

        @Override
        public @Nullable List<String> get(@NotNull ItemStack item) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return null;
            }
            return meta.getCustomModelDataComponent().getStrings();
        }

        @Override
        public void set(@NotNull ItemStack item, List<String> string) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }
            meta.getCustomModelDataComponent().setStrings(string);
            item.setItemMeta(meta);
        }
    }

    public static class Colors implements ItemField.MappedIterable<Color, List<Color>> {

        private static final Function<Color, Object> MAPPER = color -> color.getRed() + "," + color.getGreen() + "," + color.getBlue();

        @Override
        public @NotNull AnyPredicate<?> elementPredicate(@NotNull Object object) {
            return StringPredicate.valueOf(object);
        }

        @Override
        public @NotNull Function<Color, Object> mapper() {
            return MAPPER;
        }

        @Override
        public @Nullable List<Color> get(@NotNull ItemStack item) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return null;
            }
            return meta.getCustomModelDataComponent().getColors();
        }

        @Override
        public void set(@NotNull ItemStack item, List<Color> colors) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }
            meta.getCustomModelDataComponent().setColors(colors);
            item.setItemMeta(meta);
        }
    }
}
