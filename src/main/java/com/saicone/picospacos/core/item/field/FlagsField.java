package com.saicone.picospacos.core.item.field;

import com.saicone.picospacos.core.item.ItemField;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class FlagsField implements ItemField.MappedIterable<ItemFlag, Set<ItemFlag>> {

    private static final Function<ItemFlag, Object> MAPPER = Enum::name;

    @Override
    public @NotNull Function<ItemFlag, Object> mapper() {
        return MAPPER;
    }

    @Override
    public @Nullable Set<ItemFlag> get(@NotNull ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getItemFlags();
    }

    @Override
    public void set(@NotNull ItemStack item, Set<ItemFlag> flags) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        final Set<ItemFlag> remove = new HashSet<>();
        for (ItemFlag value : ItemFlag.values()) {
            if (!flags.contains(value)) {
                remove.add(value);
            }
        }
        meta.removeItemFlags(remove.toArray(new ItemFlag[0]));
        meta.addItemFlags(flags.toArray(new ItemFlag[0]));
        item.setItemMeta(meta);
    }
}
