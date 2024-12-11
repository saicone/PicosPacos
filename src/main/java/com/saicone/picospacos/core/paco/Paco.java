package com.saicone.picospacos.core.paco;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.PicosPacosAPI;
import com.saicone.picospacos.api.event.InventoryPacoEvent;
import com.saicone.picospacos.api.event.ItemsPacoEvent;
import com.saicone.picospacos.api.object.PlayerData;
import com.saicone.picospacos.core.Lang;
import com.saicone.picospacos.core.paco.rule.PacoRule;
import com.saicone.picospacos.core.paco.rule.Parameter;
import com.saicone.picospacos.core.paco.rule.RuleType;
import com.saicone.picospacos.module.hook.Placeholders;
import com.saicone.picospacos.module.settings.SettingsFile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Paco implements Listener {

    private final PicosPacos pl;
    private final SettingsFile file;

    private final List<PacoRule> deathRules = new ArrayList<>();
    private final List<PacoRule> dropRules = new ArrayList<>();
    private final List<PacoRule> deleteRules = new ArrayList<>();
    private final Map<UUID, ItemStack> warnings = new HashMap<>();

    public Paco(PicosPacos pl) {
        this.pl = pl;
        this.file = new SettingsFile("rules.yml", true);
    }

    @NotNull
    public List<PacoRule> getRules(@NotNull RuleType type) {
        switch (type) {
            case DEATH:
                return deathRules;
            case DROP:
            case NODROP:
                return dropRules;
            case DELETE:
                return deleteRules;
            default:
                return List.of();
        }
    }

    @Nullable
    public PacoRule ruleMatches(@NotNull RuleType type, @Nullable ItemStack item, @NotNull Player player) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        for (PacoRule rule : getRules(type)) {
            if (rule.match(item, player)) {
                return rule;
            }
        }
        return null;
    }

    public void onLoad() {

    }

    public void onEnable() {
        onReload();
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    public void onDisable() {
        deathRules.clear();
        dropRules.clear();
    }

    public void onReload() {
        deathRules.clear();
        dropRules.clear();
        deleteRules.clear();
        file.loadFrom(pl.getDataFolder(), true);
        file.getKeys(false).forEach(key -> {
            if (file.isConfigurationSection(key)) {
                String type = file.getString(key + ".type");
                if (!type.equals("null") || !type.isBlank()) {
                    List<RuleType> rules = new ArrayList<>();
                    for (String s : type.split(",")) {
                        rules.add(RuleType.of(s.trim()));
                    }
                    if (!rules.isEmpty() && !rules.contains(RuleType.DISABLED)) {
                        PacoRule rule = Parameter.ruleOf(file, key, rules, file.getStringList(key + ".commands"));
                        if (rule == null) {
                            PicosPacos.log(2, "Rule '" + key + "' has not tags to parse!");
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
                    PicosPacos.log(2, "Unknown rule type '" + type + "', check rules.yml");
                }
            } else {
                // TODO: Do stuff with JSON formatted string
                PicosPacos.log(2, "Path '" + key + "' isn''t a section!");
            }
        });
        PicosPacos.log(3, "Paco loaded " + dropRules.size() + " drop rules and " + deathRules.size() + " death rules");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        if (e.getKeepInventory()) return;

        final PlayerData data = PicosPacosAPI.getPlayerData(e.getEntity());
        if (data.getSaves() > 0) {
            InventoryPacoEvent event = new InventoryPacoEvent(e.getEntity(), e.getDrops());
            pl.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                e.setKeepInventory(true);
                data.takeSaves(1);
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
                    data.addItemsList(event.getItems());
                    e.getDrops().removeAll(event.getItems());
                }
            }
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!PicosPacos.settings().getBoolean("Config.Respawn.Enabled")
                || PicosPacos.settings().getStringList("Config.Respawn.Blacklist-Worlds").contains(e.getPlayer().getWorld().getName())) {
            return;
        }
        final Player player = e.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            final PlayerData data = PicosPacosAPI.getPlayerDataAsync(player).join();
            if (!data.getItems().isEmpty()) {
                if (PicosPacos.settings().getInt("Config.Respawn-Delay", 0) > 0) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(pl, () -> {
                        if (player.isOnline()) {
                            player.getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
                            data.clearItems();
                        }
                    }, PicosPacos.settings().getInt("Config.Respawn-Delay", 0) * 20L);
                } else {
                    if (player.isOnline()) {
                        player.getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
                        data.clearItems();
                    }
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (!PicosPacos.settings().getBoolean("Config.Drop.Enabled")
                || PicosPacos.settings().getStringList("Config.Drop.Blacklist-Worlds").contains(e.getItemDrop().getWorld().getName())
                || !e.getPlayer().hasPermission(PicosPacos.settings().getString("Config.Drop.Permission", "picospacos.drop.protection"))) {
            return;
        }

        UUID uuid = e.getPlayer().getUniqueId();
        if (warnings.containsKey(uuid) && e.getItemDrop().getItemStack().equals(warnings.get(uuid))) {
            warnings.remove(uuid);
            return;
        }

        dropRules.forEach(rule -> {
            if (rule.match(e.getItemDrop().getItemStack(), e.getPlayer())) {
                e.setCancelled(true);
                if (rule.containsRule(RuleType.NODROP)) {
                    Lang.PACO_DROP_ERROR.sendTo(e.getPlayer(), s -> Placeholders.parse(e.getPlayer(), s));
                } else {
                    warnings.put(uuid, e.getItemDrop().getItemStack());
                    Lang.PACO_DROP_WARNING.sendTo(e.getPlayer(), s -> Placeholders.parse(e.getPlayer(), s));
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPickUp(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            final ItemStack item = e.getItem().getItemStack();
            if (deleteItem((Player) e.getEntity(), item)) {
                e.setCancelled(true);
                item.setType(Material.AIR);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        final ItemStack item = e.getCurrentItem();
        if (deleteItem((Player) e.getWhoClicked(), item)) {
            e.setCancelled(true);
            item.setType(Material.AIR);
            e.setCurrentItem(item);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(pl, () -> {
            final Inventory inventory = e.getPlayer().getInventory();
            for (int i = 0; i < inventory.getContents().length; i++) {
                final ItemStack item = inventory.getItem(i);
                if (deleteItem(e.getPlayer(), item)) {
                    item.setType(Material.AIR);
                    inventory.setItem(i, item);
                }
            }
        }, 100L);
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            PlayerData data = PicosPacosAPI.getPlayerDataAsync(e.getPlayer()).join();
            if (PicosPacos.settings().getBoolean("Config.Join.Enabled") && !PicosPacos.settings().getStringList("Config.Join.Blacklist-Worlds").contains(e.getPlayer().getWorld().getName())) {
                if (!data.getItems().isEmpty()) {
                    if (PicosPacos.settings().getInt("Config.Join.Delay", 10) > 0) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(pl, () -> {
                            if (e.getPlayer().isOnline()) {
                                e.getPlayer().getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
                                data.clearItems();
                            }
                        }, PicosPacos.settings().getInt("Config.Join.Delay", 10) * 20L);
                    } else {
                        if (e.getPlayer().isOnline()) {
                            e.getPlayer().getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
                            data.clearItems();
                        }
                    }
                }
            }
        });
    }

    private boolean deleteItem(@NotNull Player player, @Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        for (PacoRule rule : deleteRules) {
            if (rule.match(item, player)) {
                final String ruleID = rule.getId();
                Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
                    for (String s : PicosPacos.settings().getStringList("Execute.onDelete")) {
                        String cmd = Placeholders.parse(
                                player,
                                s.replace("{player}", player.getName()).replace("{rule}", ruleID));
                        Bukkit.getScheduler().runTask(pl, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                    }
                });
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        warnings.remove(e.getPlayer().getUniqueId());
    }
}
