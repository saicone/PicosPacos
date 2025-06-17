package com.saicone.picospacos.core.item.field;

import com.saicone.picospacos.core.item.ItemField;
import com.saicone.picospacos.util.function.AnyPredicate;
import com.saicone.picospacos.util.function.NumberPredicate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AmountField implements ItemField<Integer> {

    @Override
    public @NotNull AnyPredicate<?> predicate(@NotNull Object object, @NotNull String... args) {
        return NumberPredicate.valueOf(object);
    }

    @Override
    public @Nullable Integer get(@NotNull ItemStack item) {
        return item.getAmount();
    }

    @Override
    public void set(@NotNull ItemStack item, Integer amount) {
        item.setAmount(amount);
    }
}
