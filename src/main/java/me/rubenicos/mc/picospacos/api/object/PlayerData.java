package me.rubenicos.mc.picospacos.api.object;

import me.rubenicos.mc.picospacos.module.Locale;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {

    private final String name;
    private final String uuid;
    private int saves;
    private final List<ItemStack> items = new ArrayList<>();

    private boolean edited = false;

    public PlayerData(Player player) {
        this(player, 0);
    }

    public PlayerData(Player player, int saves) {
        this(player.getName(), player.getUniqueId().toString(), saves);
    }

    public PlayerData(String name, String uuid, int saves) {
        this.name = name;
        this.uuid = uuid;
        this.saves = saves;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean hasEdited() {
        return edited;
    }

    public int getSaves() {
        return saves;
    }

    public void setSaves(int saves) {
        edited = true;
        this.saves = saves;
    }

    public void addSaves(int amount) {
        edited = true;
        saves = saves + amount;
    }

    public void reduceSaves(int amount) {
        edited = true;
        saves = saves - amount;
    }

    public List<ItemStack> getItems() {
        edited = true;
        return items;
    }

    public String toString() {
        return Locale.replaceArgs("{\"name\":\"{0}\",\"uuid\":\"{1}\",\"saves\":{2}}", name, uuid, String.valueOf(saves));
    }
}
