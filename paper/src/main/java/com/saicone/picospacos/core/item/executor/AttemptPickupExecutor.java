package com.saicone.picospacos.core.item.executor;

import com.saicone.picospacos.core.item.ScriptExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class AttemptPickupExecutor extends ItemPickupExecutor<PlayerAttemptPickupItemEvent> implements ScriptExecutor.Player<PlayerAttemptPickupItemEvent> {

    @Override
    public @NotNull Class<PlayerAttemptPickupItemEvent> eventType() {
        return PlayerAttemptPickupItemEvent.class;
    }

    @Override
    public @NotNull HandlerList handlerList() {
        return PlayerAttemptPickupItemEvent.getHandlerList();
    }

    @Override
    public void iterate(@NotNull PlayerAttemptPickupItemEvent event, @NotNull UnaryOperator<ItemStack> operator) {
        iterate(event.getItem(), operator);
    }
}
