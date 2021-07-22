package me.rubenicos.mc.picospacos;

import me.rubenicos.mc.picospacos.core.data.Database;
import me.rubenicos.mc.picospacos.core.paco.Paco;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.Settings;
import me.rubenicos.mc.picospacos.module.cmd.CommandLoader;
import me.rubenicos.mc.picospacos.module.hook.HookLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PicosPacos extends JavaPlugin {

    private static PicosPacos instance;

    private static Settings settings;
    private Paco paco;

    public static PicosPacos get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        settings = new Settings(this, "settings.yml");
        settings.listener(this::onSettingsReload);
        paco = new Paco(this);
    }

    @Override
    public void onDisable() {
        if (paco != null) paco.disable();
        CommandLoader.unload();
        HookLoader.unload();
        Database.Instance.unload();
    }

    public void reload() {
        onSettingsReload();
        paco.onRulesReload();
    }

    private void onSettingsReload() {
        if (!settings.reload()) {
            getLogger().severe("Cannot reload settings.yml file");
        }
        Locale.reload();
        Database.Instance.reload();
        HookLoader.reload();
        CommandLoader.reload();
    }

    @NotNull
    public static Settings getSettings() {
        return settings;
    }
}
