package com.saicone.picospacos.api.data;

import com.saicone.picospacos.module.hook.PlayerProvider;
import com.saicone.rtag.item.ItemTagStream;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class PlayerData {

    private final String name;
    private final UUID uniqueId;

    private int saves;
    private final List<ItemStack> items = new ArrayList<>();
    private List<ItemStack> savedItems = new ArrayList<>();
    private List<ItemStack> takenItems = new ArrayList<>();

    private transient boolean edited = false;
    private transient Boolean saved;

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

    public void load(@NotNull PlayerData data) {
        if (data.saved && !this.saved) {
            this.saved = true;
            this.saves = this.saves + data.saves;
            this.items.addAll(data.items);
            this.savedItems.addAll(data.savedItems);
            this.takenItems.addAll(data.takenItems);
        }
    }

    public boolean isEdited() {
        return edited;
    }

    public boolean isLoaded() {
        return saved != null;
    }

    public boolean isSaved() {
        return Boolean.TRUE.equals(saved);
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
        return Collections.unmodifiableList(items);
    }

    @NotNull
    public List<ItemStack> getSavedItems() {
        return Collections.unmodifiableList(savedItems);
    }

    @NotNull
    public List<ItemStack> getTakenItems() {
        return Collections.unmodifiableList(takenItems);
    }

    public String getItemsBase64() {
        return !items.isEmpty() ? ItemTagStream.INSTANCE.listToBase64(items) : "";
    }

    public int getSaves() {
        return saves;
    }

    public void setSaves(int saves) {
        edited = true;
        this.saves = saves;
    }

    public void setSaves(@NotNull UnaryOperator<Integer> operator) {
        edited = true;
        this.saves = operator.apply(this.saves);
    }

    public void setSavedItems(@NotNull List<ItemStack> savedItems) {
        this.savedItems = new ArrayList<>(savedItems);
    }

    public void setTakenItems(@NotNull List<ItemStack> takenItems) {
        this.takenItems = new ArrayList<>(takenItems);
    }

    public void addItemsBase64(String items) {
        this.items.addAll(ItemTagStream.INSTANCE.listFromBase64(items));
    }

    public void addItemsList(List<ItemStack> items) {
        edited = true;
        this.items.addAll(items);
    }

    public void addItems(@NotNull ItemStack... items) {
        Collections.addAll(this.items, items);
    }

    public void addSavedItems(@NotNull ItemStack... savedItems) {
        Collections.addAll(this.savedItems, savedItems);
    }

    public void addTakenItems(@NotNull ItemStack... takenItems) {
        Collections.addAll(this.takenItems, takenItems);
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
