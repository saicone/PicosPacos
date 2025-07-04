package com.saicone.picospacos.core.item.field;

import com.cryptomorin.xseries.XMaterial;
import com.saicone.picospacos.core.item.ItemField;
import com.saicone.picospacos.util.function.AnyPredicate;
import com.saicone.picospacos.util.function.StringPredicate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaterialField implements ItemField<String> {

    @Override
    public @NotNull AnyPredicate<?> predicate(@NotNull Object object, @NotNull String... args) {
        return StringPredicate.valueOf(object);
    }

    @Override
    public @Nullable String get(@NotNull ItemStack item) {
        return item.getType().name();
    }

    @Override
    public void set(@NotNull ItemStack item, String material) {
        XMaterial.matchXMaterial(material).map(XMaterial::get).ifPresent(item::setType);
    }

    public static class XSeries extends MaterialField {

        @Override
        public @Nullable String get(@NotNull ItemStack item) {
            return XMaterial.matchXMaterial(item).name();
        }
    }
}
