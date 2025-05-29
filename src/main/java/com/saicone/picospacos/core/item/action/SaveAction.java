package com.saicone.picospacos.core.item.action;

import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.util.Items;
import org.jetbrains.annotations.NotNull;

public class SaveAction implements ItemAction {

    public static final Builder<SaveAction> BUILDER = new Builder<SaveAction>("SAVE", "(?i)save-?item").provides(SaveAction::new);

    @Override
    public void execute(@NotNull ItemHolder holder) {
        holder.getPlayerData().addSavedItems(holder.getItem());
        holder.setItem(Items.empty());
    }
}
