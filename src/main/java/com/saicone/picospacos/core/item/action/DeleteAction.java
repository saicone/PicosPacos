package com.saicone.picospacos.core.item.action;

import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.util.Items;
import org.jetbrains.annotations.NotNull;

public class DeleteAction implements ItemAction {

    public static final Builder<DeleteAction> BUILDER = new Builder<DeleteAction>("DELETE", "(?i)delete-?item").provides(DeleteAction::new);

    @Override
    public void execute(@NotNull ItemHolder holder) {
        holder.setItem(Items.empty());
    }
}
