package com.saicone.picospacos.util;

import com.saicone.rtag.item.ItemObject;
import com.saicone.rtag.item.mirror.IDisplayMirror;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Items {

    private static final ItemStack EMPTY = new ItemStack(Material.AIR);
    private static final List<String> ALLOWED_TAGS = List.of(
            "CustomModelData",
            "Damage",
            "HideFlags",
            "Unbreakable",
            "Enchantments",
            "StoredEnchantments",
            "display",
            "AttributeModifiers",
            "CanPlaceOn",
            "CanDestroy",
            "Potion",
            "CustomPotionColor",
            "CustomPotionEffects",
            "map",
            "SkullOwner",
            "Fireworks",
            "Items",
            "author",
            "title"
    );
    private static final IDisplayMirror DISPLAY_MIRROR = new IDisplayMirror();

    Items() {
    }

    @NotNull
    public static ItemStack empty() {
        return EMPTY;
    }

    @NotNull
    public static ItemStack notNull(@Nullable ItemStack item) {
        return item == null ? EMPTY : item;
    }

    @NotNull
    public static String getItemData(@NotNull ItemStack item) {
        return ItemObject.save(ItemObject.asNMSCopy(item)).toString();
    }

    @NotNull
    public static String getFilteredItemData(@NotNull ItemStack item) {
        final Object base = ItemObject.save(ItemObject.asNMSCopy(item));
        if (ServerInstance.Release.LEGACY) {
            return base.toString();
        }
        if (TagCompound.hasKey(base, "tag")) {
            final Map<String, Object> tag = TagCompound.getValue(TagCompound.get(base, "tag"));
            tag.entrySet().removeIf(entry -> !ALLOWED_TAGS.contains(entry.getKey()));
            if (ServerInstance.Release.FLAT && tag.containsKey("display")) {
                DISPLAY_MIRROR.downgrade(base, "", TagCompound.get(base, "tag"), ServerInstance.VERSION, 12);
            }
        } else if (TagCompound.hasKey(base, "components")) {
            final Map<String, Object> components = TagCompound.getValue(TagCompound.get(base, "components"));
            components.remove("minecraft:custom_data");
            if (components.containsKey("minecraft:custom_name")) {
                Object name = DISPLAY_MIRROR.processTag(TagCompound.get(components, "minecraft:custom_name"), false);
                if (name != null) {
                    TagCompound.set(components, "minecraft:custom_name", name);
                }
            }
            if (components.containsKey("minecraft:lore")) {
                Object lore = TagCompound.get(components, "minecraft:lore");
                if (lore != null) {
                    int size = TagList.size(lore);
                    for (int i = 0; i < size; i++) {
                        Object tag = DISPLAY_MIRROR.processTag(TagList.get(lore, i), false);
                        if (tag != null) {
                            TagList.set(lore, i, tag);
                        }
                    }
                }
            }
        }
        return base.toString();
    }
}
