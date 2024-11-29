package com.saicone.picospacos.core.paco.rule;

import com.saicone.picospacos.PicosPacos;
import com.saicone.rtag.item.ItemObject;
import com.saicone.rtag.item.mirror.IDisplayMirror;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public final class PacoRule {

    private final String id;
    private final List<RuleType> rules;
    private final List<TagType> tags;
    private final List<String> commands;

    public PacoRule(String id, List<RuleType> rules, List<TagType> tags, List<String> commands) {
        this.id = id;
        this.rules = rules;
        this.tags = tags;
        this.commands = commands.isEmpty() ? null : commands;
    }

    public String getId() {
        return id;
    }

    public boolean containsRule(RuleType rule) {
        return rules.contains(rule);
    }

    public boolean match(ItemStack item, Player player) {
        for (TagType tag : tags) {
            if (!tag.valid(item, player)) {
                return false;
            }
        }
        if (commands != null) {
            final String itemStr = getItemData(item);
            Bukkit.getScheduler().runTask(PicosPacos.get(), () -> {
                for (String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("{player}", player.getName())
                            .replace("{rule}", this.id)
                            .replace("{item}", itemStr)
                    );
                }
            });
        }
        return true;
    }

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

    @NotNull
    private static String getItemData(@NotNull ItemStack item) {
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
