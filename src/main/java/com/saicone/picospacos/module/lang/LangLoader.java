/*
 * This file is part of PixelBuy, licensed under the MIT License
 *
 * Copyright (c) 2024 Rubenicos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.saicone.picospacos.module.lang;

import com.saicone.picospacos.module.lang.display.ActionbarDisplay;
import com.saicone.picospacos.module.lang.display.TextDisplay;
import com.saicone.picospacos.module.lang.display.TitleDisplay;
import com.saicone.picospacos.module.settings.BukkitSettings;
import com.saicone.picospacos.util.MStrings;
import com.saicone.picospacos.util.Strings;
import com.saicone.types.IterableType;
import com.saicone.types.ValueType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class LangLoader implements Listener {

    protected static final String DEFAULT_LANGUAGE = "en_us";

    protected static final List<String> DEFAULT_LANGUAGES = List.of("en_US");

    protected static final Map<String, String> LANGUAGE_ALIASES = Map.of(
            "en_au", "en_us",
            "en_ca", "en_us",
            "en_gb", "en_us",
            "en_nz", "en_us",
            "es_ar", "es_es",
            "es_cl", "es_es",
            "es_ec", "es_es",
            "es_mx", "es_es",
            "es_uy", "es_es",
            "es_ve", "es_es"
    );

    private final Plugin plugin;

    private Value[] paths = new Value[0];

    protected String defaultLanguage = null;
    protected final Map<String, String> languageAliases = new HashMap<>();
    protected final Map<String, String> playerLanguages = new HashMap<>();

    protected String filePrefix = ".yml";
    protected final Map<String, Map<String, LangDisplay>> displays = new HashMap<>();

    public LangLoader(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        load(new File(plugin.getDataFolder(), "lang"));
    }

    public void load(@NotNull File langFolder) {
        defaultLanguage = getDefaultLanguage().toLowerCase();
        languageAliases.clear();
        languageAliases.putAll(getLanguageAliases());

        if (!langFolder.exists()) {
            langFolder.mkdirs();

        }
        computePaths();

        final Map<String, List<File>> langFiles = getLangFiles(langFolder);
        for (String defaultLanguage : getDefaultLanguages()) {
            final String key = defaultLanguage.toLowerCase();
            if (!langFiles.containsKey(key)) {
                final File file = saveDefaultLang(langFolder, defaultLanguage);
                if (file != null) {
                    final List<File> list = new ArrayList<>();
                    list.add(file);
                    langFiles.put(key, list);
                }
            }
        }

        langFiles.forEach((key, list) -> list.forEach(file -> loadDisplays(key, file)));
    }

    protected void loadDisplays(@NotNull String name, @NotNull File file) {
        String prefix = "&6Picos&ePacos &8» ";
        for (var entry : getObjects(file).entrySet()) {
            if (entry.getKey().equalsIgnoreCase("prefix") && entry.getValue() instanceof String) {
                prefix = (String) entry.getValue();
                continue;
            }
            final LangDisplay display = loadDisplay(name, prefix, entry.getKey(), entry.getValue());
            if (display != null) {
                if (!displays.containsKey(name)) {
                    displays.put(name, new HashMap<>());
                }
                final Map<String, LangDisplay> map = displays.get(name);
                map.put(entry.getKey().toLowerCase(), display);
            }
        }
    }

    @Nullable
    protected LangDisplay loadDisplay(@NotNull String name, @NotNull String prefix, @NotNull String path, @Nullable Object object) {
        if (object instanceof BukkitSettings) {
            final BukkitSettings config = (BukkitSettings) object;
            final String type = config.getIgnoreCase("type").asString();
            if (type == null) {
                sendLog(2, "The language " + name + " doesn't have type at '" + path + "' path");
                return null;
            }
            switch (type.toLowerCase()) {
                case "actionbar":
                    return new ActionbarDisplay(config.getRegex("(?i)actionbar|text|value").asString("").replace("{prefix}", prefix));
                case "title":
                    final String title = config.getRegex("(?i)title|text|value").asString();
                    final String subtitle = config.getIgnoreCase("subtitle").asString();
                    final int fadeIn = config.getIgnoreCase("fadeIn").asInt(10);
                    final int stay = config.getIgnoreCase("stay").asInt(70);
                    final int fadeOut = config.getIgnoreCase("fadeOut").asInt(20);
                    return new TitleDisplay(
                            title == null ? null : title.replace("{prefix}", prefix),
                            subtitle == null ? null : subtitle.replace("{prefix}", prefix),
                            fadeIn,
                            stay,
                            fadeOut
                    );
                default:
                    sendLog(2, "The language " + name + " doesn't have a valid type at '" + path + "' path");
                    return null;
            }
        } else if (object != null) {
            final List<String> text = new ArrayList<>();
            for (Object obj : IterableType.of(object)) {
                text.add(MStrings.color(String.valueOf(obj).replace("{prefix}", prefix)));
            }
            return new TextDisplay(text);
        }
        sendLog(2, "The language " + name + " has invalid path at '" + path + "'");
        return null;
    }

    private void computePaths() {
        if (paths.length > 0) {
            return;
        }
        final List<Value> paths = new ArrayList<>();
        // Check every superclass
        for (Class<?> clazz = getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        final Value path = (Value) field.get(null);
                        path.setLoader(this);
                        paths.add(path);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        this.paths = paths.toArray(new Value[0]);
    }

    public void unload() {
        languageAliases.clear();
        playerLanguages.clear();
        for (var entry : displays.entrySet()) {
            entry.getValue().clear();
        }
        displays.clear();
    }

    @Nullable
    protected File saveDefaultLang(@NotNull File folder, @NotNull String name) {
        try {
            plugin.saveResource("lang/" + name + getFilePrefix(), false);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return new File(folder, name + getFilePrefix());
    }

    public void setFilePrefix(@NotNull String filePrefix) {
        this.filePrefix = filePrefix;
    }

    @NotNull
    protected Map<String, List<File>> getLangFiles(@NotNull File langFolder) {
        final Map<String, List<File>> map = new HashMap<>();
        final File[] files = langFolder.listFiles();
        if (files == null) {
            return map;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                getLangFiles(langFolder).forEach((key, list) -> map.computeIfAbsent(key, s -> new ArrayList<>()).addAll(list));
            } else {
                final int index = file.getName().lastIndexOf('.');
                final String name = index >= 1 ? file.getName().substring(0, index) : file.getName();
                map.computeIfAbsent(name.toLowerCase(), s -> new ArrayList<>()).add(file);
            }
        }
        return map;
    }

    @NotNull
    private Map<String, Object> getObjects(@NotNull File file) {
        return getFileObjects(file, paths);
    }

    @NotNull
    protected abstract Map<String, Object> getFileObjects(@NotNull File file, @NotNull Value[] values);

    public int getLogLevel() {
        return 2;
    }

    @NotNull
    public Value[] getPaths() {
        return paths;
    }

    @NotNull
    public String getLanguage(@NotNull String lang) {
        if (displays.containsKey(lang)) {
            return lang;
        } else {
            return languageAliases.getOrDefault(lang, defaultLanguage);
        }
    }

    @NotNull
    public String getLanguage(@NotNull CommandSender sender) {
        if (sender instanceof Player) {
            return getPlayerLanguage((Player) sender);
        } else {
            return getPluginLanguage();
        }
    }

    @NotNull
    public Map<String, String> getLanguageAliases() {
        return LANGUAGE_ALIASES;
    }

    @NotNull
    public String getPluginLanguage() {
        return DEFAULT_LANGUAGE;
    }

    @NotNull
    public String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    @NotNull
    public List<String> getDefaultLanguages() {
        return DEFAULT_LANGUAGES;
    }

    @NotNull
    public String getPlayerLanguage(@NotNull Player player) {
        final String name = player.getName();
        if (!playerLanguages.containsKey(name)) {
            playerLanguages.put(name, getLanguage(player.getLocale().toLowerCase()));
        }
        return playerLanguages.get(name);
    }

    @NotNull
    public Map<String, String> getPlayerLanguages() {
        return playerLanguages;
    }

    @NotNull
    public String getFilePrefix() {
        return filePrefix;
    }

    @NotNull
    public Map<String, Map<String, LangDisplay>> getDisplays() {
        return displays;
    }

    @NotNull
    public Map<String, LangDisplay> getDisplays(@NotNull CommandSender sender) {
        return getDisplays(getLanguage(sender));
    }

    @NotNull
    public Map<String, LangDisplay> getDisplays(@NotNull String language) {
        final Map<String, LangDisplay> map = getDisplaysOrNull(language);
        if (map != null) {
            return map;
        } else if (!language.equals(defaultLanguage)) {
            return displays.getOrDefault(defaultLanguage, Map.of());
        } else {
            return Map.of();
        }
    }

    @Nullable
    public Map<String, LangDisplay> getDisplaysOrNull(@NotNull CommandSender sender) {
        return getDisplaysOrNull(getLanguage(sender));
    }

    @Nullable
    public Map<String, LangDisplay> getDisplaysOrNull(@NotNull String language) {
        return displays.get(language);
    }

    @NotNull
    public LangDisplay getDisplay(@NotNull String path) {
        return getDisplay(getPluginLanguage(), path);
    }

    @NotNull
    public LangDisplay getDisplay(@NotNull CommandSender sender, @NotNull String path) {
        return getDisplay(getLanguage(sender), path);
    }

    @NotNull
    public LangDisplay getDisplay(@NotNull String language, @NotNull String path) {
        final LangDisplay display = getDisplayOrNull(language, path);
        if (display != null) {
            return display;
        } else if (!language.equals(defaultLanguage)) {
            return getDefaultDisplay(path);
        } else {
            return LangDisplay.EMPTY;
        }
    }

    @Nullable
    public LangDisplay getDisplayOrNull(@NotNull CommandSender sender, @NotNull String path) {
        return getDisplayOrNull(getLanguage(sender), path);
    }

    @Nullable
    public LangDisplay getDisplayOrNull(@NotNull String language, @NotNull String path) {
        return getDisplays(language).get(path.toLowerCase());
    }

    @NotNull
    public LangDisplay getDefaultDisplay(@NotNull String path) {
        return getDisplays(defaultLanguage).getOrDefault(path.toLowerCase(), LangDisplay.EMPTY);
    }

    @Nullable
    public LangDisplay getDefaultDisplayOrNull(@NotNull String path) {
        return getDisplays(defaultLanguage).get(path.toLowerCase());
    }

    @NotNull
    public String getLangText(@NotNull String path) {
        final String text = getLangTextOrNull(path);
        return text == null ? "" : text;
    }

    @NotNull
    public String getLangText(@NotNull CommandSender sender, @NotNull String path) {
        final String text = getLangTextOrNull(sender, path);
        return text == null ? "" : text;
    }

    @Nullable
    public String getLangTextOrNull(@NotNull String path) {
        final LangDisplay display = getDisplay(getPluginLanguage(), path);
        return display == LangDisplay.EMPTY ? null : display.getText();
    }

    @Nullable
    public String getLangTextOrNull(@NotNull CommandSender sender, @NotNull String path) {
        final LangDisplay display = getDisplay(getLanguage(sender), path);
        return display == LangDisplay.EMPTY ? null : display.getText();
    }

    public void printStackTrace(int level, @NotNull Throwable throwable) {
        if (getLogLevel() >= level) {
            throwable.printStackTrace();
        }
    }

    public void printStackTrace(int level, @NotNull Throwable throwable, @NotNull Supplier<String> msg, @Nullable Object... args) {
        sendLog(level, msg, args);
        printStackTrace(level, throwable);
    }

    public void printStackTrace(int level, @NotNull Throwable throwable, @NotNull String msg, @Nullable Object... args) {
        sendLog(level, msg, args);
        printStackTrace(level, throwable);
    }

    public void sendLog(int level, @NotNull Supplier<String> msg, @Nullable Object... args) {
        if (getLogLevel() < level) {
            return;
        }
        for (String s : Strings.replaceArgs(msg.get(), args).split("\n")) {
            switch (level) {
                case 1:
                    plugin.getLogger().severe(s);
                    break;
                case 2:
                    plugin.getLogger().warning(s);
                    break;
                case 3:
                case 4:
                default:
                    plugin.getLogger().info(s);
                    break;
            }
        }
    }

    public void sendLog(int level, @NotNull String msg, @Nullable Object... args) {
        if (getLogLevel() < level) {
            return;
        }
        for (String s : Strings.replaceArgs(msg, args).split("\n")) {
            switch (level) {
                case 1:
                    plugin.getLogger().severe(s);
                    break;
                case 2:
                    plugin.getLogger().warning(s);
                    break;
                case 3:
                case 4:
                default:
                    plugin.getLogger().info(s);
                    break;
            }
        }
    }

    public void sendTo(@NotNull CommandSender sender, @NotNull String path, @Nullable Object... args) {
        sendTo(sender, getLanguage(sender), path, args);
    }

    protected void sendTo(@NotNull CommandSender sender, @NotNull String language, @NotNull String path, @Nullable Object... args) {
        sendTo(sender, language, path, s -> Strings.replaceArgs(s, args));
    }

    public void sendTo(@NotNull CommandSender sender, @NotNull String path, @NotNull Function<String, String> parser) {
        sendTo(sender, getLanguage(sender), path, parser);
    }

    protected void sendTo(@NotNull CommandSender sender, @NotNull String language, @NotNull String path, @NotNull Function<String, String> parser) {
        getDisplay(language, path).sendTo(sender, parser);
    }

    public void sendToConsole(@NotNull String path, @Nullable Object... args) {
        sendTo(Bukkit.getConsoleSender(), path, args);
    }

    public void sendToConsole(@NotNull String path, @NotNull Function<String, String> parser) {
        sendTo(Bukkit.getConsoleSender(), path, parser);
    }

    public void sendToAll(@NotNull String path, @Nullable Object... args) {
        sendToAll(defaultLanguage, path, args);
    }

    public void sendToAll(@NotNull String language, @NotNull String path, @Nullable Object... args) {
        sendToAll(language, path, s -> Strings.replaceArgs(s, args));
    }

    public void sendToAll(@NotNull String path, @NotNull Function<String, String> parser) {
        sendToAll(defaultLanguage, path, parser);
    }

    public void sendToAll(@NotNull String language, @NotNull String path, @NotNull Function<String, String> parser) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTo(player, language, path, parser);
        }
    }

    public void sendToAll(@NotNull String path, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> playerParser) {
        sendToAll(defaultLanguage, path, parser, playerParser);
    }

    public void sendToAll(@NotNull String language, @NotNull String path, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> playerParser) {
        getDisplay(language, path).sendTo(Bukkit.getOnlinePlayers(), parser, playerParser);
    }

    public static class Value {

        private final String path;

        private LangLoader loader;

        public Value(@NotNull String path) {
            this.path = path;
        }

        @NotNull
        public String getPath() {
            return path;
        }

        @Nullable
        protected LangLoader getLoader() {
            return loader;
        }

        @NotNull
        public LangDisplay getDisplay() {
            return loader.getDisplay(path);
        }

        @NotNull
        public LangDisplay getDisplay(@NotNull CommandSender sender) {
            return loader.getDisplay(sender, path);
        }

        @NotNull
        public String getText() {
            return loader.getLangText(path);
        }

        @NotNull
        public String getText(@NotNull CommandSender sender) {
            return loader.getLangText(sender, path);
        }

        protected void setLoader(@Nullable LangLoader loader) {
            this.loader = loader;
        }

        public void sendTo(@NotNull CommandSender sender, @Nullable Object... args) {
            loader.sendTo(sender, path, args);
        }

        public void sendTo(@NotNull CommandSender sender, @NotNull Function<String, String> parser) {
            loader.sendTo(sender, path, parser);
        }

        public void sendToConsole(@Nullable Object... args) {
            loader.sendToConsole(path, args);
        }

        public void sendToConsole(@NotNull Function<String, String> parser) {
            loader.sendToConsole(path, parser);
        }

        public void sendToAll(@Nullable Object... args) {
            loader.sendToAll(path, args);
        }

        public void sendToAll(@NotNull String language, @Nullable Object... args) {
            loader.sendToAll(language, path, args);
        }

        public void sendToAll(@NotNull Function<String, String> parser) {
            loader.sendToAll(path, parser);
        }

        public void sendToAll(@NotNull String language, @NotNull Function<String, String> parser) {
            loader.sendToAll(language, path, parser);
        }

        public void sendToAll(@NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> playerParser) {
            loader.sendToAll(path, parser, playerParser);
        }

        public void sendToAll(@NotNull String language, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> playerParser) {
            loader.sendToAll(language, path, parser, playerParser);
        }
    }
}
