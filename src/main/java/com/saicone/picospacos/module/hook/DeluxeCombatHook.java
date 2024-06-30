package com.saicone.picospacos.module.hook;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.PicosPacosAPI;
import com.saicone.picospacos.api.object.PlayerData;
import com.saicone.picospacos.core.paco.Paco;
import com.saicone.picospacos.core.paco.rule.RuleType;
import nl.marido.deluxecombat.events.CombatlogEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeluxeCombatHook implements Listener {

    private final Paco paco;

    public DeluxeCombatHook(@NotNull Paco paco) {
        this.paco = paco;
    }

    public void load() {
        Bukkit.getPluginManager().registerEvents(this, PicosPacos.get());
    }

    @EventHandler
    public void onCombatLog(CombatlogEvent event) {
        final Player player = event.getCombatlogger();
        final Inventory inventory = player.getInventory();

        final List<ItemStack> savedItems = new ArrayList<>();
        int slot = 0;
        for (ItemStack item : inventory.getContents()) {
            if (paco.ruleMatches(RuleType.DROP, item, player) != null) {
                inventory.setItem(slot, null);
                savedItems.add(item);
            }
            slot++;
        }

        if (!savedItems.isEmpty()) {
            PlayerData data = PicosPacosAPI.getPlayerOrLoad(player);
            data.addItemsList(savedItems);
            PicosPacosAPI.savePlayer(data);
        }
    }
}
