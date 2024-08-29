package com.saicone.picospacos;

import com.saicone.ezlib.Dependencies;
import com.saicone.ezlib.Dependency;
import com.saicone.ezlib.EzlibLoader;
import com.saicone.picospacos.core.Lang;
import com.saicone.picospacos.core.data.Database;
import com.saicone.picospacos.core.paco.Paco;
import com.saicone.picospacos.module.cmd.CommandLoader;
import com.saicone.picospacos.module.hook.DeluxeCombatHook;
import com.saicone.picospacos.module.hook.Placeholders;
import com.saicone.picospacos.module.hook.PlayerProvider;
import com.saicone.picospacos.module.settings.SettingsFile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@Dependencies({
        @Dependency(value = "com.saicone:types:1.1", relocate = {"com.saicone.types", "{package}.libs.types"}),
        @Dependency(value = "com.saicone.rtag:rtag-item:1.5.4", relocate = {"com.saicone.rtag", "{package}.libs.rtag"})
})
public class PicosPacos extends JavaPlugin {

    private static PicosPacos instance;

    @NotNull
    public static PicosPacos get() {
        return instance;
    }

    @NotNull
    public static SettingsFile settings() {
        return instance.getSettings();
    }

    public static void log(int level, @NotNull Supplier<String> msg, @Nullable Object... args) {
        instance.getLang().sendLog(level, msg, args);
    }

    public static void log(int level, @NotNull String msg, @Nullable Object... args) {
        instance.getLang().sendLog(level, msg, args);
    }

    public static void logException(int level, @NotNull Throwable throwable) {
        instance.getLang().printStackTrace(level, throwable);
    }

    public static void logException(int level, @NotNull Throwable throwable, @NotNull Supplier<String> msg, @Nullable Object... args) {
        instance.getLang().printStackTrace(level, throwable, msg, args);
    }

    public static void logException(int level, @NotNull Throwable throwable, @NotNull String msg, @Nullable Object... args) {
        instance.getLang().printStackTrace(level, throwable, msg, args);
    }

    private final SettingsFile settings;
    private final Lang lang;
    private final Database database;
    private final Paco paco;

    private List<String> placeholderNames;

    public PicosPacos() {
        this.settings = new SettingsFile("settings.yml", true);
        lang = new Lang(this);
        this.database = new Database(this);
        this.paco = new Paco(this);

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
    public void onLoad() {
        instance = this;
        settings.loadFrom(getDataFolder(), true);
        lang.load();
        PlayerProvider.compute(settings.getIgnoreCase("plugin", "playerprovider").asString("AUTO"));
        this.database.onLoad();
        this.paco.onLoad();
    }

    @Override
    public void onEnable() {
        this.database.onEnable();
        this.paco.onEnable();

        // Register hooks
        registerPlaceholders();
        if (Bukkit.getPluginManager().isPluginEnabled("DeluxeCombat") && settings.getBoolean("Hook.DeluxeCombat.enabled", true)) {
            new DeluxeCombatHook(paco).load();
        }

        CommandLoader.reload();
    }

    @Override
    public void onDisable() {
        CommandLoader.unload();
        unregisterPlaceholders();
        this.paco.onDisable();
        this.database.onDisable();
    }

    public void onReload() {
        settings.loadFrom(getDataFolder(), true);
        lang.load();
        PlayerProvider.compute(settings.getIgnoreCase("plugin", "playerprovider").asString("AUTO"));
        this.database.onReload();
        this.paco.onReload();
        unregisterPlaceholders();
        registerPlaceholders();
        CommandLoader.reload();
    }

    @NotNull
    public SettingsFile getSettings() {
        return settings;
    }

    @NotNull
    public Lang getLang() {
        return lang;
    }

    @NotNull
    public Database getDatabase() {
        return database;
    }

    @NotNull
    public Paco getPaco() {
        return paco;
    }

    private void registerPlaceholders() {
        if (settings.getBoolean("Hook.PlaceholderAPI.enabled", true)) {
            this.placeholderNames = Placeholders.register(this, settings.getStringList("Hook.PlaceholderAPI.names"), (player, params) -> {
                if (params.equalsIgnoreCase("saves")) {
                    return String.valueOf(this.database.getPlayerDataAsync(player).join().getSaves());
                }
                return "Invalid placeholder";
            });
        }
    }

    private void unregisterPlaceholders() {
        if (this.placeholderNames != null) {
            Placeholders.unregister(this.placeholderNames);
            this.placeholderNames = null;
        }
    }
}
