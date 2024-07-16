package com.saicone.picospacos.module.lang.display;

import com.saicone.picospacos.module.lang.LangDisplay;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ActionbarDisplay implements LangDisplay {

    private final String actionbar;

    public ActionbarDisplay(@NotNull String actionbar) {
        this.actionbar = actionbar;
    }

    @Override
    public @NotNull String getText() {
        return actionbar;
    }

    @Override
    public void sendTo(@NotNull CommandSender sender, @NotNull Function<String, String> parser) {
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(parser.apply(actionbar)));
        } else {
            sender.sendMessage(parser.apply(actionbar));
        }
    }

    @Override
    public void sendTo(@NotNull Collection<? extends CommandSender> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> senderParser) {
        final String actionbar = parser.apply(this.actionbar);
        for (CommandSender sender : senders) {
            if (sender instanceof Player) {
                ((Player) sender).spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(parser.apply(actionbar)));
            } else {
                sender.sendMessage(parser.apply(actionbar));
            }
        }
    }
}
