package com.saicone.picospacos.api.item;

import com.saicone.picospacos.api.PicosPacosAPI;
import com.saicone.picospacos.api.data.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class ItemHolder implements Function<Object, Object> {

    private ItemScript script;
    private Object event;
    private CommandSender user;
    private ItemStack item;

    private transient boolean edited;
    private transient ItemStack editedItem;

    public ItemHolder(@Nullable ItemScript script, @Nullable Object event, @NotNull CommandSender user, @NotNull ItemStack item) {
        this.script = script;
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
    @Contract("_, _ -> this")
    public ItemHolder next(@NotNull ItemScript script, @NotNull Object event) {
        this.script = script;
        this.event = event;
        return this;
    }

    public boolean isPlayer() {
        return user instanceof Player;
    }

    public boolean isEdited() {
        return edited;
    }

    @NotNull
    public CommandSender getUser() {
        return user;
    }

    @NotNull
    public ItemScript getScript() {
        return script;
    }

    @NotNull
    public Object getEvent() {
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
        this.script = null;
        this.event = null;
        this.user = null;
        this.item = null;
        this.edited = false;
        this.editedItem = null;
    }
}
