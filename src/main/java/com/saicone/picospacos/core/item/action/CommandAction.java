package com.saicone.picospacos.core.item.action;

import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.api.item.ItemHolder;
import com.saicone.types.Types;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandAction implements ItemAction {

    public static final Builder<CommandAction> BUILDER = new Builder<CommandAction>("COMMAND", "(?i)((run|execute)-?)?(command|cmd)s?")
            .accept(config -> {
                final List<String> commands = config.getRegex("(?i)(value|command|cmd)s?").asList(Types.STRING);
                final boolean console = config.getRegex("(?i)console(-?sender)?").asBoolean(true);
                return new CommandAction(commands, console);
            });

    private final List<String> commands;
    private final boolean console;

    public CommandAction(@NotNull List<String> commands, boolean console) {
        this.commands = commands;
        this.console = console;
    }

    @Override
    public void execute(@NotNull ItemHolder holder) {
        final CommandSender sender;
        if (console) {
            sender = Bukkit.getConsoleSender();
        } else {
            sender = holder.getPlayer();
        }
        for (String command : commands) {
            Bukkit.dispatchCommand(sender, holder.parse(command));
        }
    }
}
