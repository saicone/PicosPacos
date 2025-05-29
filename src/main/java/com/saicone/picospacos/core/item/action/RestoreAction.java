package com.saicone.picospacos.core.item.action;

import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.util.Items;
import org.jetbrains.annotations.NotNull;

public class RestoreAction implements ItemAction {

    public static final Builder<RestoreAction> BUILDER = new Builder<RestoreAction>("RESTORE", "(?i)restore-?item").provides(RestoreAction::new);

    @Override
    public void execute(@NotNull ItemHolder holder) {
        holder.getPlayerData().addItems(holder.getItem());
        holder.setItem(Items.empty());
    }
}
