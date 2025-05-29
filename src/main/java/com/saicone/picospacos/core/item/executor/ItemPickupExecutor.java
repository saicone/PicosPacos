package com.saicone.picospacos.core.item.executor;

import com.google.common.base.Suppliers;
import com.saicone.picospacos.core.item.ScriptExecutor;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class ItemPickupExecutor<E extends Event> implements ScriptExecutor<E> {

    private static final Supplier<ItemPickupExecutor<?>> INSTANCE = Suppliers.<ItemPickupExecutor<?>>memoize(() -> {
        try {
            // PlayerAttemptPickupItemEvent
            final Class<? extends ItemPickupExecutor> paperExecutor = Class.forName("com.saicone.picospacos.core.item.executor.AttemptPickupExecutor").asSubclass(ItemPickupExecutor.class);
            return paperExecutor.getDeclaredConstructor().newInstance();
        } catch (Throwable t) {
            try {
                Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
                return new EntityPickup();
            } catch (Throwable t1) {
                return new PlayerPickup();
            }
        }
    });

    @NotNull
    public static ItemPickupExecutor<?> instance() {
        return INSTANCE.get();
    }

    protected void iterate(@NotNull Item item, @NotNull UnaryOperator<ItemStack> operator) {
        final ItemStack stack = operator.apply(item.getItemStack());
        if (stack != null) {
            item.setItemStack(stack);
        }
    }

    @SuppressWarnings("deprecation")
    public static class PlayerPickup extends ItemPickupExecutor<PlayerPickupItemEvent> implements ScriptExecutor.Player<PlayerPickupItemEvent> {

        @Override
        public @NotNull Class<PlayerPickupItemEvent> eventType() {
            return PlayerPickupItemEvent.class;
        }

        @Override
        public @NotNull HandlerList handlerList() {
            return PlayerPickupItemEvent.getHandlerList();
        }

        @Override
        public void iterate(@NotNull PlayerPickupItemEvent event, @NotNull UnaryOperator<ItemStack> operator) {
            iterate(event.getItem(), operator);
        }
    }

    public static class EntityPickup extends ItemPickupExecutor<EntityPickupItemEvent> implements ScriptExecutor.Entity<EntityPickupItemEvent> {
        @Override
        public @NotNull Class<EntityPickupItemEvent> eventType() {
            return EntityPickupItemEvent.class;
        }

        @Override
        public @NotNull HandlerList handlerList() {
            return EntityPickupItemEvent.getHandlerList();
        }

        @Override
        public void iterate(@NotNull EntityPickupItemEvent event, @NotNull UnaryOperator<ItemStack> operator) {
            iterate(event.getItem(), operator);
        }
    }
}
