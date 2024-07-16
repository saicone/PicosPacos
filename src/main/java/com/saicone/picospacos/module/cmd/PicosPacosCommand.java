package com.saicone.picospacos.module.cmd;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.PicosPacosAPI;
import com.saicone.picospacos.module.Locale;
import com.saicone.types.Types;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PicosPacosCommand extends Command {

    private final PicosPacos pl = PicosPacos.get();
    private final List<String> helpTab = Arrays.asList("help", "reload", "saves");
    private final List<String> savesTab = Arrays.asList("give", "take", "set", "info");
    private String permAll = "picospacos.*";
    private String permReload = "picospacos.command.reload";
    private String permSaves = "picospacos.command.saves";

    PicosPacosCommand() {
        super("picospacos");
        setPermission("picospacos.*;picospacos.use");
    }

    public void setPerms(String permAll, String permUse, String permReload, String permSaves) {
        this.permAll = permAll;
        setPermission(permAll + ";" + permUse);
        this.permReload = permReload;
        this.permSaves = permSaves;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Locale.color("&6> PicosPacos Plugin"));
            sender.sendMessage(Locale.replaceArgs("&6> Version: &f {0}", pl.getDescription().getVersion()));
            sender.sendMessage(Locale.color("&6> Created by: &fRubenicos"));
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (hasPerm(sender, permReload)){
                Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
                    long init = System.currentTimeMillis();
                    pl.onReload();
                    Locale.sendTo(sender, "Command.Reload", String.valueOf(System.currentTimeMillis() - init));
                });
            }
        } else if (args[0].equalsIgnoreCase("saves")) {
            if (hasPerm(sender, permSaves)) {
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("info")) {
                        PicosPacosAPI.editPlayerData(args[2], data -> {
                            Locale.sendTo(sender, "Command.Saves.Info", args[2], String.valueOf(data.getSaves()));
                        });
                        return true;
                    } else if (args.length > 3) {
                        int amount = Types.INTEGER.parse(args[3], -1);
                        if (amount < 0) {
                            Locale.sendTo(sender, "Command.Saves.Invalid-Amount", args[3]);
                            return true;
                        } else {
                            PicosPacosAPI.editPlayerData(args[2], data -> {
                                switch (args[1].toLowerCase()) {
                                    case "give":
                                    case "add":
                                        data.addSaves(amount);
                                        Locale.sendTo(sender, "Command.Saves.Give", args[2], args[3]);
                                        break;
                                    case "take":
                                    case "remove":
                                        data.takeSaves(amount);
                                        Locale.sendTo(sender, "Command.Saves.Take", args[3], args[2]);
                                        break;
                                    case "set":
                                        data.setSaves(amount);
                                        Locale.sendTo(sender, "Command.Saves.Set", args[2], args[3]);
                                        break;
                                    default:
                                        Locale.sendTo(sender, "Command.Saves.Usage");
                                        break;
                                }
                            });
                            return true;
                        }
                    }
                }
                Locale.sendTo(sender, "Command.Saves.Usage");
            }
        } else {
            Locale.sendTo(sender, "Command.Help");
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) throws IllegalArgumentException {
        switch (args.length) {
            case 1:
                return helpTab;
            case 2:
                if (args[1].equalsIgnoreCase("saves")) {
                    return savesTab;
                } else {
                    return new ArrayList<>();
                }
            case 3:
                List<String> list = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
                return list;
            default:
                return new ArrayList<>();
        }
    }

    @Override
    public boolean testPermission(@NotNull CommandSender sender) {
        if (testPermissionSilent(sender)) {
            return true;
        } else {
            Locale.sendTo(sender, "Command.NoPerm");
            return false;
        }
    }

    private boolean hasPerm(CommandSender sender, String perm) {
        if (!sender.hasPermission(permAll) && !sender.hasPermission(perm)) {
            Locale.sendTo(sender, "Command.NoPerm");
            return false;
        }
        return true;
    }
}