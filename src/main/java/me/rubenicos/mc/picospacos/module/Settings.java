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

    private final Map<String, Section> sections = new HashMap<>();
    private final List<String> keys = new ArrayList<>();
    private final List<String> deepKeys = new ArrayList<>();
    private final Map<String, Object> cache = new HashMap<>();

    private final String path;
    private final boolean update;
    private boolean defaultExists = true;

    private DreamYaml defYaml;
    private DYFileEventListener<DYFileEvent> listener;
    private boolean fileListener = false;
    private boolean locked = false;

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
                plugin.getLogger().severe("Cannot find " + defPath + " file on plugin JAR!");
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
                plugin.getLogger().severe("Cannot load " + defPath + " file on plugin JAR!");
                plugin.getPluginLoader().disablePlugin(plugin);
            }
        }
    }

    public String getPath() {
        return path;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
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
            List<List<String>> paths = new ArrayList<>();
            getAllLoaded().forEach(module -> {
                paths.add(module.getKeys());
                getAllInEdit().add(module);
            });
            defYaml.getAllLoaded().forEach(module -> {
                if (!paths.contains(module.getKeys())) {
                    getAllLoaded().add(module);
                    getAllInEdit().add(module);
                }
            });
            try {
                save(true);
                load();
            } catch (IllegalListException | IOException | DYWriterException | DuplicateKeyException | DYReaderException e) {
                plugin.getLogger().severe("Cannot update " + this.path + " file on plugin folder!");
                e.printStackTrace();
            }
        }

        getAllLoaded().forEach(module -> {
            List<String> k = module.getKeys();
            if (!keys.contains(k.get(0))) {
                keys.add(k.get(0));
            }
            deepKeys.add(String.join(".", k));
        });
        return true;
    }

    public void listener(Runnable runnable) {
        runnable.run();
        this.listener = (event) -> {
            if (event.getWatchEventKind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            }
        };
        resolveListener();
    }

    public void resolveListener() {
        if (listener == null) return;
        if (getBoolean("File-Listener")) {
            if (!fileListener) {
                fileListener = true;
                addListener();
            }
        } else {
            if (fileListener) {
                fileListener = false;
                removeListener();
            }
        }
    }

    private void addListener() {
        try {
            addFileEventListener(listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeListener() {
        try {
            removeFileEventListener(listener);
        } catch (NullPointerException ignored) { }
    }

    @Nullable
    public DYModule getModule(String path) {
        return getModule(Arrays.asList(path.split("\\.")));
    }

    private DYModule getModule(List<String> keys) {
        for (DYModule module : getAllLoaded()) {
            if (module.getKeys().equals(keys)) return module;
        }
        return null;
    }

    public Section getSection(String path) {
        return sections.getOrDefault(path, getSection0(path));
    }

    private Section getSection0(String path) {
        DYModule module = getModule(path);
        if (module != null && !module.getChildModules().isEmpty()) {
            sections.put(path, new Section(module));
        } else {
            sections.put(path, null);
        }
        return sections.get(path);
    }

    public boolean isSection(String path) {
        return getSection(path) != null;
    }

    public List<String> getKeys() {
        return getKeys(false);
    }

    public List<String> getKeys(boolean deep) {
        return deep ? deepKeys : keys;
    }

    public List<String> getKeys(String path) {
        return getKeys(path, false);
    }

    public List<String> getKeys(String path, boolean deep) {
        if (isSection(path)) {
            return sections.get(path).getKeys(deep);
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
        Object obj = cache.getOrDefault(path, cache(path, getStringList0(path)));
        if (obj instanceof List) {
            return (List<String>) obj;
        } else {
            return Collections.singletonList(String.valueOf(obj));
        }
    }

    private Object getStringList0(String path) {
        DYModule module = getModule(path);
        if (module == null) {
            return new ArrayList<>();
        } else {
            List<String> list = module.asStringList();
            if (list == null) {
                list = new ArrayList<>();
                String s = module.asString();
                if (s != null) {
                    list.add(s);
                }
            }
            return list;
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

    public static final class Section {

        private final List<String> keys = new ArrayList<>();
        private final List<String> deepKeys = new ArrayList<>();

        public Section(DYModule module) {
            module.getChildModules().forEach(m -> {
                List<String> path = m.getKeys();
                path = path.subList(module.getKeys().size(), path.size());
                keys.add(path.get(0));
                deepKeys.add(String.join(".", path));
            });
        }

        public List<String> getKeys() {
            return getKeys(false);
        }

        public List<String> getKeys(boolean deep) {
            return deep ? deepKeys : keys;
        }
    }
}
