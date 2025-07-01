package com.saicone.picospacos.core.item.executor;

import com.saicone.picospacos.core.item.ScriptExecutor;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ListIterator;
import java.util.function.UnaryOperator;

public class PlayerDeathExecutor implements ScriptExecutor.Entity<PlayerDeathEvent> {

    private static final PlayerDeathExecutor INSTANCE = new PlayerDeathExecutor();

    @NotNull
    public static PlayerDeathExecutor instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull Class<PlayerDeathEvent> eventType() {
        return PlayerDeathEvent.class;
    }

    @Override
    public @NotNull HandlerList handlerList() {
        return PlayerDeathEvent.getHandlerList();
    }

    @Override
    public void iterate(@NotNull PlayerDeathEvent event, @NotNull UnaryOperator<ItemStack> operator) {
        final ListIterator<ItemStack> drops = event.getDrops().listIterator();
        while (drops.hasNext()) {
            final ItemStack item = drops.next();
            if (item != null) {
                final ItemStack edited = operator.apply(item);
                if (edited != null) {
                    if (edited.getType() == Material.AIR) {
                        drops.remove();
                    } else {
                        drops.set(edited);
                    }
                }
            }
        }
    }
}
