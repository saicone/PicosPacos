package com.saicone.picospacos.module.lang;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface LangDisplay {

    LangDisplay EMPTY = new LangDisplay() {
        @Override
        public @NotNull String getText() {
            return "";
        }

        @Override
        public void sendTo(@NotNull CommandSender sender, @NotNull Function<String, String> parser) {
            // empty method
        }

        @Override
        public void sendTo(@NotNull Collection<? extends CommandSender> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> senderParser) {
            // empty method
        }
    };

    @NotNull
    String getText();

    void sendTo(@NotNull CommandSender sender, @NotNull Function<String, String> parser);

    void sendTo(@NotNull Collection<? extends CommandSender> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> senderParser);

}
