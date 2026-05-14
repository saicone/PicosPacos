package com.saicone.picospacos.core.item.field;

import com.saicone.picospacos.core.item.ItemField;
import com.saicone.picospacos.util.Strings;
import com.saicone.picospacos.util.function.AnyPredicate;
import com.saicone.picospacos.util.function.BooleanPredicate;
import com.saicone.picospacos.util.function.IterablePredicate;
import com.saicone.picospacos.util.function.MapPredicate;
import com.saicone.picospacos.util.function.NumberPredicate;
import com.saicone.picospacos.util.function.StringPredicate;
import com.saicone.rtag.Rtag;
import com.saicone.rtag.RtagItem;
import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.item.ItemObject;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ThrowableFunction;
import com.saicone.types.IterableType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class NbtField implements ItemField<Object> {

    private static final java.util.Map<Object[], NbtField> REGISTRY = new HashMap<>();

    @NotNull
    public static NbtField valueOf(@NotNull Object... path) {
        NbtField field = REGISTRY.get(path);
        if (field == null) {
            Object[] finalPath = path;
            NbtField child = null;
            boolean all = false;
            for (int i = 0; i < path.length; i++) {
                final Object key = path[i];
                if (key instanceof String && i + 1 < path.length) {
                    final String str = (String) key;
                    if (str.equals("[]") || str.equals("[*]")) {
                        finalPath = Arrays.copyOfRange(finalPath, 0, i);
                        child = valueOf(Arrays.copyOfRange(finalPath, i + 1, finalPath.length));
                        all = str.equals("[*]");
                        break;
                    } else if (str.startsWith("[") && str.endsWith("]")) {
                        final Integer index = Strings.asInt(str.substring(1, str.length() - 1));
                        if (index != null) {
                            final ThrowableFunction<Object, Object> function = tag -> {
                                if (TagList.isTagList(tag)) {
                                    final List<Object> list = TagList.getValue(tag);
                                    return index < list.size() ? list.get(index) : null;
                                } else {
                                    return null;
                                }
                            };
                            finalPath[i] = function;
                        }
                    }
                }
            }
            field = new NbtField(finalPath, child, all);
            REGISTRY.put(path, field);
        }
        return field;
    }

    private final Object[] path;
    private final NbtField child;
    private final boolean all;

    public NbtField(@NotNull Object[] path, @Nullable NbtField child, boolean all) {
        this.path = path;
        this.child = child;
        this.all = all;
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
        return AnyPredicate.exist(object);
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
        final Object tag = getTag(item);
        if (tag == null) {
            return null;
        }
        return Rtag.INSTANCE.get(tag, this.path);
    }

    @Nullable
    public Object getTag(@NotNull ItemStack item) {
        final Object mcItem;
        final ItemStack craftItem = ItemObject.getCraftStack(item);
        if (craftItem == null) {
            mcItem = ItemObject.asNMSCopy(item);
        } else {
            mcItem = ItemObject.getUncheckedHandle(craftItem);
        }

        return ItemObject.getCustomDataTag(mcItem);
    }

    @Override
    public void set(@NotNull ItemStack item, Object o) {
        RtagItem.edit(item, tag -> {
            tag.set(o, path);
        });
    }

    @Override
    public boolean test(@NotNull ItemStack item, @NotNull AnyPredicate<?> predicate) {
        return test(getTag(item), predicate, null);
    }

    @Override
    public boolean test(@NotNull ItemStack item, @NotNull AnyPredicate<?> predicate, @NotNull Function<Object, Object> parser) {
        return test(getTag(item), predicate, parser);
    }

    @SuppressWarnings("unchecked")
    private boolean test(@Nullable Object tag, @NotNull AnyPredicate<?> predicate, @Nullable Function<Object, Object> parser) {
        if (tag == null) {
            return false;
        }

        tag = Rtag.INSTANCE.getExact(tag, this.path);

        if (this.child != null) {
            if (TagList.isTagList(tag)) {
                for (Object element : TagList.getValue(tag)) {
                    if (this.child.test(element, predicate, parser)) {
                        if (!this.all) {
                            return true;
                        }
                    } else {
                        if (this.all) {
                            return false;
                        }
                    }
                }
                return this.all;
            } else {
                return this.child.test(tag, predicate, parser);
            }
        }

        Object value = RtagMirror.INSTANCE.getTagValue(tag);
        if ((value instanceof Object[] || (value != null && value.getClass().isArray())) && predicate instanceof IterablePredicate) {
            value = IterableType.of(value);
        }

        if (parser == null) {
            return ((AnyPredicate<Object>) predicate).test(value);
        } else {
            return ((AnyPredicate<Object>) predicate).test(value, parser);
        }
    }
}
