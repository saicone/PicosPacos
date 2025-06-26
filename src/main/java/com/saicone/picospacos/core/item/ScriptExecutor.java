package com.saicone.picospacos.core.item;

import com.saicone.picospacos.api.item.ItemHolder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface ScriptExecutor<E extends Event> {

    @NotNull
    Class<E> eventType();

    @NotNull
    HandlerList handlerList();

    @NotNull
    ItemHolder holder(@NotNull E event);

    @NotNull
    default ItemHolder holder(@NotNull org.bukkit.entity.Entity entity) {
        return PacoItemHolder.valueOf(entity);
    }

    @NotNull
    default ItemHolder holder(@NotNull org.bukkit.entity.Player player) {
        return PacoItemHolder.valueOf(player);
    }

    @NotNull
    default ItemHolder holder(@NotNull Location location) {
        return PacoItemHolder.valueOf(location);
    }

    void iterate(@NotNull E event, @NotNull UnaryOperator<ItemStack> operator);

    default void iterate(@NotNull org.bukkit.entity.Player player, @NotNull UnaryOperator<ItemStack> operator) {
        final org.bukkit.inventory.Inventory inventory = player.getInventory();
        int slot = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                final ItemStack edited = operator.apply(item);
                if (edited != null) {
                    if (edited.getType() == Material.AIR) {
                        inventory.setItem(slot, null);
                    } else {
                        inventory.setItem(slot, edited);
                    }
                }
            }
            slot++;
        }
    }

    interface Entity<E extends EntityEvent> extends ScriptExecutor<E> {

        @Override
        default @NotNull ItemHolder holder(@NotNull E event) {
            return holder(event.getEntity());
        }
    }

    interface Player<E extends PlayerEvent> extends ScriptExecutor<E> {

        @NotNull
        static <E extends PlayerEvent> Player<E> valueOf(@NotNull Class<E> eventType, @NotNull Supplier<HandlerList> supplier) {
            return new Player<>() {
                @Override
                public @NotNull Class<E> eventType() {
                    return eventType;
                }

                @Override
                public @NotNull HandlerList handlerList() {
                    return supplier.get();
                }
            };
        }

        @Override
        default @NotNull ItemHolder holder(@NotNull E event) {
            return holder(event.getPlayer());
        }

        @Override
        default void iterate(@NotNull E event, @NotNull UnaryOperator<ItemStack> operator) {
            iterate(event.getPlayer(), operator);
        }
    }

    interface Inventory<E extends InventoryEvent> extends ScriptExecutor<E> {

        @Override
        default @NotNull ItemHolder holder(@NotNull E event) {
            return holder(event.getView().getPlayer());
        }
    }
}
