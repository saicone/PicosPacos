package me.rubenicos.mc.picospacos.module.hook;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.module.hook.type.HookPlaceholderAPI;
import me.rubenicos.mc.picospacos.module.hook.type.HookVault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HookLoader {

    private static final List<HookType> hooks = Arrays.asList(
            new HookPlaceholderAPI(),
            new HookVault()
    );
    private static final List<String> loaded = new ArrayList<>();

    public static void reload() {
        hooks.forEach(hook -> {
            if (loaded.contains(hook.pluginName())) {
                if (PicosPacos.SETTINGS().getBoolean("Hook." + hook.pluginName() + ".enabled")) {
                    hook.reload();
                } else {
                    hook.unload();
                    loaded.remove(hook.pluginName());
                }
            } else if (PicosPacos.SETTINGS().getBoolean("Hook." + hook.pluginName() + ".enabled")) {
                hook.load();
                loaded.add(hook.pluginName());
            }
        });
    }

    public static void unload() {
        hooks.forEach(hook -> {
            if (loaded.contains(hook.pluginName())) {
                hook.unload();
            }
        });
    }
}
