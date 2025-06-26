package com.saicone.picospacos.core.item;

import com.google.common.base.Suppliers;
import com.saicone.picospacos.api.PicosPacosAPI;
import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.api.item.ItemPredicate;
import com.saicone.picospacos.core.item.field.AmountField;
import com.saicone.picospacos.core.item.field.CustomModelDataField;
import com.saicone.picospacos.core.item.field.DisplayNameField;
import com.saicone.picospacos.core.item.field.DurabilityField;
import com.saicone.picospacos.core.item.field.EnchantmentsField;
import com.saicone.picospacos.core.item.field.FlagsField;
import com.saicone.picospacos.core.item.field.LoreField;
import com.saicone.picospacos.core.item.field.MaterialField;
import com.saicone.picospacos.core.item.field.NbtField;
import com.saicone.picospacos.module.settings.BukkitSettings;
import com.saicone.picospacos.util.Strings;
import com.saicone.picospacos.util.function.AnyPredicate;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

public class ItemFields {

    private static final String NBT_PATH = "nbt";

    private static final Map<String[], ItemField<?>> REGISTRY = new TreeMap<>(Comparator.comparingInt((String[] arr) -> arr.length).thenComparing(Arrays::compare));

    public static final ItemField<Integer> AMOUNT = register("amount", new AmountField());
    public static final ItemField<Integer> CUSTOM_MODEL_DATA = register("custom_model_data", new CustomModelDataField.Plain());
    public static final ItemField.Iterable<Float, List<Float>> CUSTOM_MODEL_DATA_FLOATS = register("custom_model_data.floats", new CustomModelDataField.Floats());
    public static final ItemField.Iterable<Boolean, List<Boolean>> CUSTOM_MODEL_DATA_FLAGS = register("custom_model_data.flags", new CustomModelDataField.Flags());
    public static final ItemField.Iterable<String, List<String>> CUSTOM_MODEL_DATA_STRINGS = register("custom_model_data.strings", new CustomModelDataField.Strings());
    public static final ItemField.MappedIterable<Color, List<Color>> CUSTOM_MODEL_DATA_COLORS = register("custom_model_data.colors", new CustomModelDataField.Colors());
    public static final ItemField<String> DISPLAY_NAME = register("(display_)?name", new DisplayNameField());
    public static final ItemField<Short> DURABILITY = register("durability", new DurabilityField());
    public static final ItemField.MappedMap<Enchantment, Integer, Map<Enchantment, Integer>> ENCHANTMENTS = register("enchant(ment)?s", new EnchantmentsField());
    public static final ItemField.MappedIterable<ItemFlag, Set<ItemFlag>> FLAGS = register("flags", new FlagsField());
    public static final ItemField.Iterable<String, List<String>> LORE = register("lore", new LoreField());
    public static final ItemField<String> MATERIAL = register("material|type", new MaterialField());
    public static final ItemField<String> MATERIAL_XSERIES = register("material|type.xseries", new MaterialField.XSeries());

    @NotNull
    public static <T, F extends ItemField<T>> F register(@NotNull @Language("RegExp") String key, @NotNull F field) {
        key = key.replace("_", "[-_ ]?"); // match separators
        final String[] path = key.split("\\.");
        for (int i = 0; i < path.length; i++) {
            String s = path[i];
            s = "(?i)" + s; // case insensitive
            if (i + 1 >= path.length) {
                s = "(" + s + ")(?:\\(.*\\))?"; // ignore args
            }
            path[i] = s;
        }
        REGISTRY.put(path, field);
        return field;
    }

    @NotNull
    public static Optional<ItemPredicate> predicate(@NotNull BukkitSettings settings) {
        final List<FieldPredicate> list = new ArrayList<>() {
            @Override
            public boolean contains(Object o) {
                if (o instanceof String[]) {
                    for (FieldPredicate predicate : this) {
                        if (Arrays.equals((String[]) o, predicate.path())) {
                            return true;
                        }
                    }
                    return false;
                }
                return super.contains(o);
            }
        };
        // Load registry fields
        for (Map.Entry<String[], ItemField<?>> entry : REGISTRY.entrySet()) {
            // Get by entry key (regex path)
            final Map.Entry<String[], Object> result = settings.getRegexEntry(entry.getKey());
            Object value = result.getValue();
            if (value == null) {
                continue;
            }
            // Normalize value
            if (value instanceof ConfigurationSection) {
                value = BukkitSettings.of(value).asMap();
            }
            // Load predicate (and also extract arguments)
            final AnyPredicate<?> predicate = entry.getValue().predicate(value, args(result.getKey()));
            // Append predicate
            list.add(new FieldPredicate(result.getKey(), entry.getValue(), predicate));
        }

        // Load NBT fields
        final BukkitSettings nbt = settings.getConfigurationSection(config -> config.getIgnoreCase(NBT_PATH));
        if (nbt != null) {
            for (String key : nbt.getKeys(path -> path.contains("(") && path.endsWith(")"))) {
                final String[] path = array(NBT_PATH, key.split("\\."));
                if (list.contains(path)) {
                    // Already loaded by some external field
                    continue;
                }

                Object value = nbt.get(key);
                if (value == null) {
                    continue;
                }
                // Normalize value
                if (value instanceof ConfigurationSection) {
                    value = BukkitSettings.of(value).asMap();
                }

                final NbtField field = NbtField.valueOf((Object[]) key.substring(0, key.indexOf('(')).split("\\."));

                list.add(new FieldPredicate(path, field, field.predicate(value, args(key))));
            }
        }

        if (list.isEmpty()) {
            return Optional.empty();
        } else if (list.size() == 1) {
            return Optional.of(list.get(0));
        } else {
            return Optional.of(holder -> {
                for (FieldPredicate predicate : list) {
                    if (!predicate.test(holder)) {
                        return false;
                    }
                }
                return true;
            });
        }
    }

    private static String[] array(@NotNull String first, @NotNull String[] array) {
        final String[] result = new String[array.length + 1];
        result[0] = first;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }

    @NotNull
    private static String[] args(@NotNull String[] path) {
        return args(path[path.length - 1]);
    }

    @NotNull
    private static String[] args(@NotNull String last) {
        // key(arg1, "arg 2", arg3... etc)
        final int index = last.indexOf('(');
        if (index < 0 || last.charAt(last.length() - 1) != ')') {
            return new String[0];
        }
        final String s = last.substring(index + 1, last.length() - 1).trim();
        if (s.isEmpty()) {
            return new String[0];
        }
        final List<String> args = new ArrayList<>();
        int start = -1;
        boolean skip = false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == ',') {
                if (skip) continue;
                args.add(s.substring(Math.min(start, 0), i).trim());
                start = -1;
            } else if (start < 0) {
                if (c == '"' || c == '\'' || c == '`') {
                    final int finish = Strings.escapeIndexOf(s, c, i + 1);
                    if (finish >= 0) {
                        args.add(s.substring(i + 1, finish).replace("\\" + c, String.valueOf(c)));
                        skip = true;
                        continue;
                    }
                }
                start = i;
            }
        }
        return args.toArray(new String[0]);
    }

    private static final class FieldPredicate implements ItemPredicate {

        private final String[] path;
        private final ItemField<?> field;
        private final AnyPredicate<?> predicate;
        private final Supplier<Boolean> parse; // lazy init

        private FieldPredicate(@NotNull String[] path, @NotNull ItemField<?> field, @NotNull AnyPredicate<?> predicate) {
            this.path = path;
            this.field = field;
            this.predicate = predicate;
            this.parse = Suppliers.memoize(() -> predicate.testAnyElement(object -> object instanceof String && PicosPacosAPI.isParsable((String) object)));
        }

        @NotNull
        public String[] path() {
            return path;
        }

        @NotNull
        public ItemField<?> field() {
            return field;
        }

        @NotNull
        public AnyPredicate<?> predicate() {
            return predicate;
        }

        public boolean parse() {
            return parse.get();
        }

        @Override
        public boolean test(@NotNull ItemHolder holder) {
            if (parse()) {
                return field.test(holder.getItem(), predicate, holder);
            } else {
                return field.test(holder.getItem(), predicate);
            }
        }
    }
}
