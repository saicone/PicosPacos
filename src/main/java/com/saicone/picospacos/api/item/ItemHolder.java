package com.saicone.picospacos.api.item;

import com.saicone.picospacos.api.PicosPacosAPI;
import com.saicone.picospacos.api.data.PlayerData;
import com.saicone.picospacos.util.Items;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class ItemHolder implements Function<Object, Object> {

    private String event;
    private CommandSender user;
    private ItemStack item;

    private transient boolean cancelled;
    private transient boolean edited;
    private transient ItemStack editedItem;

    public ItemHolder(@NotNull String event, @NotNull CommandSender user, @NotNull ItemStack item) {
        this.event = event;
        this.user = user;
        this.item = item;
    }

    @ApiStatus.Internal
    @Contract("_ -> this")
    public ItemHolder next(@NotNull ItemStack item) {
        this.item = item;
        this.edited = false;
        this.editedItem = null;
        return this;
    }

    @ApiStatus.Internal
    @Contract("_ -> this")
    public ItemHolder next(@NotNull String event) {
        this.event = event;
        return this;
    }

    public boolean isPlayer() {
        return user instanceof Player;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isEdited() {
        return edited;
    }

    @NotNull
    public String getEvent() {
        return event;
    }

    @NotNull
    public Player getPlayer() {
        return (Player) user;
    }

    @NotNull
    public PlayerData getPlayerData() {
        return PicosPacosAPI.getPlayerData(getPlayer().getUniqueId());
    }

    @NotNull
    public ItemStack getItem() {
        return edited ? editedItem : item;
    }

    @NotNull
    public ItemStack getOriginalItem() {
        return item;
    }

    @Nullable
    public ItemStack getEditedItem() {
        return editedItem;
    }

    public void setItem(@NotNull ItemStack item) {
        this.edited = true;
        this.editedItem = item;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    public abstract String parse(@NotNull String s);

    @Override
    public Object apply(Object o) {
        if (o instanceof String) {
            return parse((String) o);
        }
        return o;
    }

    public void clear() {
        this.event = null;
        this.user = null;
        this.item = null;
        this.editedItem = null;
    }
}
