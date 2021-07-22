package me.rubenicos.mc.picospacos.module.hook.type;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.PicosPacosAPI;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.hook.HookType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HookPlaceholderAPI implements HookType {

    private final PicosPacos pl = PicosPacos.get();
    private final Map<String, PlaceholderExpansion> expansions = new HashMap<>();

    private List<String> identifiers;
    private int cacheTime = 20;
    private final Map<String, String> cache = new HashMap<>();

    @Override
    public String pluginName() {
        return "PlaceholderAPI";
    }

    @Override
    public void load() {
        Locale.allowPAPI = true;
        loadIdentifiers();
        for (String identifier : identifiers) {
            expansions.put(identifier, new Expansion(this, identifier));
        }
        loadCacheTime();
    }

    @Override
    public void reload() {
        loadIdentifiers();
        for (String id : identifiers) {
            if (!expansions.containsKey(id)) {
                expansions.put(id, new Expansion(this, id));
                expansions.get(id).register();
            }
        }
        List<String> removed = new ArrayList<>();
        expansions.forEach((id, expansion) -> {
            if (!identifiers.contains(id)) {
                unregister(expansion);
                removed.add(id);
            }
        });
        for (String id : removed) {
            expansions.remove(id);
        }

        loadCacheTime();
    }

    @Override
    public void unload() {
        Locale.allowPAPI = false;
        expansions.forEach((id, expansion) -> unregister(expansion));
        expansions.clear();
        identifiers.clear();
        cache.clear();
    }

    private void loadIdentifiers() {
        identifiers = PicosPacos.getSettings().getStringList("Hook.PlaceholderAPI.identifiers");
    }

    private void loadCacheTime() {
        cacheTime = PicosPacos.getSettings().getInt("Hook.PlaceholderAPI.cache");
        if (cacheTime < 1) {
            cacheTime = 20;
        }
    }

    // Compatibility with PlaceholderAPI 2.10.6 and below
    private void unregister(PlaceholderExpansion expansion) {
        try {
            // This method was added on 2.10.7
            expansion.unregister();
        } catch (NoSuchMethodError e) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "placeholderapi unregister " + expansion.getIdentifier());
        }
    }

    protected String handleRequest(Player player, String params) {
        if (cache.containsKey(player.getName() + "|" + params)) {
            return cache.get(player.getName() + "|" + params);
        }
        String[] args = params.split("_");

        boolean save = args[args.length - 1].toLowerCase().startsWith("cache");
        int time = 0;
        if (save) {
            String[] s = args[args.length - 1].split(":");
            time = (s.length > 1 ? parseInt(s[1], cacheTime) : cacheTime);
            args = Arrays.copyOf(args, args.length - 1);
        }

        String result = "";
        if (args.length < 1) {
            return "Without enought args";
        } else if (args[0].toLowerCase().equals("saves")) {
            result = String.valueOf(PicosPacosAPI.getPlayer(player).getSaves());
        }

        return (save ? cache(player.getName() + "|" + params, result, time) : result);
    }

    private String cache(String identifier, String result, int time) {
        if (time < 1) {
            time = cacheTime;
        }
        if (!cache.containsKey(identifier)) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(pl, () -> cache.remove(identifier), 20L * time);
        }
        cache.put(identifier, result);
        return result;
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static final class Expansion extends PlaceholderExpansion {

        private final HookPlaceholderAPI main;
        private final String identifier;

        public Expansion(HookPlaceholderAPI main, String identifier) {
            this.main = main;
            this.identifier = identifier;
        }

        @Override
        public @NotNull String getIdentifier() {
            return identifier;
        }

        @Override
        public @NotNull String getAuthor() {
            return "Rubenicos";
        }

        @Override
        public @NotNull String getVersion() {
            return "1.0";
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) {
                return "Player can't be null";
            } else {
                return main.handleRequest(player, params);
            }
        }
    }
}
