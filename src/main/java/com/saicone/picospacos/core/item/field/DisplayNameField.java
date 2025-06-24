package com.saicone.picospacos.core.item.field;

import com.saicone.picospacos.core.item.ItemField;
import com.saicone.picospacos.util.MStrings;
import com.saicone.picospacos.util.Strings;
import com.saicone.picospacos.util.function.AnyPredicate;
import com.saicone.picospacos.util.function.StringPredicate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisplayNameField implements ItemField<String> {

    @Override
    public @NotNull AnyPredicate<?> predicate(@NotNull Object object, @NotNull String... args) {
        return StringPredicate.valueOf(Strings.map(object, MStrings::color));
    }

    @Override
    public @Nullable String get(@NotNull ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getDisplayName();
    }

    @Override
    public void set(@NotNull ItemStack item, String name) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }
}