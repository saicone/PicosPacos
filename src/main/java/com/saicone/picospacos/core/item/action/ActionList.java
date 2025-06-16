package com.saicone.picospacos.core.item.action;

import com.saicone.picospacos.api.item.ActionResult;
import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
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
        ActionResult result = ActionResult.DONE;
        for (ItemAction action : actions) {
            result = action.apply(holder);
            if (result.isBreak()) {
                break;
            }
        }
        return result;
    }
}
