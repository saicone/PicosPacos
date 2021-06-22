package me.rubenicos.mc.picospacos.api.object;

import me.rubenicos.mc.picospacos.module.Locale;
import org.bukkit.entity.Player;

public class PlayerData {

    private final String name;
    private final String uuid;
    private boolean edited = false;
    private int saves;

    public PlayerData(Player player) {
        this(player, 0);
    }

    public PlayerData(Player player, int saves) {
        name = player.getName();
        uuid = player.getUniqueId().toString();
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

    public String toString() {
        return Locale.replaceArgs("{\"name\":\"{0}\",\"uuid\":\"{1}\",\"saves\":{2}}", name, uuid, String.valueOf(saves));
    }
}
