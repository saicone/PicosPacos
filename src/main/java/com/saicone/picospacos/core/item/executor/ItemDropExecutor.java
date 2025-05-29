package com.saicone.picospacos.core.item.executor;

import com.saicone.picospacos.core.item.ScriptExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class ItemDropExecutor implements ScriptExecutor.Player<PlayerDropItemEvent> {

    private static final ItemDropExecutor INSTANCE = new ItemDropExecutor();

    @NotNull
    public static ItemDropExecutor instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull Class<PlayerDropItemEvent> eventType() {
        return PlayerDropItemEvent.class;
    }

    @Override
    public @NotNull HandlerList handlerList() {
        return PlayerDropItemEvent.getHandlerList();
    }

    @Override
    public void iterate(@NotNull PlayerDropItemEvent event, @NotNull UnaryOperator<ItemStack> operator) {
        final ItemStack item = operator.apply(event.getItemDrop().getItemStack());
        if (item != null) {
            event.getItemDrop().setItemStack(item);
        }
    }
}
