package com.saicone.picospacos.module.hook;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.api.item.ScriptEvent;
import com.saicone.picospacos.core.item.ScriptExecutor;
import nl.marido.deluxecombat.events.CombatlogEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class DeluxeCombatHook implements ScriptExecutor<CombatlogEvent> {

    public void load() {
        PicosPacos.get().scriptRegistry().register(ScriptEvent.ITEM_DROP, this);
    }

    @Override
    public @NotNull Class<CombatlogEvent> eventType() {
        return CombatlogEvent.class;
    }

    @Override
    public @NotNull HandlerList handlerList() {
        return CombatlogEvent.getHandlerList();
    }

    @Override
    public @NotNull ItemHolder holder(@NotNull CombatlogEvent event) {
        return holder(event.getCombatlogger());
    }

    @Override
    public void iterate(@NotNull CombatlogEvent event, @NotNull UnaryOperator<ItemStack> operator) {
        iterate(event.getCombatlogger(), operator);
    }
}
