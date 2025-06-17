package com.saicone.picospacos.core.item.field;

import com.saicone.picospacos.core.item.ItemField;
import com.saicone.picospacos.util.Strings;
import com.saicone.picospacos.util.function.AnyPredicate;
import com.saicone.picospacos.util.function.BooleanPredicate;
import com.saicone.picospacos.util.function.IterablePredicate;
import com.saicone.picospacos.util.function.MapPredicate;
import com.saicone.picospacos.util.function.NumberPredicate;
import com.saicone.picospacos.util.function.StringPredicate;
import com.saicone.rtag.RtagItem;
import com.saicone.types.IterableType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Function;

public class NbtField implements ItemField<Object> {

    private static final java.util.Map<Object[], NbtField> REGISTRY = new HashMap<>();

    @NotNull
    public static NbtField valueOf(@NotNull Object... path) {
        NbtField field = REGISTRY.get(path);
        if (field == null) {
            field = new NbtField(path);
            REGISTRY.put(path, field);
        }
        return field;
    }

    private final Object[] path;

    public NbtField(@NotNull Object... path) {
        this.path = path;
    }

    @Override
    public @NotNull AnyPredicate<?> predicate(@NotNull Object object, @NotNull String... args) {
        if (args.length > 0) {
            final String type = Strings.before(args[0], '<').toLowerCase();
            if (type.equals("map") || type.equals("entries")) {
                final String[] params = params(args[0]);
                final String keyType = params.length > 0 ? params[0] : "";
                final String valueType = params.length > 0 ? params[0] : "";
                final String mapType = args.length > 1 ? args[1] : "";
                return MapPredicate.valueOf(mapType, (java.util.Map<?, ?>) object, key -> predicate(keyType, key), value -> predicate(valueType, value));
            } else if (type.endsWith("[]")) {
                final String elementType = type.substring(0, type.length() - 2);
                final String listType = args.length > 1 ? args[1] : "";
                return IterablePredicate.valueOf(listType, IterableType.of(object), element -> predicate(elementType, element));
            } else if (type.equals("iterable") || type.equals("collection") || type.equals("list") || type.equals("set")) {
                final String[] params = params(args[0]);
                final String elementType = params.length > 0 ? params[0] : "";
                final String listType = args.length > 1 ? args[1] : "";
                return IterablePredicate.valueOf(listType, IterableType.of(object), element -> predicate(elementType, element));
            } else {
                return predicate(args[0], object);
            }
        }
        return StringPredicate.valueOf(object);
    }

    @NotNull
    private static AnyPredicate<?> predicate(@NotNull String type, @NotNull Object object) {
        switch (type.toLowerCase()) {
            case "boolean":
                return BooleanPredicate.valueOf(object);
            case "number":
            case "byte":
            case "short":
            case "int":
            case "integer":
            case "long":
            case "float":
            case "double":
                return NumberPredicate.valueOf(object);
            case "string":
            case "text":
            case "char":
            case "character":
            default:
                return StringPredicate.valueOf(object);
        }
    }

    @NotNull
    private static String[] params(@NotNull String s) {
        final int start = s.indexOf('<');
        final int end = s.lastIndexOf('>');
        if (start < 0 || end < 0 || start > end || start + 1 == end) {
            return new String[0];
        }
        final String[] params = s.substring(start + 1, end).split(",");
        for (int i = 0; i < params.length; i++) {
            params[i] = params[i].trim();
        }
        return params;
    }

    @Override
    public @Nullable Object get(@NotNull ItemStack item) {
        return new RtagItem(item).get(path);
    }

    @Override
    public void set(@NotNull ItemStack item, Object o) {
        RtagItem.edit(item, tag -> {
            tag.set(o, path);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean test(@NotNull ItemStack item, @NotNull AnyPredicate<?> predicate) {
        Object value = get(item);
        if ((value instanceof Object[] || (value != null && value.getClass().isArray())) && predicate instanceof IterablePredicate) {
            value = IterableType.of(value);
        }
        return ((AnyPredicate<Object>) predicate).test(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean test(@NotNull ItemStack item, @NotNull AnyPredicate<?> predicate, @NotNull Function<Object, Object> parser) {
        Object value = get(item);
        if ((value instanceof Object[] || (value != null && value.getClass().isArray())) && predicate instanceof IterablePredicate) {
            value = IterableType.of(value);
        }
        return ((AnyPredicate<Object>) predicate).test(value, parser);
    }
}
