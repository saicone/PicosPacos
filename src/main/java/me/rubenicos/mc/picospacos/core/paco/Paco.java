package me.rubenicos.mc.picospacos.core.paco;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.PicosPacosAPI;
import me.rubenicos.mc.picospacos.api.event.InventoryPacoEvent;
import me.rubenicos.mc.picospacos.api.event.ItemsPacoEvent;
import me.rubenicos.mc.picospacos.core.paco.rule.PacoRule;
import me.rubenicos.mc.picospacos.api.object.PlayerData;
import me.rubenicos.mc.picospacos.core.paco.rule.Parameter;
import me.rubenicos.mc.picospacos.core.paco.rule.RuleType;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Paco implements Listener {

    private final PicosPacos pl;
    private final Settings file;

    private final List<PacoRule> deathRules = new ArrayList<>();
    private final List<PacoRule> dropRules = new ArrayList<>();
    private final List<PacoRule> deleteRules = new ArrayList<>();
    private final Map<UUID, List<ItemStack>> players = new HashMap<>();
    private final Map<UUID, ItemStack> warnings = new HashMap<>();

    public Paco(PicosPacos pl) {
        this.pl = pl;
        file = new Settings(pl, "rules.yml", null, false, false);
        file.listener(this::onRulesReload);
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    public void disable() {
        players.forEach((player, items) -> PicosPacosAPI.getPlayerOrLoad(player).addItemsList(items));
        players.clear();
        deathRules.clear();
        dropRules.clear();
    }

    public void onRulesReload() {
        if (file.isLocked()) return;
        file.setLocked(true);
        deathRules.clear();
        dropRules.clear();
        if (!file.reload()) {
            pl.getLogger().severe("Cannot reload rules.yml file");
        } else {
            file.getKeys().forEach(key -> {
                if (file.isSection(key)) {
                    String type = file.getString(key + ".type");
                    if (!type.equals("null") || !type.isBlank()) {
                        List<RuleType> rules = new ArrayList<>();
                        for (String s : type.split(",")) {
                            rules.add(RuleType.of(s.trim()));
                        }
                        if (!rules.isEmpty() && !rules.contains(RuleType.DISABLED)) {
                            PacoRule rule = Parameter.ruleOf(file, key, rules);
                            if (rule == null) {
                                Locale.sendToConsole("Paco.Error.Tags", key);
                            } else {
                                if (rules.contains(RuleType.DEATH)) {
                                    deathRules.add(rule);
                                }
                                if (rules.contains(RuleType.DROP) || rules.contains(RuleType.NODROP)) {
                                    dropRules.add(rule);
                                }
                                if (rules.contains(RuleType.DELETE)) {
                                    deleteRules.add(rule);
                                }
                            }
                        }
                    } else {
                        Locale.sendToConsole("Paco.Error.Rule-Type", type);
                    }
                } else {
                    // TODO: Do stuff with JSON formatted string
                    if (!key.equals("File-Listener")) Locale.sendToConsole("Paco.Error.Section", key);
                }
            });
            Locale.sendToConsole("Paco.Loaded", String.valueOf(dropRules.size()), String.valueOf(deathRules.size()));
        }
        file.setLocked(false);
        file.resolveListener();
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
                player.takeSaves(1);
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

        UUID uuid = e.getPlayer().getUniqueId();
        if (warnings.containsKey(uuid) && e.getItemDrop().getItemStack().equals(warnings.get(uuid))) {
            warnings.remove(uuid);
            return;
        }

        dropRules.forEach(rule -> {
            if (rule.match(e.getItemDrop().getItemStack(), e.getPlayer())) {
                e.setCancelled(true);
                if (rule.containsRule(RuleType.NODROP)) {
                    Locale.sendTo(e.getPlayer(), "Paco.Drop.Error");
                } else {
                    warnings.put(uuid, e.getItemDrop().getItemStack());
                    Locale.sendTo(e.getPlayer(), "Paco.Drop.Warning");
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }
        for (PacoRule rule : deleteRules) {
            if (rule.match(item, (Player) e.getWhoClicked())) {
                e.setCancelled(true);
                item.setType(Material.AIR);
                e.setCurrentItem(item);
                final String ruleID = rule.getId();
                Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
                    for (String s : PicosPacos.getSettings().getStringList("Execute.onDelete")) {
                        String cmd = Locale.parsePlaceholders(
                                (Player) e.getWhoClicked(),
                                s.replace("{player}", e.getWhoClicked().getName()).replace("{rule}", ruleID));
                        Bukkit.getScheduler().runTask(pl, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                    }
                });
                return;
            }
        }
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
            UUID uuid = e.getPlayer().getUniqueId();
            warnings.remove(uuid);
            PlayerData data = PicosPacosAPI.getPlayerOrLoad(e.getPlayer());
            if (players.containsKey(uuid)) {
                data.addItemsList(players.get(uuid));
                players.remove(uuid);
            }
            PicosPacosAPI.savePlayer(data);
        });
    }
}
