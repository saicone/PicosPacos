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
    public static ItemHolder valueOf(@Nullable ItemScript script, @NotNull Entity entity, @NotNull ItemStack item) {
        if (entity instanceof Player) {
            return valueOf(script, (Player) entity, item);
        } else {
            return valueOf(script, item, entity.getLocation());
        }
    }

    @NotNull
    public static ItemHolder valueOf(@Nullable ItemScript script, @NotNull Player player, @NotNull ItemStack item) {
        return new PacoItemHolder(script, player, item);
    }

    @NotNull
    public static ItemHolder valueOf(@Nullable ItemScript script, @NotNull ItemStack item, @NotNull Location location) {
        return new PacoItemHolder(script, Bukkit.getConsoleSender(), item) {
            @Override
            public @NotNull Location getLocation() {
                return location;
            }
        };
    }

    public PacoItemHolder(@Nullable ItemScript script, @NotNull CommandSender user, @NotNull ItemStack item) {
        super(script, user, item);
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
