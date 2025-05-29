package com.saicone.picospacos.core.item.field;

import com.saicone.picospacos.core.item.ItemField;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class DurabilityField implements ItemField<Short> {

    @Override
    public @Nullable Short get(@NotNull ItemStack item) {
        return item.getDurability();
    }

    @Override
    public void set(@NotNull ItemStack item, Short durability) {
        item.setDurability(durability);
    }
}
