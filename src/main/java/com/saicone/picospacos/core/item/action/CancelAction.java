package com.saicone.picospacos.core.item.action;

import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public class CancelAction implements ItemAction {

    public static final Builder<CancelAction> BUILDER = new Builder<CancelAction>("CANCEL", "(?i)cancel-?events?").provides(CancelAction::new);

    @Override
    public void execute(@NotNull ItemHolder holder) {
        if (holder.getEvent() instanceof Cancellable) {
            ((Cancellable) holder.getEvent()).setCancelled(true);
        }
    }
}
