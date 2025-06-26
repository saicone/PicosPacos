package com.saicone.picospacos.core.item;

import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.api.item.ItemScript;
import com.saicone.picospacos.module.hook.Placeholders;
import com.saicone.picospacos.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacoItemHolder extends ItemHolder {

    @NotNull
    public static ItemHolder valueOf(@NotNull Entity entity) {
        if (entity instanceof Player) {
            return valueOf((Player) entity);
        } else {
            return valueOf(entity.getLocation());
        }
    }

    @NotNull
    public static ItemHolder valueOf(@NotNull Player player) {
        return new PacoItemHolder(null, null, player, Items.empty());
    }

    @NotNull
    public static ItemHolder valueOf(@NotNull Location location) {
        return new PacoItemHolder(null, null, Bukkit.getConsoleSender(), Items.empty()) {
            @Override
            public @NotNull Location getLocation() {
                return location;
            }
        };
    }

    protected PacoItemHolder() {
    }

    public PacoItemHolder(@Nullable ItemScript script, @Nullable Object event, @NotNull CommandSender user, @NotNull ItemStack item) {
        super(script, event, user, item);
    }

    @NotNull
    public Location getLocation() {
        return getPlayer().getLocation();
    }

    @Override
    public @NotNull String parse(@NotNull String s) {
        s = s.replace("{event}", getScript().id()).replace("{player}", isPlayer() ? getPlayer().getName() : "@console");
        if (s.contains("{event_location}")) {
            s = s.replace("{event_location}", parseLocation(getLocation()));
        }
        if (s.contains("{item}")) {
            s = s.replace("{item}", Items.getFilteredItemData(getItem()));
        }
        if (s.contains("{item_all}")) {
            s = s.replace("{item_all}", Items.getItemData(getItem()));
        }
        if (s.indexOf('%') >= 0) {
            if (isPlayer()) {
                s = Placeholders.parse(getPlayer(), s);
            } else {
                s = Placeholders.parse(null, s);
            }
        }
        return s.indexOf('%') >= 0 ? Placeholders.parse(getPlayer(), s) : s;
    }

    @NotNull
    private static String parseLocation(@NotNull Location location) {
        return "(" + location.getX() + ", " + location.getY() + ", " + location.getZ() + ", " + location.getWorld() + ")";
    }
}
