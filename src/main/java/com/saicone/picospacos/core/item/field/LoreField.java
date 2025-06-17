package com.saicone.picospacos.core.item.field;

import com.saicone.picospacos.core.item.ItemField;
import com.saicone.picospacos.util.function.AnyPredicate;
import com.saicone.picospacos.util.function.StringPredicate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LoreField implements ItemField.Iterable<String, List<String>> {

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
        return meta.getLore();
    }

    @Override
    public void set(@NotNull ItemStack item, List<String> lore) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
