package com.saicone.picospacos.module.lang.display;

import com.saicone.picospacos.module.lang.LangDisplay;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextDisplay implements LangDisplay {

    private final List<String> text;

    public TextDisplay(@NotNull List<String> text) {
        this.text = text;
    }

    @Override
    public @NotNull String getText() {
        return text.isEmpty() ? "" : text.get(0);
    }

    @Override
    public void sendTo(@NotNull CommandSender sender, @NotNull Function<String, String> parser) {
        for (String s : text) {
            sender.sendMessage(parser.apply(s));
        }
    }

    @Override
    public void sendTo(@NotNull Collection<? extends CommandSender> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> senderParser) {
        final List<String> text = this.text.stream().map(parser).collect(Collectors.toList());
        for (CommandSender sender : senders) {
            for (String s : text) {
                sender.sendMessage(senderParser.apply(sender, s));
            }
        }
    }
}
