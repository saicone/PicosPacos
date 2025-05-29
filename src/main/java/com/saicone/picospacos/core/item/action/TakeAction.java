package com.saicone.picospacos.core.item.action;

import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.util.Items;
import org.jetbrains.annotations.NotNull;

public class TakeAction implements ItemAction {

    public static final Builder<TakeAction> BUILDER = new Builder<TakeAction>("TAKE", "(?i)take-?item").provides(TakeAction::new);

    @Override
    public void execute(@NotNull ItemHolder holder) {
        holder.getPlayerData().addTakenItems(holder.getItem());
        holder.setItem(Items.empty());
    }
}
