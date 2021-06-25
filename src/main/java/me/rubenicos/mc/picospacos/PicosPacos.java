package me.rubenicos.mc.picospacos;

import me.rubenicos.mc.picospacos.core.data.Database;
import me.rubenicos.mc.picospacos.core.paco.Paco;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.Settings;
import me.rubenicos.mc.picospacos.module.hook.HookLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class PicosPacos extends JavaPlugin {

    private static PicosPacos instance;

    private static Settings SETTINGS;
    private Paco paco;

    public static PicosPacos get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        SETTINGS = new Settings("settings.yml");
        SETTINGS.listener(this::onSettingsReload);

        paco = new Paco(this);
    }

    @Override
    public void onDisable() {
        paco.disable();
        HookLoader.unload();
        Database.unload();
    }

    public static Settings SETTINGS() {
        return SETTINGS;
    }

    private void onSettingsReload() {
        if (!SETTINGS.reload()) {
            getLogger().severe("Cannot reload settings.yml file");
        }
        Locale.reload();
        Database.reload();
        HookLoader.reload();
    }
}
