package me.rubenicos.mc.picospacos.api.event;

import me.rubenicos.mc.picospacos.api.object.PacoRule;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemsPacoEvent extends Event implements Cancellable {

    private final Player player;
    private final PacoRule rule;
    private final List<ItemStack> items;

    private boolean cancelled = false;

    public ItemsPacoEvent(Player player, PacoRule rule, List<ItemStack> items) {
        this.player = player;
        this.rule = rule;
        this.items = items;
    }

    public Player getPlayer() {
        return player;
    }

    public PacoRule getRule() {
        return rule;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
