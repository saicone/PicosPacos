package com.saicone.picospacos.core.item.executor;

import com.saicone.picospacos.core.item.ScriptExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class ItemClickExecutor implements ScriptExecutor.Inventory<InventoryClickEvent> {

    private static final ItemClickExecutor INSTANCE = new ItemClickExecutor();

    @NotNull
    public static ItemClickExecutor instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull Class<InventoryClickEvent> eventType() {
        return InventoryClickEvent.class;
    }

    @Override
    public @NotNull HandlerList handlerList() {
        return InventoryClickEvent.getHandlerList();
    }

    @Override
    public void iterate(@NotNull InventoryClickEvent event, @NotNull UnaryOperator<ItemStack> operator) {
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }
        final org.bukkit.inventory.Inventory inventory = event.getClickedInventory();
        if (inventory == null) {
            return;
        }
        final InventoryHolder holder = inventory.getHolder();
        if (holder == null || !holder.getClass().getName().startsWith("org.bukkit.")) {
            return;
        }
        item = operator.apply(item);
        if (item != null) {
            event.setCurrentItem(item);
        }
    }
}
