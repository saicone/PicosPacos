package com.saicone.picospacos.core.item.executor;

import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.core.item.ScriptExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class BlockPlaceExecutor implements ScriptExecutor<BlockPlaceEvent> {

    private static final BlockPlaceExecutor INSTANCE = new BlockPlaceExecutor();

    @NotNull
    public static BlockPlaceExecutor instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull Class<BlockPlaceEvent> eventType() {
        return BlockPlaceEvent.class;
    }

    @Override
    public @NotNull HandlerList handlerList() {
        return BlockPlaceEvent.getHandlerList();
    }

    @Override
    public @NotNull ItemHolder holder(@NotNull BlockPlaceEvent event) {
        return holder(event.getPlayer());
    }

    @Override
    public void iterate(@NotNull BlockPlaceEvent event, @NotNull UnaryOperator<ItemStack> operator) {
        operator.apply(event.getItemInHand());
    }
}
