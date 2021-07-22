package me.rubenicos.mc.picospacos.core.paco;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.PicosPacosAPI;
import me.rubenicos.mc.picospacos.api.event.InventoryPacoEvent;
import me.rubenicos.mc.picospacos.api.event.ItemsPacoEvent;
import me.rubenicos.mc.picospacos.core.paco.rule.PacoRule;
import me.rubenicos.mc.picospacos.api.object.PlayerData;
import me.rubenicos.mc.picospacos.core.paco.rule.Parameter;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.Settings;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Paco implements Listener {

    private final PicosPacos pl;
    private final Settings file;

    private final List<PacoRule> deathRules = new ArrayList<>();
    private final List<PacoRule> dropRules = new ArrayList<>();
    private final Map<UUID, List<ItemStack>> players = new HashMap<>();
    private final List<UUID> warnings = new ArrayList<>();

    public Paco(PicosPacos pl) {
        this.pl = pl;
        file = new Settings(pl, "rules.yml", null, false, false);
        file.listener(this::onRulesReload);
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    public void disable() {
        players.forEach((player, items) -> PicosPacosAPI.getPlayerOrLoad(player).addItems(items));
        players.clear();
        deathRules.clear();
        dropRules.clear();
    }

    public void onRulesReload() {
        if (!file.reload()) {
            pl.getLogger().severe("Cannot reload rules.yml file");
        } else {
            file.getKeys().forEach(key -> {
                if (file.isSection(key)) {
                    String type = file.getString(key + ".type");
                    if (!type.equals("null") || !type.isBlank()) {
                        String[] types = type.split(",");
                        PacoRule rule = null;
                        boolean invalid = false;
                        for (String s : types) {
                            if (invalid) break;
                            s = s.trim();
                            if (s.equalsIgnoreCase("DEATH")) {
                                if (rule == null) {
                                    rule = Parameter.ruleOf(file, key);
                                }
                                if (rule == null) {
                                    invalid = true;
                                    Locale.sendToConsole("Paco.Error.Tags", key);
                                } else {
                                    deathRules.add(rule);
                                }
                            } else if (s.equalsIgnoreCase("DROP")) {
                                if (rule == null) {
                                    rule = Parameter.ruleOf(file, key);
                                }
                                if (rule == null) {
                                    invalid = true;
                                    Locale.sendToConsole("Paco.Error.Tags", key);
                                } else {
                                    dropRules.add(rule);
                                }
                            } else if (!s.equalsIgnoreCase("DISABLED")) {
                                Locale.sendToConsole("Paco.Error.Rule-Type", s, key);
                            }
                        }
                    } else {
                        Locale.sendToConsole("Paco.Error.Rule-Type", type, key);
                    }
                } else {
                    // TODO: Do stuff with JSON formatted string
                    Locale.sendToConsole("Paco.Error.Section", key);
                }
            });
            Locale.sendToConsole("Paco.Loaded", String.valueOf(deathRules.size() + dropRules.size()));
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (e.getKeepInventory()) return;

        PlayerData player = PicosPacosAPI.getPlayer(e.getEntity());
        if (player != null && player.getSaves() > 0) {
            InventoryPacoEvent event = new InventoryPacoEvent(e.getEntity(), e.getDrops());
            pl.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                e.setKeepInventory(true);
                player.reduceSaves(1);
                return;
            }
        }

        deathRules.forEach(rule -> {
            List<ItemStack> matches = new ArrayList<>();
            e.getDrops().forEach(item -> {
                if (rule.match(item, e.getEntity())) {
                    matches.add(item);
                }
            });
            if (!matches.isEmpty()) {
                ItemsPacoEvent event = new ItemsPacoEvent(e.getEntity(), rule, matches);
                pl.getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    if (players.containsKey(e.getEntity().getUniqueId())) {
                        players.get(e.getEntity().getUniqueId()).addAll(event.getItems());
                    } else {
                        players.put(e.getEntity().getUniqueId(), event.getItems());
                    }
                    e.getDrops().removeAll(event.getItems());
                }
            }
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!PicosPacos.getSettings().getBoolean("Config.Respawn.Enabled") || PicosPacos.getSettings().getStringList("Config.Respawn.Blacklist-Worlds").contains(e.getPlayer().getWorld().getName())) return;
        if (players.containsKey(e.getPlayer().getUniqueId())) {
            if (PicosPacos.getSettings().getInt("Config.Respawn-Delay", 0) > 0) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(pl, () -> {
                    if (e.getPlayer().isOnline()) {
                        e.getPlayer().getInventory().addItem(players.get(e.getPlayer().getUniqueId()).toArray(new ItemStack[0]));
                        players.get(e.getPlayer().getUniqueId()).clear();
                    }
                }, PicosPacos.getSettings().getInt("Config.Respawn-Delay", 0) * 20L);
            } else {
                if (e.getPlayer().isOnline()) {
                    e.getPlayer().getInventory().addItem(players.get(e.getPlayer().getUniqueId()).toArray(new ItemStack[0]));
                    players.get(e.getPlayer().getUniqueId()).clear();
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (e.isCancelled() || !PicosPacos.getSettings().getBoolean("Config.Drop.Enabled") || PicosPacos.getSettings().getStringList("Config.Drop.Blacklist-Worlds").contains(e.getItemDrop().getWorld().getName()) || !e.getPlayer().hasPermission(PicosPacos.getSettings().getString("Config.Drop.Permission", "picospacos.drop.protection"))) return;

        if (warnings.contains(e.getPlayer().getUniqueId())) {
            warnings.remove(e.getPlayer().getUniqueId());
            return;
        }

        dropRules.forEach(rule -> {
            if (rule.match(e.getItemDrop().getItemStack(), e.getPlayer())) {
                e.setCancelled(true);
                warnings.add(e.getPlayer().getUniqueId());
                Locale.sendTo(e.getPlayer(), "Paco.Drop.Warning");
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            PlayerData data = PicosPacosAPI.loadPlayer(e.getPlayer());
            if (PicosPacos.getSettings().getBoolean("Config.Join.Enabled") && !PicosPacos.getSettings().getStringList("Config.Join.Blacklist-Worlds").contains(e.getPlayer().getWorld().getName())) {
                if (!data.getItems().isEmpty()) {
                    if (PicosPacos.getSettings().getInt("Config.Join.Delay", 10) > 0) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(pl, () -> {
                            if (e.getPlayer().isOnline()) {
                                e.getPlayer().getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
                                data.getItems().clear();
                                data.setEdited(true);
                            }
                        }, PicosPacos.getSettings().getInt("Config.Join.Delay", 10) * 20L);
                    } else {
                        if (e.getPlayer().isOnline()) {
                            e.getPlayer().getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
                            data.getItems().clear();
                            data.setEdited(true);
                        }
                    }
                }
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            if (players.containsKey(e.getPlayer().getUniqueId())) {
                PicosPacosAPI.getPlayerOrLoad(e.getPlayer()).addItems(players.get(e.getPlayer().getUniqueId()));
                players.remove(e.getPlayer().getUniqueId());
            }
            PicosPacosAPI.savePlayer(e.getPlayer());
        });
    }
}
