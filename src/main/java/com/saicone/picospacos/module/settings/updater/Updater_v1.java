package com.saicone.picospacos.module.settings.updater;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.module.settings.BukkitSettings;
import com.saicone.picospacos.module.settings.SettingsFile;
import com.saicone.picospacos.module.settings.SettingsUpdater;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Updater_v1 extends SettingsUpdater {

    private static final Map<String, String> MOVE = new LinkedHashMap<>();
    private static final Map<String, List<String>> COMMENT = new LinkedHashMap<>();

    private static final Map<String, String> LANG_MOVE = new LinkedHashMap<>();

    static {
        // Plugin
        MOVE.put("Locale.LogLevel", "plugin.log-level");
        MOVE.put("Locale.Language", "plugin.language");
        MOVE.put("Locale.PlayerProvider", "plugin.player-provider");
        MOVE.put("#Locale", "plugin");
        // Lang
        MOVE.put("Lang.Default", "lang.default");
        MOVE.put("Lang.Aliases", "lang.aliases");
        MOVE.put("#Lang", "lang");
        // Restore
        MOVE.put("Config.Respawn.Enabled", "restore.respawn.enabled");
        MOVE.put("Config.Respawn.Blacklist-Worlds", "restore.respawn.world-blacklist");
        MOVE.put("Config.Respawn.Delay", "restore.respawn.delay");
        MOVE.put("#Config.Respawn", "restore.respawn");
        MOVE.put("Config.Join.Enabled", "restore.join.enabled");
        MOVE.put("Config.Join.Blacklist-Worlds", "restore.join.world-blacklist");
        MOVE.put("Config.Join.Delay", "restore.join.delay");
        MOVE.put("#Config.Join", "restore.join");
        MOVE.put("Config", "");
        COMMENT.put("restore", List.of("Item restoration configuration"));
        // Database
        MOVE.put("Database.Type", "database.type");
        COMMENT.put("database.type", List.of(
                "Available types:",
                "",
                "SQL  = Save data on a sql database",
                "FILE = Save data locally on files"
        ));
        MOVE.put("Database.Method", "database.method");
        MOVE.put("Database.Sql", "database.sql");
        // Hook
        MOVE.put("Hook.PlaceholderAPI", "hook.PlaceholderAPI");
        MOVE.put("Hook.DeluxeCombat", "hook.DeluxeCombat");
        MOVE.put("#Hook", "hook");
        // Command
        MOVE.put("Command", "command");

        LANG_MOVE.put("Prefix", "prefix");
        LANG_MOVE.put("Paco.Drop.Error", "script.drop-message");
        LANG_MOVE.put("Paco", "");
        LANG_MOVE.put("Command.NoPerm", "command.permission");
        LANG_MOVE.put("Command.Help", "command.help");
        LANG_MOVE.put("Command.Reload", "command.reload");
        LANG_MOVE.put("Command.Saves.Usage", "command.saves.usage");
        LANG_MOVE.put("Command.Saves.Invalid-Amount", "command.saves.invalid");
        LANG_MOVE.put("Command.Saves.Give", "command.saves.give");
        LANG_MOVE.put("Command.Saves.Take", "command.saves.take");
        LANG_MOVE.put("Command.Saves.Set", "command.saves.set");
        LANG_MOVE.put("Command.Saves.Info", "command.saves.info");
        LANG_MOVE.put("Command", "");
    }

    public static void update(@NotNull SettingsFile settings) {
        move(settings, MOVE);
        comment(settings, COMMENT);

        // Execute
        final List<String> onDeleteCommands = settings.getStringList("Execute.onDelete");
        settings.set("Execute", null);

        // Database
        if ("UUID".equals(settings.getString("Database.Method"))) {
            settings.set("Database.Method", null);
        }
        final String databaseType = settings.getString("Database.Type", "JSON");
        switch (databaseType) {
            case "JSON":
                settings.set("Database.Type", "FILE");
                break;
            case "SQLITE":
            case "MYSQL":
                settings.set("Database.Type", "SQL");
                settings.set("database.sql.type", databaseType);
                break;
        }

        // Hook
        settings.set("hook.PlaceholderAPI.rule", true);

        // --- Migrate rules to scripts
        final File rulesFile = new File(PicosPacos.get().getDataFolder(), "rules.yml");
        if (rulesFile.exists()) {
            final File folder = new File(PicosPacos.get().getDataFolder(), "scripts");
            folder.mkdirs();
            File toFile = new File(folder, "default.yml");
            if (toFile.exists()) {
                toFile = new File(folder, "rules-migrated.yml");
            }
            save(rulesToScripts(BukkitSettings.of(rulesFile), onDeleteCommands), toFile);
            rulesFile.delete();
        }

        // --- Migrate lang
        final File langFolder = new File(PicosPacos.get().getDataFolder(), "lang");
        if (langFolder.exists()) {
            final File[] files = langFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    final BukkitSettings lang = BukkitSettings.of(file);
                    move(lang, LANG_MOVE);
                    save(lang, file);
                }
            }
        }
    }

    @NotNull
    private static BukkitSettings rulesToScripts(@NotNull BukkitSettings rules, @NotNull List<String> onDeleteCommands) {
        final BukkitSettings scripts = new BukkitSettings();
        for (String key : rules.getKeys(false)) {
            final Object value = rules.get(key);
            if (value instanceof ConfigurationSection) {
                final ConfigurationSection section = (ConfigurationSection) value;
                final Set<String> types = new HashSet<>();
                int count = 0;
                for (String type : section.getString("type", "").toUpperCase().split(",")) {
                    type = type.trim();
                    if (types.contains(type)) {
                        continue;
                    }
                    if ((type.equals("DROP") && types.contains("NODROP")) || type.equals("NODROP") && types.contains("DROP")) {
                        continue;
                    }
                    types.add(type);
                    rules.set(count > 0 ? key + "-" + count : key, ruleToScript(type, section, onDeleteCommands));
                    count++;
                }
            }
        }
        return scripts;
    }

    @NotNull
    private static ConfigurationSection ruleToScript(@NotNull String type, @NotNull ConfigurationSection section, @NotNull List<String> onDeleteCommands) {
        final BukkitSettings script = new BukkitSettings();

        final List<String> when = new ArrayList<>();
        final List<String> run = new ArrayList<>();

        for (String command : section.getStringList("commands")) {
            run.add("command: " + command);
        }

        switch (type) {
            case "DEATH":
                when.add("PLAYER_DIES");
                run.add("restore");
                break;
            case "DROP":
            case "NODROP":
                when.add("ITEM_DROP");
                run.add("lang: script.drop-message");
                run.add("cancel");
                break;
            case "DELETE":
            case "DETECT":
                when.add("PLAYER_JOIN");
                when.add("ITEM_PICKUP");
                when.add("ITEM_CLICK");
                if (type.equals("DELETE")) {
                    for (String command : onDeleteCommands) {
                        run.add("command: " + command);
                    }
                    run.add("delete");
                }
                break;
            case "DISABLED":
                script.set("enabled", false);
                break;
            default:
                break;
        }
        
        script.set("when", when);
        if (!run.isEmpty()) {
            script.set("run", run);
        }

        for (String key : section.getKeys(false)) {
            final String[] split = key.replace("<allowPapi>", "").split(":");
            final String comparator = split.length > 1 ? split[1].toLowerCase() : "equal";
            switch (split[0].toLowerCase()) {
                case "material":
                case "mat":
                    script.set("item.material", stringPredicate(comparator, section.getString(key)));
                    break;
                case "durability":
                    script.set("item.durability", numberPredicate(comparator, section.getString(key)));
                    break;
                case "amount":
                case "size":
                    script.set("item.amount", numberPredicate(comparator, section.getString(key)));
                    break;
                case "name":
                case "displayname":
                    script.set("item.name", stringPredicate(comparator, section.getString(key)));
                    break;
                case "lore":
                case "description":
                    script.set("item.lore" + iterableComparator(split[split.length - 1]), listPredicate(comparator, section.getStringList(key)));
                    break;
                case "custommodeldata":
                case "modeldata":
                    script.set("item.custom-model-data", numberPredicate(comparator, section.getString(key)));
                    break;
                case "enchantments":
                case "enchants":
                    final ConfigurationSection enchants = new BukkitSettings();
                    final boolean all = split[split.length - 1].equalsIgnoreCase("all");
                    final String levelComparator = split.length > 2 ? (all ? (split.length > 3 ? split[3] : "equal") : split[2]) : "equal";
                    for (String str : section.getStringList(key)) {
                        String[] s = str.split("=", 2);
                        final String enchant;
                        final String level;
                        if (s.length > 1) {
                            enchant = s[0];
                            level = s[1];
                        } else {
                            enchant = str;
                            level = "1";
                        }
                        enchants.set(stringPredicate(comparator, enchant), numberPredicate(levelComparator, level));
                    }
                    script.set("item.enchants" + iterableComparator(split[split.length - 1]), enchants);
                    break;
                case "flags":
                    script.set("item.flags" + iterableComparator(split[split.length - 1]), listPredicate(comparator, section.getStringList(key)));
                    break;
                default:
                    break;
            }
        }

        return script;
    }

    @Nullable
    @Contract("_, !null -> !null")
    private static String stringPredicate(@NotNull String comparator, @Nullable String value) {
        if (value == null) {
            return null;
        }
        if (comparator.equalsIgnoreCase("equal") || comparator.equalsIgnoreCase("all")) {
            return value;
        }
        return comparator + "=" + value;
    }

    @Nullable
    private static Object numberPredicate(@NotNull String comparator, @Nullable String value) {
        if (value == null) {
            return null;
        }
        switch (comparator.toLowerCase()) {
            case "equals":
            case "equal":
            case "exact":
            case "all":
                return value;
            case "more":
                return ">" + value;
            case "less":
                return "<" + value;
            case "between":
                final String[] split = value.split("-", 2);
                if (split.length < 2) {
                    return value;
                }
                return List.of(List.of(">=" + split[0], "<=" + split[1]));
            default:
                return null;
        }
    }

    @NotNull
    private static String iterableComparator(@NotNull String comparator) {
        if (comparator.equalsIgnoreCase("all")) {
            return "(contains)";
        } else {
            return "(any)";
        }
    }

    @Nullable
    private static Object listPredicate(@NotNull String comparator, @NotNull List<String> value) {
        if (value.isEmpty()) {
            return null;
        }
        return value.stream().map(s -> stringPredicate(comparator, s)).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
