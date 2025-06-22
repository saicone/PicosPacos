package com.saicone.picospacos.module.cmd;

import com.saicone.picospacos.PicosPacos;
import com.saicone.rtag.util.EasyLookup;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;

public class CommandLoader {

    private static final PicosPacosCommand cmd = new PicosPacosCommand();
    private static final CommandMap commandMap;
    private static final MethodHandle knownCommands;

    static {
        CommandMap mapObject = null;
        MethodHandle get$knownCommands = null;
        try {
            mapObject = (CommandMap) EasyLookup.unreflectGetter(Bukkit.getServer().getClass(), "commandMap").invoke(Bukkit.getServer());
            Class<? extends CommandMap> mapClass = mapObject.getClass();
            get$knownCommands = EasyLookup.unreflectGetter(mapClass.getSimpleName().equals("CraftCommandMap") ? mapClass.getSuperclass() : mapClass, "knownCommands");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        commandMap = mapObject;
        knownCommands = get$knownCommands;
    }

    @SuppressWarnings("unchecked")
    public static void reload() {
        cmd.setPerms(PicosPacos.settings().getString("command.permission.all", "picospacos.*"), PicosPacos.settings().getString("command.permission.use", "picospacos.use"), PicosPacos.settings().getString("command.permission.reload", "picospacos.command.reload"), PicosPacos.settings().getString("command.permission.saves", "picospacos.command.saves"));

        Map<String, Command> commands;
        try {
            commands = (Map<String, Command>) knownCommands.invoke(commandMap);
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        if (!cmd.isRegistered()) {
            commands.put("picospacos", cmd);
            cmd.register(commandMap);
        }
        List<String> aliases = PicosPacos.settings().getStringList("command.aliases");
        cmd.getAliases().forEach(alias -> {
            if (!aliases.contains(alias) && !alias.equalsIgnoreCase("picospacos") && commands.containsKey(alias) && commands.get(alias).getName().equalsIgnoreCase("picospacos")) {
                commands.remove(alias);
            }
        });
        aliases.forEach(alias -> {
            if (!cmd.getAliases().contains(alias) && !alias.equalsIgnoreCase("picospacos") && !commands.containsKey(alias)) {
                commands.put(alias, cmd);
            }
        });
        cmd.setAliases(aliases);
    }

    @SuppressWarnings("unchecked")
    public static void unload() {
        if (!cmd.isRegistered()) return;
        Map<String, Command> commands;
        try {
            commands = (Map<String, Command>) knownCommands.invoke(commandMap);
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        commands.remove("picospacos");
        cmd.getAliases().forEach(alias -> {
            if (commands.containsKey(alias) && commands.get(alias).getName().equalsIgnoreCase("picospacos")) {
                commands.remove(alias);
            }
        });
        cmd.unregister(commandMap);
    }
}
