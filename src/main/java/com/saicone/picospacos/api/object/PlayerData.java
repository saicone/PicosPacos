package com.saicone.picospacos.api.object;

import com.saicone.picospacos.module.hook.PlayerProvider;
import com.saicone.picospacos.util.ItemUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private final String name;
    private final UUID uniqueId;

    private int saves;
    private final List<ItemStack> items = new ArrayList<>();

    private transient boolean edited = false;
    private transient boolean saved = false;

    public PlayerData(@NotNull OfflinePlayer player) {
        this(player, 0);
    }

    public PlayerData(@NotNull OfflinePlayer player, int saves) {
        this(player.getName() != null ? player.getName() : PlayerProvider.getName(player.getUniqueId()), player.getUniqueId(), saves);
    }

    public PlayerData(@NotNull String name, @NotNull UUID uniqueId, int saves) {
        this.name = name;
        this.uniqueId = uniqueId;
        this.saves = saves;
    }

    public boolean isEdited() {
        return edited;
    }

    public boolean isSaved() {
        return saved;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    @NotNull
    public List<ItemStack> getItems() {
        return items;
    }

    public String getItemsBase64() {
        return !items.isEmpty() ? ItemUtils.itemsToBase64(items) : "";
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

    public void addItemsBase64(String items) {
        this.items.addAll(ItemUtils.itemsFromBase64(items));
    }

    public void addItemsList(List<ItemStack> items) {
        edited = true;
        this.items.addAll(items);
    }

    public void clearItems() {
        edited = true;
        this.items.clear();
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
