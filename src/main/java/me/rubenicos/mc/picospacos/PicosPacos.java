package me.rubenicos.mc.picospacos;

import me.rubenicos.mc.picospacos.core.data.Database;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.Settings;
import org.bukkit.plugin.java.JavaPlugin;

public class PicosPacos extends JavaPlugin {

    private static PicosPacos instance;

    public static Settings SETTINGS;

    public static PicosPacos get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        SETTINGS = new Settings("settings.yml");
        SETTINGS.listener(this::onSettingsReload);

        Locale.reload();
        Database.reload();
    }

    @Override
    public void onDisable() {

    }

    private void onSettingsReload() {
        if (!SETTINGS.reload()) {
            getLogger().severe("Cannot reload settings.yml file! Check console.");
        }
        Locale.reload();
        Database.reload();
    }
}
