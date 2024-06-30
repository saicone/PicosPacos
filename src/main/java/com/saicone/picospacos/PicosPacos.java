package com.saicone.picospacos;

import com.saicone.ezlib.Dependencies;
import com.saicone.ezlib.Dependency;
import com.saicone.ezlib.EzlibLoader;
import com.saicone.picospacos.api.PicosPacosAPI;
import com.saicone.picospacos.core.data.Database;
import com.saicone.picospacos.core.paco.Paco;
import com.saicone.picospacos.module.Locale;
import com.saicone.picospacos.module.Settings;
import com.saicone.picospacos.module.cmd.CommandLoader;
import com.saicone.picospacos.module.hook.DeluxeCombatHook;
import com.saicone.picospacos.module.hook.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Dependencies({
        @Dependency(value = "com.github.Osiris-Team:Dream-Yaml:6.8", relocate = {"com.osiris.dyml", "{package}.libs.dyml"}),
        @Dependency(value = "com.saicone.rtag:rtag-item:1.5.4", relocate = {"com.saicone.rtag", "{package}.libs.rtag"})
})
public class PicosPacos extends JavaPlugin {

    private static PicosPacos instance;

    private static Settings settings;
    private Paco paco;

    private List<String> placeholderNames;

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
        }).replace("{package}", "com.saicone.picospacos").load();
    }

    @Override
    public void onEnable() {
        settings = new Settings(this, "settings.yml");
        settings.listener(this::onSettingsReload);
        paco = new Paco(this);
        if (Bukkit.getPluginManager().isPluginEnabled("DeluxeCombat")) {
            new DeluxeCombatHook(paco).load();
        }
    }

    @Override
    public void onDisable() {
        if (paco != null) paco.disable();
        CommandLoader.unload();
        if (placeholderNames != null) {
            Placeholders.unregister(placeholderNames);
            placeholderNames = null;
        }
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
        if (placeholderNames != null) {
            Placeholders.unregister(placeholderNames);
            placeholderNames = null;
        }
        if (settings.getBoolean("Hook.PlaceholderAPI.enabled")) {
            placeholderNames = Placeholders.register(this, settings.getStringList("Hook.PlaceholderAPI.names"), (player, params) -> {
                if (params.equalsIgnoreCase("saves")) {
                    return String.valueOf(PicosPacosAPI.getPlayer(player).getSaves());
                }
                return "Invalid placeholder";
            });
        }
        CommandLoader.reload();
        settings.setLocked(false);
    }

    @NotNull
    public static Settings getSettings() {
        return settings;
    }
}
