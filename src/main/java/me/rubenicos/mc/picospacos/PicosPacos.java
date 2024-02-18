package me.rubenicos.mc.picospacos;

import com.saicone.ezlib.Dependencies;
import com.saicone.ezlib.Dependency;
import com.saicone.ezlib.EzlibLoader;
import me.rubenicos.mc.picospacos.core.data.Database;
import me.rubenicos.mc.picospacos.core.paco.Paco;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.Settings;
import me.rubenicos.mc.picospacos.module.cmd.CommandLoader;
import me.rubenicos.mc.picospacos.module.hook.HookLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Dependencies({
        @Dependency(value = "com.github.Osiris-Team:Dream-Yaml:6.8", relocate = {"com.cryptomorin.xseries", "{package}.libs.dyml"}),
        @Dependency(value = "com.saicone.rtag:rtag-item:1.4.4", relocate = {"com.saicone.rtag", "{package}.libs.rtag"})
})
public class PicosPacos extends JavaPlugin {

    private static PicosPacos instance;

    private static Settings settings;
    private Paco paco;

    public static PicosPacos get() {
        return instance;
    }

    public PicosPacos() {
        instance = this;
    }

    @Override
    public void onLoad() {
        new EzlibLoader().logger((level, msg) -> {
            switch (level) {
                case 1:
                    getLogger().severe(msg);
                    break;
                case 2:
                    getLogger().warning(msg);
                    break;
                case 3:
                    getLogger().info(msg);
                    break;
                default:
                    break;
            }
        }).replace("{package}", "me.rubenicos.mc.picospacos").load();
    }

    @Override
    public void onEnable() {
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
        settings.removeListener();
    }

    public void reload() {
        onSettingsReload();
        settings.resolveListener();
        paco.onRulesReload();
    }

    private void onSettingsReload() {
        if (settings.isLocked()) return;
        settings.setLocked(true);
        if (!settings.reload()) {
            getLogger().severe("Cannot reload settings.yml file");
        }
        Locale.reload();
        Database.Instance.reload();
        HookLoader.reload();
        CommandLoader.reload();
        settings.setLocked(false);
    }

    @NotNull
    public static Settings getSettings() {
        return settings;
    }
}
