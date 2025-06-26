package com.saicone.picospacos.core.item.action;

import com.saicone.picospacos.api.item.ActionResult;
import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class DelayAction implements ItemAction {

    public static final Builder<DelayAction> BUILDER = new Builder<DelayAction>("DELAY", "(?i)delay|wait|pause")
            .accept(config -> {
                final String[] value = config.getRegex("(?i)value|time|delay")
                        .asString("0 SECONDS")
                        .replace("  ", " ")
                        .replace("  ", " ")
                        .split(" ", 2);
                final long time = Long.parseLong(value[0]);
                final TimeUnit unit;
                if (value.length > 1) {
                    String name = value[1].toUpperCase();
                    if (!name.endsWith("S")) {
                        name = name + "S";
                    }
                    unit = TimeUnit.valueOf(name);
                } else {
                    unit = TimeUnit.SECONDS;
                }
                return new DelayAction(ActionResult.delay(time, unit));
            });

    private final ActionResult.Delay result;

    public DelayAction(@NotNull ActionResult.Delay result) {
        this.result = result;
    }

    @Override
    public @NotNull ActionResult apply(@NotNull ItemHolder holder) {
        return result.time() > 0 ? result : ActionResult.DONE;
    }
}
