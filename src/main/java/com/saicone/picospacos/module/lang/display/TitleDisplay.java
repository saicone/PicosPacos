package com.saicone.picospacos.module.lang.display;

import com.saicone.picospacos.module.lang.LangDisplay;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TitleDisplay implements LangDisplay {

    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public TitleDisplay(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public @NotNull String getText() {
        if (title != null) {
            if (subtitle != null) {
                return title + " " + subtitle;
            } else {
                return title;
            }
        } else if (subtitle != null) {
            return subtitle;
        }
        return "";
    }

    @Override
    public void sendTo(@NotNull CommandSender sender, @NotNull Function<String, String> parser) {
        if (sender instanceof Player) {
            ((Player) sender).sendTitle(
                    title != null ? parser.apply(title) : null,
                    subtitle != null ? parser.apply(subtitle) : null,
                    fadeIn,
                    stay,
                    fadeOut
            );
        } else {
            sender.sendMessage(parser.apply(getText()));
        }
    }

    @Override
    public void sendTo(@NotNull Collection<? extends CommandSender> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> senderParser) {
        final String title = this.title != null ? parser.apply(this.title) : null;
        final String subtitle = this.subtitle != null ? parser.apply(this.subtitle) : null;
        for (CommandSender sender : senders) {
            if (sender instanceof Player) {
                ((Player) sender).sendTitle(
                        title != null ? senderParser.apply(sender, title) : null,
                        subtitle != null ? senderParser.apply(sender, subtitle) : null,
                        fadeIn,
                        stay,
                        fadeOut
                );
            } else {
                sender.sendMessage(senderParser.apply(sender, getText()));
            }
        }
    }
}
