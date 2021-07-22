package me.rubenicos.mc.picospacos.module.cmd;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.util.LookupUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.util.List;
import java.util.Map;

public class CommandLoader {

    private static final PicosPacosCommand cmd = new PicosPacosCommand();
    private static final CommandMap map;

    static {
        CommandMap m = null;
        try {
            m = (CommandMap) LookupUtils.getField(Bukkit.getServer().getClass(), "commandMap").get(Bukkit.getServer());
            Class<? extends CommandMap> c1 = m.getClass();
            LookupUtils.addField("commands", c1.getSimpleName().equals("CraftCommandMap") ? c1.getSuperclass() : c1, "knownCommands");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        map = m;
    }

    @SuppressWarnings("unchecked")
    public static void reload() {
        cmd.setPermission(PicosPacos.getSettings().getString("Command.permission.use", "picospacos.use"));
        cmd.setPerms(PicosPacos.getSettings().getString("Command.permission.all", "picospacos.*"), PicosPacos.getSettings().getString("Command.permission.reload", "picospacos.command.reload"), PicosPacos.getSettings().getString("Command.permission.saves", "picospacos.command.saves"));

        Map<String, Command> commands;
        try {
            commands = (Map<String, Command>) LookupUtils.get("commands").invoke(map);
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        if (!cmd.isRegistered()) {
            commands.put("picospacos", cmd);
            cmd.register(map);
        }
        List<String> aliases = PicosPacos.getSettings().getStringList("Command.aliases");
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
            commands = (Map<String, Command>) LookupUtils.get("commands").invoke(map);
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
        cmd.unregister(map);
    }
}
