package com.saicone.picospacos;

import com.saicone.ezlib.Dependencies;
import com.saicone.ezlib.Dependency;
import com.saicone.ezlib.EzlibLoader;
import com.saicone.picospacos.core.Lang;
import com.saicone.picospacos.core.data.Database;
import com.saicone.picospacos.core.item.ActionRegistry;
import com.saicone.picospacos.core.item.ScriptRegistry;
import com.saicone.picospacos.module.cmd.CommandLoader;
import com.saicone.picospacos.module.hook.DeluxeCombatHook;
import com.saicone.picospacos.module.hook.Placeholders;
import com.saicone.picospacos.module.hook.PlayerProvider;
import com.saicone.picospacos.module.listener.BukkitListener;
import com.saicone.picospacos.module.settings.SettingsFile;
import com.saicone.picospacos.module.settings.SettingsUpdater;
import com.saicone.types.Types;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;

@Dependencies({
        @Dependency(value = "com.saicone:types:1.3.0", relocate = {"com.saicone.types", "{package}.libs.types"}),
        @Dependency(value = "com.saicone.rtag:rtag-item:1.5.11", relocate = {"com.saicone.rtag", "{package}.libs.rtag"}),
        @Dependency(value = "com.github.cryptomorin:XSeries:13.3.1", relocate = {"com.cryptomorin.xseries", "{package}.libs.xseries"})
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
    private final ActionRegistry actionRegistry;
    private final ScriptRegistry scriptRegistry;

    private Set<String> placeholderNames;

    public PicosPacos() {
        this.settings = new SettingsFile("settings.yml", true);
        this.lang = new Lang(this);
        this.database = new Database(this);
        this.actionRegistry = new ActionRegistry();
        this.scriptRegistry = new ScriptRegistry();

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
        SettingsUpdater.run(settings);
        lang.load();
        PlayerProvider.compute(settings.getIgnoreCase("plugin", "player-provider").asString("AUTO"));
        this.database.onLoad();
        getLogger().info("Loading actions");
        this.actionRegistry.load();
        getLogger().info("Loading scripts");
        this.scriptRegistry.load();
    }

    @Override
    public void onEnable() {
        this.database.onEnable();
        Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);

        // Register hooks
        registerPlaceholders();
        if (Bukkit.getPluginManager().isPluginEnabled("DeluxeCombat") && settings.getIgnoreCase("hook", "DeluxeCombat", "enabled").asBoolean(true)) {
            new DeluxeCombatHook().load();
        }

        CommandLoader.reload();
    }

    @Override
    public void onDisable() {
        CommandLoader.unload();
        unregisterPlaceholders();
        this.scriptRegistry.disable();
        this.database.onDisable();
    }

    public void onReload() {
        settings.loadFrom(getDataFolder(), true);
        lang.load();
        PlayerProvider.compute(settings.getIgnoreCase("plugin", "player-provider").asString("AUTO"));
        this.database.onReload();
        this.scriptRegistry.reload();
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
    public ActionRegistry actionRegistry() {
        return actionRegistry;
    }

    @NotNull
    public ScriptRegistry scriptRegistry() {
        return scriptRegistry;
    }

    private void registerPlaceholders() {
        if (settings.getIgnoreCase("hook", "PlaceholderAPI", "enabled").asBoolean(true)) {
            this.placeholderNames = Placeholders.register(this, settings.getIgnoreCase("hook", "PlaceholderAPI", "names").asSet(Types.STRING), (player, params) -> {
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
