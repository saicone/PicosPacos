package me.rubenicos.mc.picospacos.module.cmd;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.util.LookupUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.Map;

public class CommandLoader {

    private static final PicosPacosCommand cmd = new PicosPacosCommand();
    private static final CommandMap map;

    static {
        CommandMap m = null;
        try {
            m = (CommandMap) LookupUtils.getField(Bukkit.getServer().getClass(), "commandMap", CommandMap.class).invoke(Bukkit.getServer());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        map = m;
    }

    public static void reload() {
        if (!cmd.isRegistered()) {
            map.register("picospacos", cmd);
        }
        cmd.setAliases(PicosPacos.SETTINGS.getStringList("Command.aliases"));
        cmd.setPermission(PicosPacos.SETTINGS.getString("Command.permission.use"));
    }

    @SuppressWarnings("unchecked")
    public static void unload() {
        Class<? extends CommandMap> m = map.getClass();
        Map<String, Command> commands;
        try {
            final Field cmds = m.getSimpleName().equals("CraftCommandMap") ? m.getSuperclass().getDeclaredField("knownCommands") : m.getDeclaredField("knownCommands");
            cmds.setAccessible(true);
            commands = (Map<String, Command>) cmds.get(map);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
        commands.remove(cmd.getName());
        cmd.getAliases().forEach(alias -> {
            if (commands.containsKey(alias) && commands.get(alias).toString().contains(cmd.getName())) {
                commands.remove(alias);
            }
        });
        cmd.unregister(map);
    }
}
