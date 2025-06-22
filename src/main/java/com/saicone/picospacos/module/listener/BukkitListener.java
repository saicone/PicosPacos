package com.saicone.picospacos.module.listener;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.PicosPacosAPI;
import com.saicone.picospacos.api.data.PlayerData;
import com.saicone.types.Types;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BukkitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        restoreItems("join", event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        restoreItems("respawn", event.getPlayer());
    }

    private void restoreItems(@NotNull String type, @NotNull Player player) {
        if (!PicosPacos.settings().getIgnoreCase("restore", type, "enabled").asBoolean(true)) {
            return;
        }
        PicosPacosAPI.getPlayerDataAsync(player).thenAccept(data -> {
            if (data.getItems().isEmpty()) {
                return;
            }
            final List<String> blacklist = PicosPacos.settings().getIgnoreCase("config", type, "world-blacklist").asList(Types.STRING);
            if (blacklist.contains(player.getWorld().getName())) {
                return;
            }
            final int delay = PicosPacos.settings().getIgnoreCase("config", type, "delay").asInt(10);
            if (delay > 0) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(PicosPacos.get(), () -> restoreItems(player, data), delay * 20L);
            } else {
                restoreItems(player, data);
            }
        });
    }

    private void restoreItems(@NotNull Player player, @NotNull PlayerData data) {
        if (player.isOnline()) {
            player.getInventory().addItem(data.getItems().toArray(new ItemStack[0]));
            data.clearItems();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory()) return;

        final PlayerData data = PicosPacosAPI.getPlayerData(event.getEntity());
        if (data.getSaves() > 0) {
            event.setKeepInventory(true);
            data.setSaves(saves -> saves - 1);
        }
    }
}
