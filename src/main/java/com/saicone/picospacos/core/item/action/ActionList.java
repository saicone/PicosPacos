package com.saicone.picospacos.core.item.action;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.item.ActionResult;
import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ActionList implements ItemAction {

    private final List<ItemAction> actions;

    public ActionList(@NotNull List<ItemAction> actions) {
        this.actions = actions;
    }

    @NotNull
    public List<ItemAction> getActions() {
        return actions;
    }

    @Override
    public @NotNull ActionResult apply(@NotNull ItemHolder holder) {
        return result(holder, actions, 0);
    }

    protected static ActionResult result(@NotNull ItemHolder holder, @NotNull List<ItemAction> actions, int start) {
        ActionResult result = ActionResult.DONE;
        for (int i = start; i < actions.size(); i++) {
            final ItemAction action = actions.get(i);
            result = action.apply(holder);
            if (result.isBreak()) {
                break;
            } else if (result instanceof ActionResult.Delay) {
                final ItemHolder clone = holder.clone();
                final int next = i + 1;
                if (Bukkit.isPrimaryThread()) {
                    Bukkit.getScheduler().runTaskLater(PicosPacos.get(), () -> result(clone, actions, next), ((ActionResult.Delay) result).ticks());
                } else {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(PicosPacos.get(), () -> result(clone, actions, next), ((ActionResult.Delay) result).ticks());
                }
                break;
            }
        }
        return result;
    }
}
