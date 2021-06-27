package me.rubenicos.mc.picospacos;

import me.rubenicos.mc.picospacos.core.data.Database;
import me.rubenicos.mc.picospacos.core.Paco;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.Settings;
import me.rubenicos.mc.picospacos.module.hook.HookLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class PicosPacos extends JavaPlugin {

    private static PicosPacos instance;

    public static final Settings SETTINGS = new Settings();
    private Paco paco;

    public static PicosPacos get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        SETTINGS.init("settings.yml");
        SETTINGS.listener(this::onSettingsReload);

        paco = new Paco(this);
    }

    @Override
    public void onDisable() {
        paco.disable();
        HookLoader.unload();
        Database.unload();
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
