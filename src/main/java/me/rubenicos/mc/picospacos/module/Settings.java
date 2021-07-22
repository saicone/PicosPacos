package me.rubenicos.mc.picospacos.module;

import com.osiris.dyml.DYModule;
import com.osiris.dyml.DreamYaml;
import com.osiris.dyml.exceptions.DYReaderException;
import com.osiris.dyml.exceptions.DYWriterException;
import com.osiris.dyml.exceptions.DuplicateKeyException;
import com.osiris.dyml.exceptions.IllegalListException;
import com.osiris.dyml.watcher.DYFileEvent;
import com.osiris.dyml.watcher.DYFileEventListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardWatchEventKinds;
import java.util.*;

/**
 * Settings class for YAML files on plugin folder. <br>
 * Depends on {@link DreamYaml}.
 * @author Rubenicos
 * @version 2.0
 */
public class Settings extends DreamYaml {

    private final JavaPlugin plugin;

    private final Map<String, Boolean> sections = new HashMap<>();
    private final List<String> keys = new ArrayList<>();
    private final Map<String, Object> cache = new HashMap<>();

    private final String path;
    private final boolean update;
    private boolean defaultExists = true;

    private DreamYaml defYaml;
    private DYFileEventListener<DYFileEvent> listener;

    /**
     * Create a {@link Settings} object from Bukkit plugin.
     * Take in count this requires a path of YAML file inside
     * plugin folder.
     *
     * @param plugin                  Bukkit plugin who tries to create the object.
     * @param path                    Plugin file path to load the settings
     */
    public Settings(@NotNull JavaPlugin plugin, @NotNull String path) {
        this(plugin, path, path, true, true, true, false);
    }

    /**
     * Create a {@link Settings} object from Bukkit plugin.
     * Take in count this requires a path of YAML file inside
     * plugin folder.
     *
     * @param plugin                  Bukkit plugin who tries to create the object.
     * @param path                    Plugin file path to load the settings
     * @param defPath                 Default path to get a InputStream from plugin .jar file
     *                                in case of normal path doesn't exists inside plugin.
     * @param requireDef              The needed of default file, if true the plugin will
     *                                unloaded when error is present while loading InputStream.
     * @param update                  Set true to load new stuff from InputStream into file.
     */
    public Settings(@NotNull JavaPlugin plugin, @NotNull String path, String defPath, boolean requireDef, boolean update) {
        this(plugin, path, defPath, requireDef, update, true, false);
    }

    /**
     * Create a {@link Settings} object from Bukkit plugin.
     * Take in count this requires a path of YAML file inside
     * plugin folder.
     *
     * @param plugin                  Bukkit plugin who tries to create the object.
     * @param path                    Plugin file path to load the settings
     * @param defPath                 Default path to get a InputStream from plugin .jar file
     *                                in case of normal path doesn't exists inside plugin.
     * @param requireDef              The needed of default file, if true the plugin will
     *                                unloaded when error is present while loading InputStream.
     * @param update                  Set true to load new stuff from InputStream into file.
     * @param isPostProcessingEnabled (DreamYaml) Enabled by default. <br>
     *                                You can also enable/disable specific post-processing options individually: <br>
     *                                See {@link DreamYaml#isPostProcessingEnabled()} for details.
     * @param isDebugEnabled          (DreamYaml) Disabled by default. Shows debugging stuff.
     */
    public Settings(@NotNull JavaPlugin plugin, @NotNull String path, String defPath, boolean requireDef, boolean update, boolean isPostProcessingEnabled, boolean isDebugEnabled) {
        super(new File(plugin.getDataFolder() + File.separator + path), isPostProcessingEnabled, isDebugEnabled);
        this.plugin = plugin;
        this.path = path;
        this.update = update;
        if (defPath == null && !requireDef) return;
        InputStream in = plugin.getResource(path);
        if (in == null && defPath != null) {
            in = plugin.getResource(defPath);
        }
        if (in == null) {
            if (requireDef) {
                Bukkit.getLogger().severe("Cannot find " + defPath + " file on plugin JAR!");
                plugin.getPluginLoader().disablePlugin(plugin);
                return;
            }
            defaultExists = false;
        } else {
            try {
                defYaml = new DreamYaml(in).load();
            } catch (IllegalListException | IOException | DuplicateKeyException | DYReaderException e) {
                e.printStackTrace();
                defaultExists = false;
            }
            if (requireDef && !defaultExists) {
                Bukkit.getLogger().severe("Cannot load " + defPath + " file on plugin JAR!");
                plugin.getPluginLoader().disablePlugin(plugin);
            }
        }
    }

    public String getPath() {
        return path;
    }

    public boolean reload() {
        sections.clear();
        keys.clear();
        cache.clear();
        String path = plugin.getDataFolder() + File.separator + this.path;
        File file = new File(path);
        if (!file.exists()) {
            try {
                plugin.saveResource(this.path, false);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            load();
        } catch (IOException | IllegalListException | DYReaderException | DuplicateKeyException e) {
            e.printStackTrace();
            return false;
        }

        if (defaultExists && update) {
            getAllInEdit().clear();
            getAllInEdit().addAll(defYaml.getAllLoaded());
            try {
                save(true);
                load();
            } catch (IllegalListException | IOException | DYWriterException | DuplicateKeyException | DYReaderException e) {
                Bukkit.getLogger().severe("Cannot update " + this.path + " file on plugin folder!");
                e.printStackTrace();
            }
        }

        getAllLoaded().forEach(module -> keys.add(module.getFirstKey()));
        return true;
    }

    public void listener(Runnable runnable) {
        runnable.run();
        this.listener = (event) -> {
            if (event.getWatchEventKind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            }
        };
        addListener();
    }

    private void addListener() {
        try {
            addFileEventListener(listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public DYModule getModule(String path) {
        return get(path.split("\\."));
    }

    public boolean isSection(String path) {
        return sections.getOrDefault(path, isSection0(path));
    }

    private boolean isSection0(String path) {
        DYModule module = getModule(path);
        if (module != null && !module.getChildModules().isEmpty()) {
            sections.put(path, true);
            return true;
        } else {
            sections.put(path, false);
            return false;
        }
    }

    public List<String> getKeys() {
        return keys;
    }

    public List<String> getKeys(String path) {
        DYModule module = getModule(path);
        if (module != null && !module.getChildModules().isEmpty()) {
            return module.getKeys();
        } else {
            return Collections.emptyList();
        }
    }

    @NotNull
    public String getString(@NotNull String path, String def) {
        return String.valueOf(cache.getOrDefault(path, cache(path, getString0(path, def))));
    }

    @NotNull
    public String getString(@NotNull String path) {
        return String.valueOf(cache.getOrDefault(path, cache(path, getString0(path, "null"))));
    }

    private Object getString0(String path, String def) {
        DYModule module = getModule(path);
        if (module == null) {
            return def;
        } else {
            return module.asString();
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public List<String> getStringList(@NotNull String path) {
        return (List<String>) cache.getOrDefault(path, cache(path, getStringList0(path)));
    }

    private Object getStringList0(String path) {
        DYModule module = getModule(path);
        if (module == null) {
            return Collections.emptyList();
        } else {
            List<String> list = module.asStringList();
            if (list == null) {
                String s = module.asString();
                if (s == null) {
                    return Collections.emptyList();
                } else {
                    return Collections.singleton(s);
                }
            } else if (list.isEmpty()) {
                return Collections.emptyList();
            } else {
                return list;
            }
        }
    }

    public int getInt(@NotNull String path, int def) {
        return (int) cache.getOrDefault(path, cache(path, getInt0(path, def)));
    }

    public int getInt(@NotNull String path) {
        return (int) cache.getOrDefault(path, cache(path, getInt0(path, -1)));
    }

    private Object getInt0(String path, int def) {
        DYModule module = getModule(path);
        if (module == null) {
            return def;
        } else {
            try {
                return Integer.parseInt(module.asString());
            } catch (NumberFormatException e) {
                return def;
            }
        }
    }

    public boolean getBoolean(@NotNull String path) {
        return (boolean) cache.getOrDefault(path, cache(path, getBoolean0(path)));
    }

    private Object getBoolean0(String path) {
        DYModule module = getModule(path);
        if (module == null) {
            return false;
        } else {
            return module.asBoolean();
        }
    }

    private Object cache(String path, Object obj) {
        cache.put(path, obj);
        return obj;
    }
}
