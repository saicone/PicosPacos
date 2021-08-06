package me.rubenicos.mc.picospacos.api.object;

import me.rubenicos.mc.picospacos.util.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PlayerData {

    private final String name;
    private final String uuid;
    private int saves;
    private final List<ItemStack> items = new ArrayList<>();

    private boolean edited = false;
    private boolean onDatabase = false;

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

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public boolean hasEdited() {
        return edited;
    }

    public void setOnDB(boolean onDatabase) {
        this.onDatabase = onDatabase;
    }

    public boolean onDB() {
        return onDatabase;
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

    public void takeSaves(int amount) {
        edited = true;
        saves = saves - amount;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public String items() {
        return !items.isEmpty() ? ItemUtils.itemArrayToBase64(items.toArray(new ItemStack[0])) : "nothing";
    }

    public void addItemsBase64(String items) {
        if (!items.equals("nothing")) {
            for (ItemStack item : ItemUtils.itemArrayFromBase64(items)) {
                if (item != null) this.items.add(item);
            }
        }
    }

    public void addItemsList(List<ItemStack> items) {
        edited = true;
        this.items.addAll(items);
    }

    public boolean isOnDatabase() {
        return onDatabase;
    }

    public boolean isTrash() {
        return items.isEmpty() && saves == 0;
    }
}
