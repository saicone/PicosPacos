package com.saicone.picospacos.core.item.action;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.picospacos.util.Strings;
import org.jetbrains.annotations.NotNull;

public class LangAction implements ItemAction {

    public static final Builder<LangAction> BUILDER = new Builder<LangAction>("LANG", "(?i)(send-?)?lang")
            .accept(config -> {
                final String[] value = Strings.splitQuoted(config.getRegex("(?i)(value|command|cmd)s?").asString(""), ' ');
                final Object[] args = new Object[value.length - 1];
                System.arraycopy(value, 1, args, 0, value.length - 1);
                return new LangAction(value[0], args);
            });

    private final String path;
    private final Object[] args;

    public LangAction(@NotNull String path, @NotNull Object... args) {
        this.path = path;
        this.args = args;
    }

    @Override
    public void execute(@NotNull ItemHolder holder) {
        PicosPacos.get().getLang().sendTo(holder.getUser(), path, s -> holder.parse(Strings.replaceArgs(s, args)));
    }
}
