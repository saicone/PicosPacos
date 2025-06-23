package com.saicone.picospacos.module.settings;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.module.settings.updater.Updater_v1;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingsUpdater {

    private static final int CURRENT_VERSION = 1;

    public static void run(@NotNull SettingsFile settings) {
        final int version = settings.getInt("version", 0);
        if (version == CURRENT_VERSION) {
            return;
        }
        if (version < 1) {
            Updater_v1.update(settings);
        }
        settings.set("version", CURRENT_VERSION);
        save(settings, new File(PicosPacos.get().getDataFolder(), "settings.yml"));
    }

    protected static void save(@NotNull YamlConfiguration config, @NotNull File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void move(@NotNull ConfigurationSection section, @NotNull Map<String, String> paths) {
        for (Map.Entry<String, String> entry : paths.entrySet()) {
            String fromPath = entry.getKey();
            final String toPath = entry.getValue();
            if (toPath.isBlank()) {
                section.set(fromPath, null);
                continue;
            }

            if (fromPath.startsWith("#")) {
                fromPath = fromPath.substring(1);
            } else {
                final Object value = section.get(fromPath);
                section.set(toPath, value);
            }
            if (SettingsFile.ALLOW_COMMENTS) {
                section.setComments(toPath, section.getComments(fromPath));
                section.setComments(toPath, section.getComments(fromPath));
            }
            section.set(fromPath, null);
        }
    }

    protected static void comment(@NotNull ConfigurationSection section, @NotNull Map<String, List<String>> paths) {
        if (SettingsFile.ALLOW_COMMENTS) {
            for (Map.Entry<String, List<String>> entry : paths.entrySet()) {
                section.setComments(entry.getKey(), entry.getValue().stream().map(s -> " " + s).collect(Collectors.toList()));
            }
        }
    }
}
