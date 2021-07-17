package me.rubenicos.mc.picospacos.module.cmd;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.PicosPacosAPI;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PicosPacosCommand extends Command {

    private final PicosPacos pl = PicosPacos.get();
    private final List<String> savesTab = Arrays.asList("give", "take", "set");

    PicosPacosCommand() {
        super("picospacos");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Locale.color("&6> PicosPacos Plugin"));
            sender.sendMessage(Locale.replaceArgs("&6> Version: &f", pl.getDescription().getVersion()));
            sender.sendMessage(Locale.color("&6> Created by: &fRubenicos"));
        } else if (args[0].equalsIgnoreCase("reload")) {
            long init = System.currentTimeMillis();
            pl.reload();
            Locale.sendTo(sender, "Command.Reload", String.valueOf(System.currentTimeMillis() - init));
        } else if (args[0].equalsIgnoreCase("saves")) {
            if (args.length > 3) {
                Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
                    int amount = TextUtils.asInt(args[3], 0);
                    if (amount == 0) {
                        Locale.sendTo(sender, "Command.Saves.Invalid-Amount", args[3]);
                    } else if (args[1].equalsIgnoreCase("give")) {
                        PicosPacosAPI.getPlayerOrLoad(player(args[2])).addSaves(amount);
                        Locale.sendTo(sender, "Command.Saves.Give", args[2], args[3]);
                    } else if (args[1].equalsIgnoreCase("take")) {
                        PicosPacosAPI.getPlayerOrLoad(player(args[2])).takeSaves(amount);
                        Locale.sendTo(sender, "Command.Saves.Take", args[2], args[3]);
                    } else if (args[1].equalsIgnoreCase("set")) {
                        PicosPacosAPI.getPlayerOrLoad(player(args[2])).setSaves(amount);
                        Locale.sendTo(sender, "Command.Saves.Set", args[3], args[2]);
                    } else {
                        Locale.sendTo(sender, "Command.Saves.Usage");
                    }
                });
            } else {
                Locale.sendTo(sender, "Command.Saves.Usage");
            }
        } else {
            Locale.sendTo(sender, "Command.Help");
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) throws IllegalArgumentException {
        if (args.length == 1 && args[0].equalsIgnoreCase("saves")) {
            return savesTab;
        }
        List<String> list = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
        return list;
    }

    @SuppressWarnings("deprecation")
    private UUID player(String player) {
        return Bukkit.getOfflinePlayer(player).getUniqueId();
    }
}