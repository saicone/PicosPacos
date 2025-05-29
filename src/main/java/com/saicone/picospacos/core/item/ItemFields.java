package com.saicone.picospacos.core.item;

import com.saicone.picospacos.api.item.ItemPredicate;
import com.saicone.picospacos.core.item.field.AmountField;
import com.saicone.picospacos.core.item.field.CustomModelDataField;
import com.saicone.picospacos.core.item.field.DisplayNameField;
import com.saicone.picospacos.core.item.field.DurabilityField;
import com.saicone.picospacos.core.item.field.EnchantmentsField;
import com.saicone.picospacos.core.item.field.FlagsField;
import com.saicone.picospacos.core.item.field.LoreField;
import com.saicone.picospacos.core.item.field.MaterialField;
import com.saicone.picospacos.module.settings.BukkitSettings;
import com.saicone.rtag.RtagItem;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ItemFields {

    private static final Map<String, ItemField<?>> REGISTRY = new TreeMap<>((s1, s2) -> {
        int dotCount1 = s1.length() - s1.replace(".", "").length();
        int dotCount2 = s2.length() - s2.replace(".", "").length();
        if (dotCount1 != dotCount2) {
            return Integer.compare(dotCount2, dotCount1);
        }
        return s1.compareTo(s2);
    });

    public static final ItemField<Integer> AMOUNT = register("amount", new AmountField());
    public static final ItemField<Integer> CUSTOM_MODEL_DATA = register("custom_model_data", new CustomModelDataField.Plain());
    public static final ItemField<List<Float>> CUSTOM_MODEL_DATA_FLOATS = register("custom_model_data.floats", new CustomModelDataField.Floats());
    public static final ItemField<List<Boolean>> CUSTOM_MODEL_DATA_FLAGS = register("custom_model_data.flags", new CustomModelDataField.Flags());
    public static final ItemField<List<String>> CUSTOM_MODEL_DATA_STRINGS = register("custom_model_data.strings", new CustomModelDataField.Strings());
    public static final ItemField<List<Color>> CUSTOM_MODEL_DATA_COLORS = register("custom_model_data.colors", new CustomModelDataField.Colors());
    public static final ItemField<String> DISPLAY_NAME = register("display_name", new DisplayNameField());
    public static final ItemField<Short> DURABILITY = register("durability", new DurabilityField());
    public static final ItemField<Map<Enchantment, Integer>> ENCHANTMENTS = register("enchantments", new EnchantmentsField());
    public static final ItemField<Set<ItemFlag>> FLAGS = register("flags", new FlagsField());
    public static final ItemField<List<String>> LORE = register("lore", new LoreField());
    public static final ItemField<String> MATERIAL = register("material", new MaterialField());
    public static final ItemField<String> MATERIAL_XSERIES = register("material.xseries", new MaterialField.XSeries());

    @NotNull
    public static <T, F extends ItemField<T>> F register(@NotNull String key, @NotNull F field) {
        REGISTRY.put(key, field);
        return field;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static ItemField<Object> nbt(@NotNull Object... path) {
        final String key = "nbt." + Arrays.stream(path).map(String::valueOf).collect(Collectors.joining("."));
        ItemField<Object> field = (ItemField<Object>) REGISTRY.get(key);
        if (field == null) {
            field = new ItemField<>() {
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
            };
            REGISTRY.put(key, field);
        }
        return field;
    }

    @NotNull
    public ItemPredicate predicate(@NotNull BukkitSettings settings) {

    }
}
