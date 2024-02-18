package com.saicone.picospacos.module.hook;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.module.hook.type.HookPlaceholderAPI;
import com.saicone.picospacos.module.hook.type.HookVault;

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
                if (PicosPacos.getSettings().getBoolean("Hook." + hook.pluginName() + ".enabled")) {
                    hook.reload();
                } else {
                    hook.unload();
                    loaded.remove(hook.pluginName());
                }
            } else if (PicosPacos.getSettings().getBoolean("Hook." + hook.pluginName() + ".enabled")) {
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
