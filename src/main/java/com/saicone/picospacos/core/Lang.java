package com.saicone.picospacos.core;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.module.lang.LangLoader;
import com.saicone.picospacos.module.settings.BukkitSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lang extends LangLoader {

    public static final Value COMMAND_PERMISSION = new Value("command.permission");
    public static final Value COMMAND_HELP = new Value("command.help");
    public static final Value COMMAND_RELOAD = new Value("command.reload");
    public static final Value COMMAND_SAVES_USAGE = new Value("command.saves.usage");
    public static final Value COMMAND_SAVES_INVALID = new Value("command.saves.invalid");
    public static final Value COMMAND_SAVES_GIVE = new Value("command.saves.give");
    public static final Value COMMAND_SAVES_TAKE = new Value("command.saves.take");
    public static final Value COMMAND_SAVES_SET = new Value("command.savesS.set");
    public static final Value COMMAND_SAVES_INFO = new Value("command.saves.info");


    private int logLevel;
    private Map<String, String> languageAliases;
    private String pluginLanguage;
    private String defaultLanguage;

    private final List<String> defaultLanguages = List.of("en_US", "es_ES");

    public Lang(@NotNull Plugin plugin) {
        super(plugin);
    }

    @Override
    public void load(@NotNull File langFolder) {
        logLevel = PicosPacos.settings().getIgnoreCase("plugin", "log-level").asInt(3);
        if (languageAliases != null) {
            languageAliases.clear();
        } else {
            languageAliases = new HashMap<>();
        }
        final ConfigurationSection section = PicosPacos.settings().getConfigurationSection(settings -> settings.getIgnoreCase("lang", "aliases"));
        if (section != null) {
            for (String key : section.getKeys(false)) {
                final Object aliases = section.get(key);
                if (aliases instanceof List) {
                    for (Object alias : (List<?>) aliases) {
                        languageAliases.put(key.toLowerCase(), String.valueOf(alias).toLowerCase());
                    }
                } else if (aliases != null) {
                    languageAliases.put(key.toLowerCase(), String.valueOf(aliases).toLowerCase());
                }
            }
        }
        pluginLanguage = PicosPacos.settings().getIgnoreCase("plugin", "language").asString("en_US");
        defaultLanguage = PicosPacos.settings().getIgnoreCase("lang", "default").asString("en_US");
        super.load(langFolder);
    }

    @Override
    protected @NotNull Map<String, Object> getFileObjects(@NotNull File file, @NotNull Value[] values) {
        final Map<String, Object> map = new HashMap<>();
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
            for (Value value : values) {
                Object object = config.get(value.getPath());
                if (object == null) {
                    continue;
                }
                if (object instanceof Map || object instanceof ConfigurationSection) {
                    object = BukkitSettings.of(object);
                }
                map.put(value.getPath(), object);
            }
            for (String path : config.getKeys(true)) {
                boolean ignore = false;
                for (Value value : values) {
                    if (path.startsWith(value.getPath())) {
                        ignore = true;
                        break;
                    }
                }
                if (ignore) {
                    continue;
                }
                map.put(path, config.get(path));
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public @NotNull Map<String, String> getLanguageAliases() {
        return languageAliases;
    }

    @NotNull
    @Override
    public String getPluginLanguage() {
        return pluginLanguage;
    }

    @NotNull
    @Override
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    @NotNull
    @Override
    public List<String> getDefaultLanguages() {
        return defaultLanguages;
    }
}