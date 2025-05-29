package com.saicone.picospacos.core.item.field;

import com.saicone.picospacos.core.item.ItemField;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public class EnchantmentsField implements ItemField.Map<Enchantment, Integer, Map<Enchantment, Integer>> {

    @SuppressWarnings("deprecation")
    private static final Function<Enchantment, Object> KEY_MAPPER = Enchantment::getName;
    private static final Function<Integer, Object> VALUE_MAPPER = level -> level;

    @Override
    public @NotNull Function<Enchantment, Object> keyMapper() {
        return KEY_MAPPER;
    }

    @Override
    public @NotNull Function<Integer, Object> valueMapper() {
        return VALUE_MAPPER;
    }

    @Override
    public java.util.@Nullable Map<Enchantment, Integer> get(@NotNull ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getEnchants();
    }

    @Override
    public void set(@NotNull ItemStack item, java.util.Map<Enchantment, Integer> enchantments) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        for (Enchantment value : Enchantment.values()) {
            if (!enchantments.containsKey(value)) {
                meta.removeEnchant(value);
            }
        }
        for (java.util.Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
        item.setItemMeta(meta);
    }
}
